package com.crdt.implement.taggedStableCusalBroadcast;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.crdt.implement.PureOpBaseCrdt.PureCrdtOperation;
import com.crdt.implement.PureOpBaseCrdt.PureReplicationState;
import com.crdt.implement.PureOpBaseCrdt.PureReplicationStatus;
import com.crdt.implement.vectorClock.Ord;
import com.crdt.implement.vectorClock.VectorClock;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.StashBuffer;
import akka.actor.typed.javadsl.TimerScheduler;
import lombok.AllArgsConstructor;
import lombok.Getter;


public class PureReplicator<S,Q,O> extends AbstractBehavior<PureOpBaseProtocal>{
	private Duration timeoutInterval;
	private PureReplicationState<S,O> state;
	private String replicaId;
	private PureCrdtOperation<S,Q,O> crdt;
	private final StashBuffer<PureOpBaseProtocal> buffer;
	private TimerScheduler<PureOpBaseProtocal> timer;
	
	@Getter
	@AllArgsConstructor
	private class StablelizeResult{
		public VectorClock stableTimeStamp;
		public Set<PureOpBaseEvent<O>> stableOperations;
		public Set<PureOpBaseEvent<O>> unStableOperations;
	}
	
	public PureReplicator(ActorContext<PureOpBaseProtocal> context ,StashBuffer<PureOpBaseProtocal> buffer, PureCrdtOperation<S,Q,O> crdt,
			TimerScheduler<PureOpBaseProtocal> timer,String replicaId,PureReplicationState<S,O> state, Duration timeoutInterval) {
		super(context);
		this.timeoutInterval = timeoutInterval;
		this.state = state;
		this.replicaId = replicaId;
		this.buffer = buffer;
		this.crdt = crdt;
		this.timer = timer;
	}

	public static <S,Q,O> Behavior<PureOpBaseProtocal> create(PureCrdtOperation<S,Q,O> crdt,String replicaId, PureReplicationState<S,O> state, Duration timeoutInterval){
		return Behaviors.withStash(100, stash ->{
			return Behaviors.setup(ctx-> Behaviors.withTimers(timer -> new PureReplicator<S,Q,O>(ctx,stash,crdt,timer,replicaId,state,timeoutInterval)) );
		});
	}
	
	private void refreshTimeout(String replicatedNodeId) {
		Object timerKey = this.state.getConnections().get(replicatedNodeId).getTimeout();
		this.timer.cancel(timerKey);
		this.timer.startTimerAtFixedRate(timerKey,new Protocal.ReplicateTimeout(replicatedNodeId),this.timeoutInterval);
	}
	
	private StablelizeResult stableize() {
		VectorClock stableTimeStamp = this.state.getObserved().min();
		
		getContext().getLog().info("{} : {} ",replicaId,this.state.getObserved().toString());
		getContext().getLog().info("{} :observed size {} stableTimeStamp {}",replicaId,this.state.getObserved().getVectorClocks().size(),stableTimeStamp.toString());
		Map<Boolean,List<PureOpBaseEvent<O>>> partition = this.state.getUnstables()
				.stream().collect(Collectors.partitioningBy(e -> e.getVectorClock().compareTo(stableTimeStamp) > Ord.Eq.getValue()));
		
		List<PureOpBaseEvent<O>> unstables = partition.getOrDefault(true, new ArrayList<>());
		List<PureOpBaseEvent<O>> stables = partition.getOrDefault(false, new ArrayList<>());
		
		return new StablelizeResult(stableTimeStamp,new HashSet<>(stables),new HashSet<>(unstables));
	}
	
	private PureReplicationState<S,O> toSnapshot(){
		return this.state.clone(this.crdt::Copy);
	}
	
	private void evictedBroadCast(String replicaId,VectorClock vectorClock) {
		for(Entry<String,PureReplicationStatus> entry : this.state.getConnections().entrySet()) {
			entry.getValue().getEndPoint().getEndPoint().tell(new Protocal.Evicted(replicaId, vectorClock));
		}
	}
	
