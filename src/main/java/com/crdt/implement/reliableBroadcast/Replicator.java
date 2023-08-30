package com.crdt.implement.reliableBroadcast;

import java.time.Duration;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

import com.crdt.implement.opBaseCrdt.OpBaseCrdtOperation;
import com.crdt.implement.opBaseCrdt.ReplicationState;
import com.crdt.implement.persistence.OpBaseCrdtDB;

import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy.Stop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.StashBuffer;
import akka.actor.typed.javadsl.TimerScheduler;


public class Replicator<S,Q,C,E> extends AbstractBehavior{
	
	private final StashBuffer<OpBaseProtocal> buffer;
	private OpBaseCrdtDB<S,E> db;
	private OpBaseCrdtOperation<S,Q,C,E> crdt;
	private String replicaId;
	private Object snapshotTimerKey;
	
	public Replicator(ActorContext context,StashBuffer<OpBaseProtocal> buffer,OpBaseCrdtDB db,String replicaId
			,OpBaseCrdtOperation<S,Q,C,E> crdt) {
		super(context);
		this.buffer = buffer;
		this.db = db;
		this.crdt = crdt;
		this.replicaId = replicaId;
		this.snapshotTimerKey = new Object();
	}
	
	public static <S,Q,C,E>  Behavior<OpBaseProtocal> create(OpBaseCrdtDB<S,E> db,String replicaId,OpBaseCrdtOperation<S,Q,C,E> crdt){
		return Behaviors.withStash(100, stash->{
			return Behaviors.setup(ctx->{
				
				CompletionStage<ReplicationState<S>> latestState =  db.LoadSnapshot(replicaId).thenCompose((Optional<ReplicationState<S>> option)->{
					final ReplicationState<S> state = option.orElseGet(()->new ReplicationState<S>(replicaId,crdt.Default()));
					
					CompletableFuture<List<OpBaseEvent<E>>> events = db.LoadEvents(replicaId, state.getSeqNr()+1L);
					
					return events.thenCompose((List<OpBaseEvent<E>> list) -> {
						return CompletableFuture.supplyAsync(()->{
							for(OpBaseEvent<E> event : list) {
								state.setCrdt(crdt.Effect(state.getCrdt(), event.getData()));
								state.setSeqNr(event.getLocalSeqNr());
								state.getVectorClock().merge(event.getVectorClock());
								state.getObserved().put(event.getOriginReplicaId(),event.getOriginSeqNr());
							}
							return state;
						});
					});
				});
				
				ctx.pipeToSelf(latestState, (value,cause)->{
					if(cause == null) {
						return new Protocal.Loaded<S>(value);
					}else {
						return new Protocal.Stop();
					}
				});
				
				return new Replicator<S,Q,C,E>(ctx,stash,db,replicaId,crdt);
			});
		});
	}
	
	private Behavior<OpBaseProtocal> onInit(Object protocal){
		if(protocal instanceof Protocal.Loaded) {
			return buffer.unstashAll(timerSettingActive((OpBaseProtocal)protocal));
		}else if(protocal instanceof Protocal.Stop) {
			return Behaviors.stopped();
		}
		else {
			buffer.stash((OpBaseProtocal)protocal);
			return Behaviors.same();
		}
	}
	
	private Behavior<OpBaseProtocal> onQuery(Protocal.Query query){
		return null;
	}
	
	private Behavior<OpBaseProtocal> onReplicate(Protocal.Replicate replicate){
		return null;
	}
	
	private Behavior<OpBaseProtocal> onReplicated(Protocal.Replicated<E> replicated){
		return null;
	}
	
	private Behavior<OpBaseProtocal> onReplicateTimeout(Protocal.ReplicateTimeout timeout){
		return null;
	}
	
	private Behavior<OpBaseProtocal> onCommand(Protocal.Command<C> command){
		return null;
	}
	
	private Behavior<OpBaseProtocal> onConnect(Protocal.Connect connect){
		return null;
	}
	
	private Behavior<OpBaseProtocal> onSnapshot(Protocal.Snapshot snapshot){
		return null;
	}
	
	private Behavior<OpBaseProtocal> timerSettingActive(OpBaseProtocal protocal){
		return Behaviors.withTimers(timer -> {
			timer.startTimerAtFixedRate(snapshotTimerKey,new Protocal.Snapshot(),Duration.ofSeconds(5));
			return active(protocal,timer);
		});
	}
	
	private Behavior<OpBaseProtocal> active(OpBaseProtocal protocal,TimerScheduler<OpBaseProtocal> timer){
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
