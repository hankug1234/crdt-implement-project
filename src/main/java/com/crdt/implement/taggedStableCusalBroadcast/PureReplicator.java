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
	
	public PureReplicator(ActorContext<PureOpBaseProtocal> context ,StashBuffer<PureOpBaseProtocal> buffer, PureCrdtOperation<S,Q,O> crdt
			,String replicaId,PureReplicationState<S,O> state, Duration timeoutInterval) {
		super(context);
		this.timeoutInterval = timeoutInterval;
		this.state = state;
		this.replicaId = replicaId;
		this.buffer = buffer;
		this.crdt = crdt;
	}

	public<S,Q,O> Behavior<PureOpBaseProtocal> create(PureCrdtOperation<S,Q,O> crdt,String replicaId, PureReplicationState<S,O> state, Duration timeoutInterval){
		return Behaviors.withStash(100, stash ->{
			return Behaviors.setup(ctx-> new PureReplicator<S,Q,O>(ctx,stash,crdt,replicaId,state,timeoutInterval));
		});
	}
	
	private void refreshTimeout(String replicatedNodeId) {
		Object timerKey = this.state.getConnections().get(replicatedNodeId).getTimeout();
		this.timer.cancel(timerKey);
		this.timer.startTimerAtFixedRate(timerKey,new Protocal.ReplicateTimeout(replicatedNodeId),this.timeoutInterval);
	}
	
	private StablelizeResult stableize() {
		VectorClock stableTimeStamp = this.state.getObserved().min();
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
		
		getContext().getLog().info("get Query protocal from {}",query.getReplyTo().toString());
		
		Protocal.Res<Q> res = new Protocal.Res<>(crdt.Query(this.state.getStable(),this.state.getUnstables()
				.stream().map(e->e.getOperation()).collect(Collectors.toSet())));
		query.getReplyTo().tell(res);
		
		return Behaviors.same();
	}
	
	private Behavior<PureOpBaseProtocal> onConnect(Protocal.Connect connect){
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
		
		this.timer.startTimerAtFixedRate(timerKey,new Protocal.ReplicateTimeout(connect.getReplicaId()),this.timeoutInterval);
		
		return Behaviors.same();
	}
	
	private Behavior<PureOpBaseProtocal> onReplicate(Protocal.Replicate replicate){
		
		final VectorClock evicted = state.getEvicted().getVectorClock(replicaId);
		
		// if require operations at some node evicted time then requiring node ins't know that node evicted so send evicted protocal 
		if(evicted.compareTo(replicate.getVectorClock()) == Ord.Cc.getValue()) {
			getContext().getLog().info("get evicted node protocal from {}",replicate.getReplicaId());
			this.evictedBroadCast(replicate.getReplicaId(), evicted);
			return Behaviors.same();
		}else if(replicate.getVectorClock().compareTo(this.state.getStableVectorClock()) == Ord.Lt.getValue()) {
			getContext().getLog().info("send reset protocal to {}",replicate.getReplicaId());
			replicate.getReplyTo().getEndPoint().tell(new Protocal.Reset<>(this.replicaId, this.toSnapshot()));
		}
		
		getContext().getLog().info("operation replicate to {}",replicate.getReplicaId());
		Set<PureOpBaseEvent<O>> unstables = this.state.getUnstables();
		Set<PureOpBaseEvent<O>> filteredOperations = unstables.stream()
		.filter(e -> e.getVectorClock().compareTo(replicate.getVectorClock()) > Ord.Eq.getValue()).collect(Collectors.toSet());
		
		replicate.getReplyTo().getEndPoint().tell(new Protocal.Replicated<>(this.replicaId, filteredOperations));
		
		return Behaviors.same();
	}
	
	private Behavior<PureOpBaseProtocal> onRest(Protocal.Reset<S,O> reset){
		
		getContext().getLog().info("get reset protocal from {}",reset.getFrom());
		
		PureReplicationState<S,O> resetState = reset.getSnapshot();
		
		if(this.state.getLastestVectorClock().compareTo(resetState.getStableVectorClock()) == Ord.Lt.getValue()) {
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
		
		if(replicated.getOperations().isEmpty()) {
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
						.filter(e2 -> !this.crdt.Obsoletes(e1.getOperation(), e2.getOperation())).collect(Collectors.toSet());
				pruned.add(e1);
				
				state.setUnstables(pruned);
				state.getLastestVectorClock().merge(e1.getVectorClock());
			}
			
			StablelizeResult result = this.stableize();
			
			if(!result.getStableOperations().isEmpty()) {
				getContext().getLog().info("{} : save stable state",replicaId);
				S newStable = this.crdt.Apply(this.state.getStable(),result.getStableOperations()
						.stream().map(e->e.getOperation()).collect(Collectors.toSet()));
				
				this.state.setStable(newStable);
				this.state.setStableVectorClock(result.getStableTimeStamp());
				this.state.setUnstables(result.getUnStableOperations());
			}
			this.refreshTimeout(replicated.getFrom());
		}
		
		return Behaviors.same();
	}
	
	private Behavior<PureOpBaseProtocal> onReplicateTimeout(Protocal.ReplicateTimeout timeout){
		getContext().getLog().info("{} timeout",timeout.getReplicaId());
		PureReplicationStatus status = this.state.getConnections().get(timeout.getReplicaId());
		status.getEndPoint().getEndPoint().tell(new Protocal.Replicate(replicaId,new EndPoint(getContext().getSelf()), this.state.getLastestVectorClock()));
		
		return Behaviors.same();
	}
	
	private Behavior<PureOpBaseProtocal> onSubmit(Protocal.Submit<O> submit){
		this.state.getLastestVectorClock().inc(replicaId);
		PureOpBaseEvent<O> operation = new PureOpBaseEvent<>(this.replicaId,this.state.getLastestVectorClock().clone(),submit.getOperation());
		this.state.getObserved().update(replicaId, this.state.getLastestVectorClock());
		
		Set<PureOpBaseEvent<O>> pruned = this.state.getUnstables().stream()
				.filter(e->!crdt.Obsoletes(e.getOperation(), submit.getOperation())).collect(Collectors.toSet());
		
		pruned.add(operation);
		
		this.state.setUnstables(pruned);
		
		submit.getReplyTo().tell(new Protocal.Res<Q>(this.crdt.Query(this.state.getStable(), pruned
				.stream().map(e->e.getOperation()).collect(Collectors.toSet()))));
		
		return Behaviors.same();
	}
	
	private Behavior<PureOpBaseProtocal> onEvict(Protocal.Evict evict){
		
		getContext().getLog().info("evict {}",evict.getReplicaId());
		
		this.state.getLastestVectorClock().inc(replicaId);
		this.evictedBroadCast(evict.getReplicaId(), this.state.getLastestVectorClock());
		this.terminateConnection(evict.getReplicaId());
		this.state.getEvicted().update(evict.getReplicaId(), this.state.getLastestVectorClock());
		this.state.getObserved().removeVectorClock(evict.getReplicaId());
		this.state.getObserved().update(replicaId, this.state.getLastestVectorClock());
		
		return Behaviors.same();
	}
	
	private Behavior<PureOpBaseProtocal> onEvicted(Protocal.Evicted evicted){
		
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
	
	private Behavior<PureOpBaseProtocal> timerSettingActive(PureOpBaseProtocal protocal){
		return Behaviors.withTimers(timer -> {
			return active(protocal,timer);
		});
	}
	
	private Behavior<PureOpBaseProtocal> active(PureOpBaseProtocal protocal, TimerScheduler<PureOpBaseProtocal> timer){
		this.timer = timer;
		return Behaviors
				.receive(PureOpBaseProtocal.class)
				.onMessage(Protocal.Query.class, this::onQuery)
				.onMessage(Protocal.Connect.class, this::onConnect)
				.onMessage(Protocal.Replicate.class, this::onReplicate)
				.onMessage(Protocal.Replicated.class, this::onReplicated)
				.onMessage(Protocal.ReplicateTimeout.class, this::onReplicateTimeout)
				.onMessage(Protocal.Submit.class, this::onSubmit)
				.onMessage(Protocal.Evict.class, this::onEvict)
				.onMessage(Protocal.Evicted.class, this::onEvicted)
				.onAnyMessage(e -> Behaviors.stopped()).build();
	}
	
	@Override
	public Receive<PureOpBaseProtocal> createReceive() {
		// TODO Auto-generated method stub
		return newReceiveBuilder().onMessage(PureOpBaseProtocal.class, this::timerSettingActive).build();
	}
	
	
}