	private void terminateConnection(String replicatedNodeId) {
		PureReplicationStatus status = this.state.getConnections().get(replicatedNodeId);
		if(status == null) return;
		this.timer.cancel(status.getTimeout());
		this.state.getConnections().remove(replicatedNodeId);
	}
	
	private Behavior<PureOpBaseProtocal> onQuery(Protocal.Query query){
		
		getContext().getLog().info("{} get Query protocal from {} : unstable Size : {} stable value : {}"
				,replicaId,query.getReplyTo().toString(),this.state.getUnstables().size(),this.state.getStable());
		
		
		Protocal.Res<Q> res = new Protocal.Res<>(crdt.Query(this.state.getStable(),this.state.getUnstables()));
		query.getReplyTo().tell(res);
		
		return Behaviors.same();
	}
	
	private Behavior<PureOpBaseProtocal> onConnect(Protocal.Connect connect){
		
		getContext().getLog().info("get connect protocal to {}",connect.getReplicaId());
		
		PureReplicationStatus status = this.state.getConnections().get(connect.getReplicaId());
		Object timerKey;
		if(status == null) {
			timerKey = new Object();
			this.state.getConnections().put(String.valueOf(connect.getReplicaId()),new PureReplicationStatus(connect.getEndPoint(),timerKey));
		}
		else {
			timerKey = status.getTimeout();
			timer.cancel(timerKey);
		}
		
		getContext().getLog().info("get connect protocal connect {} -> {}",this.replicaId,connect.getReplicaId());
		
		this.state.getObserved().update(connect.getReplicaId(),new VectorClock());
		
		this.timer.startTimerAtFixedRate(timerKey,new Protocal.ReplicateTimeout(connect.getReplicaId()),this.timeoutInterval);
		
		return Behaviors.same();
	}
	
	private Behavior<PureOpBaseProtocal> onReplicate(Protocal.Replicate replicate){
		
		
		final VectorClock evicted = state.getEvicted().getVectorClock(replicate.getReplicaId());
		final int compareResult = replicate.getVectorClock().compareTo(this.state.getStableVectorClock());
		this.state.getObserved().update(replicate.getReplicaId(), replicate.getVectorClock());
		
		// if require operations at some node evicted time then requiring node ins't know that node evicted so send evicted protocal 
		if(evicted.compareTo(replicate.getVectorClock()) == Ord.Cc.getValue()) {
			getContext().getLog().info("{} : get evicted node protocal from {}",replicaId,replicate.getReplicaId());
			this.evictedBroadCast(replicate.getReplicaId(), evicted);
			return Behaviors.same();
		}else if(compareResult == Ord.Lt.getValue()) {
			getContext().getLog().info("{} : send reset protocal to {}", replicaId,replicate.getReplicaId());
			
			replicate.getReplyTo().getEndPoint().tell(new Protocal.Reset<>(this.replicaId, this.toSnapshot()));
		}
		
		Set<PureOpBaseEvent<O>> filteredOperations = this.state.getUnstables().stream()
		.filter(e -> e.getVectorClock().compareTo(replicate.getVectorClock()) > Ord.Eq.getValue()).collect(Collectors.toSet());
		
		getContext().getLog().info("{} : operation replicated {} to {} ",replicaId,filteredOperations.size(),replicate.getReplicaId());
		
		replicate.getReplyTo().getEndPoint().tell(new Protocal.Replicated<>(this.replicaId, filteredOperations));
		
		return Behaviors.same();
	}
	
