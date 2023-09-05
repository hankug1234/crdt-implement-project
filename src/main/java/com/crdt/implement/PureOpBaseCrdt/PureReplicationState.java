package com.crdt.implement.PureOpBaseCrdt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.crdt.implement.opBaseCrdt.ReplicationState;
import com.crdt.implement.taggedStableCusalBroadcast.PureOpBaseEvent;
import com.crdt.implement.vectorClock.MetrixTime;
import com.crdt.implement.vectorClock.VectorClock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PureReplicationState<S,O> {
	
	private String replicaId;
	private S stable;
	private Set<PureOpBaseEvent<O>> unstables;
	private VectorClock stableVectorClock;
	private VectorClock lastestVectorClock;
	private MetrixTime observed;
	private MetrixTime evicted;
	private Map<String,PureReplicationStatus> connections;
	
	public PureReplicationState(String replicaId,S stable) {
		this.replicaId = replicaId;
		this.stable = stable;
		this.unstables = new HashSet<>();
		this.stableVectorClock = new VectorClock();
		this.lastestVectorClock = new VectorClock();
		this.observed = new MetrixTime();
		this.evicted = new MetrixTime();
		connections = new HashMap<>();
	}
	
	public PureReplicationState<S,O> clone(Function<S,S> copy){
		String newReplicaId = String.valueOf(replicaId);
		S newStable = copy.apply(this.stable);
		Set<PureOpBaseEvent<O>> newUnstable = new HashSet<>(); newUnstable.addAll(this.unstables);
		VectorClock newStableVectorClock = this.stableVectorClock.clone();
		VectorClock newLastestVectorClock = this.lastestVectorClock.clone();
		MetrixTime newObserved = this.observed.clone();
		MetrixTime newEmited = this.evicted.clone();
		Map<String,PureReplicationStatus> newConnections = new HashMap<>();
		
		return new PureReplicationState<S,O>(newReplicaId,newStable,newUnstable,newStableVectorClock,newLastestVectorClock,newObserved,newEmited,newConnections);
	}
	
}
