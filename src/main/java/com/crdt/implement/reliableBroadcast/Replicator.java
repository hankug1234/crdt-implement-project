package com.crdt.implement.reliableBroadcast;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

import com.crdt.implement.opBaseCrdt.OpBaseCrdtOperation;
import com.crdt.implement.opBaseCrdt.ReplicationState;
import com.crdt.implement.opBaseCrdt.ReplicationStatus;
import com.crdt.implement.persistence.OpBaseCrdtDB;
import com.crdt.implement.vectorClock.Ord;
import com.crdt.implement.vectorClock.VectorClock;

import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy.Stop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.StashBuffer;
import akka.actor.typed.javadsl.TimerScheduler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Replicator<S,Q,C,E> extends AbstractBehavior<OpBaseProtocal>{
	
	private final StashBuffer<OpBaseProtocal> buffer;
	private OpBaseCrdtDB<S,E> db;
	private OpBaseCrdtOperation<S,Q,C,E> crdt;
	private String replicaId;
	private ReplicationState<S> state;
	private Object snapshotTimerKey;
	private Map<String,ReplicationStatus> replicatingNodes;
	private TimerScheduler<OpBaseProtocal> timer;
	private Duration timeInterval;
	
	public Replicator(ActorContext<OpBaseProtocal> context,StashBuffer<OpBaseProtocal> buffer,OpBaseCrdtDB<S,E> db,String replicaId
			,OpBaseCrdtOperation<S,Q,C,E> crdt,Duration timeInterval) {
		super(context);
		this.buffer = buffer;
		this.db = db;
		this.crdt = crdt;
		this.replicaId = replicaId;
		this.snapshotTimerKey = new Object();
		this.replicatingNodes = new HashMap<>();
		this.timeInterval = timeInterval;
	}
	
	public static <E> void replay(String replicatedNodeId, VectorClock filter,EndPoint target,List<OpBaseEvent<E>> events, int count){
		long lastSeqNr = 0L;
		List<OpBaseEvent<E>> buffers = new ArrayList<>();
		for(OpBaseEvent<E> event : events) {
			if(event.getVectorClock().compareTo(filter) > Ord.Eq.getValue()) {
				buffers.add(event);
				count -=1;
				lastSeqNr = Long.max(lastSeqNr,event.getLocalSeqNr());
			}
			
			if(count == 0) {
				break;
			}
		}
		
		target.getEndpoint().tell(new Protocal.Replicated<E>(replicatedNodeId,lastSeqNr,buffers));
	}
	
	private void refreshTimeout(String replicatedNodeId) {
		Object timerKey = this.replicatingNodes.get(replicatedNodeId).getTimeoutKey();
		timer.cancel(timerKey);
		timer.startTimerAtFixedRate(timerKey,new Protocal.ReplicateTimeout(replicatedNodeId),timeInterval);
	}
	
	public static <S,Q,C,E>  Behavior<OpBaseProtocal> create(OpBaseCrdtDB<S,E> db,String replicaId,OpBaseCrdtOperation<S,Q,C,E> crdt,Duration timeInterval){
		return Behaviors.withStash(100, stash->{
			return Behaviors.setup(ctx->{
				
				CompletionStage<ReplicationState<S>> latestState =  db.LoadSnapshot(crdt::copy).thenCompose((Optional<ReplicationState<S>> option)->{
					final ReplicationState<S> state = option.orElseGet(()->new ReplicationState<S>(replicaId,crdt.Default()));
					
					CompletableFuture<List<OpBaseEvent<E>>> events = db.LoadEvents(state.getSeqNr()+1L);
					
					return events.thenCompose((List<OpBaseEvent<E>> list) -> {
						return CompletableFuture.supplyAsync(()->{
							for(OpBaseEvent<E> event : list) {
								state.setCrdt(crdt.Effect(state.getCrdt(), event));
								state.setSeqNr(event.getLocalSeqNr());
								state.getVectorClock().merge(event.getVectorClock());
								state.getObserved().merge(event.getOriginReplicaId(),event.getOriginSeqNr(),(a,b)->Long.max(a,b));
							}
							return state;
						});
					});
				});
				
				ctx.pipeToSelf(latestState, (value,cause)->{
					if(cause == null) {
						return new Protocal.Loaded<S>(value);
					}else {
						log.info(cause.getMessage());
						return new Protocal.Stop();
					}
				});
				
				return new Replicator<S,Q,C,E>(ctx,stash,db,replicaId,crdt,timeInterval);
			});
		});
	}
	
	private Behavior<OpBaseProtocal> onInit(Object protocal){
		getContext().getLog().info("{} : initialize",replicaId);
		if(protocal instanceof Protocal.Loaded) {
			getContext().getLog().info("{} : get Loaded protocal",replicaId);
			Protocal.Loaded<S> castProtocal = (Protocal.Loaded<S>) protocal; 
			this.state = castProtocal.getReplicationState();
			return buffer.unstashAll(timerSettingActive((OpBaseProtocal)protocal));
		}else if(protocal instanceof Protocal.Stop) {
			getContext().getLog().info("{} : get stop protocal",replicaId);
			return Behaviors.stopped();
		}
		else {
			getContext().getLog().info("{} : isn't get Loaded protocal so stash called",replicaId);
			buffer.stash((OpBaseProtocal)protocal);
			return Behaviors.same();
		}
	}
	
	private Behavior<OpBaseProtocal> onQuery(Protocal.Query query){
		getContext().getLog().info("{} get query protocal from {}",replicaId,query.getReplyTo().toString());
		query.getReplyTo().tell(new Protocal.Res<Q>(crdt.Query(this.state.getCrdt())));
		return Behaviors.same();
	}
	
	private Behavior<OpBaseProtocal> onReplicate(Protocal.Replicate replicate){
		getContext().getLog().info("{} : get replicate protocal from {}",replicaId,replicate.getReplayTo().toString());
		CompletionStage<List<OpBaseEvent<E>>> events = db.LoadEvents(replicate.getSeqNr());
		events.whenComplete((value,cause)->{
			if(cause == null) {
				Replicator.replay(replicaId, replicate.getFilter(), replicate.getReplayTo(), value, replicate.getMaxCount());
			}else {
				//error return code
			}
		});
		return Behaviors.same();
	}
	
	private Behavior<OpBaseProtocal> onReplicated(Protocal.Replicated<E> replicated){
		getContext().getLog().info("{} : get replicated protocal from {}",replicaId,replicated.getFrom());
		List<OpBaseEvent<E>> events = replicated.getEvents();
		long observedSeqNr = this.state.getObserved().getOrDefault(replicated.getFrom(), 0L);
		
		if(events.isEmpty()) {
			refreshTimeout(replicated.getFrom());
			if(replicated.getToSeqNr() > observedSeqNr) {
				this.state.getObserved().merge(replicated.getFrom(), replicated.getToSeqNr(),(a,b)->Long.max(a,b));
			}
			getContext().getLog().info("{} : observed time {} vectorClock {}",replicaId,this.state.getObserved().toString(),this.state.getVectorClock().toString());
			db.SaveSnapshot(state.clone(crdt.copy(state.getCrdt())));
		}else {
			List<OpBaseEvent<E>> saveBuffers = new ArrayList<>();
			S crdtState = this.state.getCrdt();
			
			for(OpBaseEvent<E> event : events) {
				if(this.state.unseen(event)) {
					this.state.setSeqNr(this.state.getSeqNr()+1L);
					this.state.getVectorClock().merge(event.getVectorClock());
					observedSeqNr = Long.max(observedSeqNr,event.getLocalSeqNr());
					
					OpBaseEvent<E> newEvent = new OpBaseEvent<>(event.getOriginReplicaId()
							,event.getOriginSeqNr(),this.state.getSeqNr(),event.getVectorClock(),event.getData());
					
					saveBuffers.add(newEvent);
					
					crdtState = this.crdt.Effect(crdtState, event);
				}
			}
			
			this.state.getObserved().merge(replicated.getFrom(), observedSeqNr, (a,b)->Long.max(a,b));
			
			this.state.setCrdt(crdtState);
			db.SaveEvents(saveBuffers);
			
			this.replicatingNodes.get(replicated.getFrom()).getEndpoint()
			.getEndpoint().tell(new Protocal.Replicate(state.getSeqNr()+1L, 100, state.getVectorClock().clone(),new EndPoint(getContext().getSelf())));
			
			refreshTimeout(replicated.getFrom());
		}
		
		return Behaviors.same();
	}
	
	private Behavior<OpBaseProtocal> onReplicateTimeout(Protocal.ReplicateTimeout timeout){
		getContext().getLog().info("{} : get timeout protocal about {}",replicaId,timeout.getReplicaId());
		
		long observedSeqNr = this.state.getObserved().getOrDefault(timeout.getReplicaId(), 0L);
		EndPoint replyTo = this.replicatingNodes.get(timeout.getReplicaId()).getEndpoint();
		replyTo.getEndpoint().tell(new Protocal.Replicate(observedSeqNr+1L, 100,this.state.getVectorClock().clone(), new EndPoint(getContext().getSelf())));
		return Behaviors.same();
	}
	
	private Behavior<OpBaseProtocal> onCommand(Protocal.Command<C> command){
		getContext().getLog().info("{} : get command protocal from {}",replicaId,command.getReplyTo().toString());
		
		this.state.setSeqNr(this.state.getSeqNr()+1L);
		this.state.getVectorClock().inc(state.getReplicaId());
		
		E data = this.crdt.Prepare(this.state.getCrdt(), command.getCommand());
		OpBaseEvent<E> event = new OpBaseEvent<>(this.replicaId, this.state.getSeqNr(), this.state.getSeqNr(), this.state.getVectorClock().clone(), data);
		
		this.state.setCrdt(this.crdt.Effect(this.state.getCrdt(), event));
		
		db.SaveEvents(List.of(event));
		
		
		command.getReplyTo().tell(new Protocal.Res<Q>(crdt.Query(this.state.getCrdt())));
		
		getContext().getLog().info("{} : observed : {} , vectorClock : {} ",replicaId,this.state.getObserved().toString(),this.state.getVectorClock().toString());
		
		return Behaviors.same();
	}
	
	private Behavior<OpBaseProtocal> onConnect(Protocal.Connect connect){
		
		getContext().getLog().info("{} : get connect protocal to {}",replicaId,connect.getReplicaId());
		
		long observedSeqNr = this.state.getObserved().getOrDefault(connect.getReplicaId(), 0L);
		connect.getEndpoint().getEndpoint().tell(new Protocal.Replicate(observedSeqNr+1L, 100, state.getVectorClock().clone(), new EndPoint(getContext().getSelf())));
		Object timerKey = new Object();
		this.replicatingNodes.put(connect.getReplicaId(), new ReplicationStatus(connect.getEndpoint(),timerKey));
		
		timer.startTimerAtFixedRate(timerKey,new Protocal.ReplicateTimeout(connect.getReplicaId()),timeInterval);
		return Behaviors.same();
	}
	
	private Behavior<OpBaseProtocal> onSnapshot(Protocal.Snapshot snapshot){
		getContext().getLog().info("{} : get snapshot protocal",replicaId);
		
		if(this.state.isDirty()) {
			this.state.setDirty(false);
			db.SaveSnapshot(state.clone(crdt.copy(state.getCrdt())));
		}
		
		return Behaviors.same();
	}
	
	private Behavior<OpBaseProtocal> timerSettingActive(OpBaseProtocal protocal){
		getContext().getLog().info("{} : timersetting",replicaId);
		
		return Behaviors.withTimers(timer -> {
			timer.startTimerAtFixedRate(snapshotTimerKey,new Protocal.Snapshot(),timeInterval);
			return active(protocal,timer);
		});
	}
	
	private Behavior<OpBaseProtocal> active(OpBaseProtocal protocal,TimerScheduler<OpBaseProtocal> timer){
		this.timer = timer;
		return Behaviors.receive(OpBaseProtocal.class)
				.onMessage(Protocal.Query.class, this::onQuery)
				.onMessage(Protocal.Replicate.class, this::onReplicate)
				.onMessage(Protocal.Replicated.class, this::onReplicated)
				.onMessage(Protocal.ReplicateTimeout.class, this::onReplicateTimeout)
				.onMessage(Protocal.Command.class, this::onCommand)
				.onMessage(Protocal.Connect.class, this::onConnect)
				.onMessage(Protocal.Snapshot.class, this::onSnapshot)
				.onMessage(Protocal.Stop.class, message -> Behaviors.stopped())
				.onAnyMessage(message -> Behaviors.stopped())
				.build();
	}

	@Override
	public Receive<OpBaseProtocal> createReceive() {
		return newReceiveBuilder().onMessage(OpBaseProtocal.class,this::onInit).build();
	}
	

}