	private Behavior<PureOpBaseProtocal> onReset(Protocal.Reset<S,O> reset){
		
		getContext().getLog().info("{} : get reset protocal from {}",replicaId,reset.getFrom());
		
		PureReplicationState<S,O> resetState = reset.getSnapshot();
		final int compareResult = this.state.getLastestVectorClock().compareTo(resetState.getStableVectorClock());
		
		if(compareResult == Ord.Lt.getValue()) {
			getContext().getLog().info("{} : process reset protocal ",this.replicaId);
			this.state.setStable(resetState.getStable());
			this.state.setStableVectorClock(resetState.getStableVectorClock());
			this.state.getLastestVectorClock().merge(resetState.getStableVectorClock());
			this.state.getObserved().merge(resetState.getObserved());
			this.state.getEvicted().merge(resetState.getEvicted());
		}
		else {
			getContext().getLog().info("{} : lastest version didn't lt then current stable version",replicaId);
		}
		
		return Behaviors.same();
	}
	
	private Behavior<PureOpBaseProtocal> onReplicated(Protocal.Replicated<O> replicated){
		
		getContext().getLog().info("{} : get replicated from {} size {} on {}"
				,replicaId,replicated.getFrom(),replicated.getOperations().size(),this.state.getLastestVectorClock().toString());
		
		if(replicated.getOperations().isEmpty()) {
			
			StablelizeResult result = this.stableize();
			
			if(!result.getStableOperations().isEmpty()) {
				getContext().getLog().info("{} : save stable state , stable op size {} : ",replicaId,result.getStableOperations().size());
				S newStable = this.crdt.Apply(this.state.getStable(),result.getStableOperations());
				
				this.state.setStable(newStable);
				this.state.setStableVectorClock(result.getStableTimeStamp());
				this.state.setUnstables(result.getUnStableOperations());
			}
			
			this.refreshTimeout(replicated.getFrom());
		}
		else {
			VectorClock evictedVersion = this.state.getEvicted().getVectorClock(replicated.getFrom());
			
			Set<PureOpBaseEvent<O>> unstables = replicated.getOperations().stream()
					.filter(e-> e.getVectorClock().compareTo(this.state.getLastestVectorClock()) > Ord.Eq.getValue())
					.filter(e->!(e.getReplicaId().equals(replicated.getFrom()) && e.getVectorClock().compareTo(evictedVersion) == Ord.Cc.getValue()))
					.collect(Collectors.toSet());
			
			for(PureOpBaseEvent<O> e1 : unstables) {
				this.state.getObserved().update(replicated.getFrom(), e1.getVectorClock());
				
				Set<PureOpBaseEvent<O>> pruned = this.state.getUnstables().stream()
						.filter(e2 -> !this.crdt.Obsoletes(e1, e2)).collect(Collectors.toSet());
				pruned.add(e1);
				
				state.setUnstables(pruned);
				state.getLastestVectorClock().merge(e1.getVectorClock());
			}
			
			state.getObserved().update(replicaId, state.getLastestVectorClock());
			
			StablelizeResult result = this.stableize();
			
			if(!result.getStableOperations().isEmpty()) {
				getContext().getLog().info("{} : save stable state , stable op size {} : ",replicaId,result.getStableOperations().size());
				S newStable = this.crdt.Apply(this.state.getStable(),result.getStableOperations());
				
				this.state.setStable(newStable);
				this.state.setStableVectorClock(result.getStableTimeStamp());
				this.state.setUnstables(result.getUnStableOperations());
			}
			this.refreshTimeout(replicated.getFrom());
		}
		
		return Behaviors.same();
	}
	
	private Behavior<PureOpBaseProtocal> onReplicateTimeout(Protocal.ReplicateTimeout timeout){
		getContext().getLog().info("{} : {} timeout",replicaId,timeout.getReplicaId());
		PureReplicationStatus status = this.state.getConnections().get(timeout.getReplicaId());
		status.getEndPoint().getEndPoint().tell(new Protocal.Replicate(replicaId,new EndPoint(getContext().getSelf()), this.state.getLastestVectorClock()));
		
		return Behaviors.same();
	}
	
	private Behavior<PureOpBaseProtocal> onSubmit(Protocal.Submit<O> submit){
		
		this.state.getLastestVectorClock().inc(replicaId);
		
		getContext().getLog().info("{} : operation submitted on {}",replicaId,this.state.getLastestVectorClock().toString());
		
		PureOpBaseEvent<O> operation = new PureOpBaseEvent<>(this.replicaId,this.state.getLastestVectorClock().clone(),submit.getOperation());
		
		this.state.getObserved().update(replicaId, this.state.getLastestVectorClock());
		
		Set<PureOpBaseEvent<O>> pruned = this.state.getUnstables().stream()
				.filter(e->!crdt.Obsoletes(e, operation)).collect(Collectors.toSet());
		
		pruned.add(operation);
		
		this.state.setUnstables(pruned);
		
		submit.getReplyTo().tell(new Protocal.Res<Q>(this.crdt.Query(this.state.getStable(), pruned)));
		
		return Behaviors.same();
	}
	
	private Behavior<PureOpBaseProtocal> onEvict(Protocal.Evict evict){
		
		getContext().getLog().info("{} : evict {}",replicaId,evict.getReplicaId());
		
		if(!this.state.getConnections().keySet().contains(evict.getReplicaId())) {
			getContext().getLog().info("{} : no search replicaId {}",replicaId,evict.getReplicaId());
			return Behaviors.same();
		}
		
		this.state.getLastestVectorClock().inc(replicaId);
		this.evictedBroadCast(evict.getReplicaId(), this.state.getLastestVectorClock());
		
		if(evict.getReplicaId().equals(replicaId)) {
			
			//end procedure
			
			return Behaviors.stopped();
		}
		
		
		this.terminateConnection(evict.getReplicaId());
		this.state.getEvicted().update(evict.getReplicaId(), this.state.getLastestVectorClock());
		this.state.getObserved().removeVectorClock(evict.getReplicaId());
		this.state.getObserved().update(replicaId, this.state.getLastestVectorClock());
		
		return Behaviors.same();
	}
	
	private Behavior<PureOpBaseProtocal> onEvicted(Protocal.Evicted evicted){
		
		getContext().getLog().info("{} : get evicted protocal",replicaId);
		
		if(evicted.getReplicaId().equals(replicaId)) {
			
			//end procedure
			
			return Behaviors.stopped();
		}
		
		this.state.getEvicted().update(evicted.getReplicaId(), evicted.getVectorClock());
		this.terminateConnection(evicted.getReplicaId());
		
		Set<PureOpBaseEvent<O>> pruned = this.state.getUnstables().stream()
				.filter(e->!(e.getReplicaId().equals(evicted.getReplicaId()) && e.getVectorClock()
						.compareTo(this.state.getEvicted().getVectorClock(evicted.getReplicaId())) == Ord.Cc.getValue()))
				.collect(Collectors.toSet());
		
		this.state.setUnstables(pruned);
		this.state.getObserved().removeVectorClock(evicted.getReplicaId());
		
		return Behaviors.same();
	}
	
	private Behavior<PureOpBaseProtocal> onError(Object o){
		getContext().getLog().info("{} : error occure",replicaId);
		return Behaviors.stopped();
	}
	
	
	
	
	@Override
	public Receive<PureOpBaseProtocal> createReceive() {
		// TODO Auto-generated method stub
		return newReceiveBuilder()
				.onMessage(Protocal.Connect.class, this::onConnect)
				.onMessage(Protocal.Query.class, this::onQuery)
				.onMessage(Protocal.Replicate.class, this::onReplicate)
				.onMessage(Protocal.Replicated.class, this::onReplicated)
				.onMessage(Protocal.ReplicateTimeout.class, this::onReplicateTimeout)
				.onMessage(Protocal.Reset.class, this::onReset)
				.onMessage(Protocal.Submit.class, this::onSubmit)
				.onMessage(Protocal.Evict.class, this::onEvict)
				.onMessage(Protocal.Evicted.class, this::onEvicted)
				.onAnyMessage(this::onError).build();
	}
	
	
}
