package com.crdt.implement.PureOpBaseCrdt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.crdt.implement.vectorClock.MetrixTime;
import com.crdt.implement.vectorClock.VectorClock;

import lombok.Data;

@Data
public class PureReplicationState<S,O> {
	
	private String replicaId;
	private S stable;
	private Set<O> unstables;
	private VectorClock stableVectorClock;
	private VectorClock lastestVectorClock;
	private MetrixTime observed;
	private MetrixTime emited;
	private Map<String,PureReplicationStatus> connections;
	
	public PureReplicationState(String replicaId,S stable) {
		this.replicaId = replicaId;
		this.stable = stable;
		this.unstables = new HashSet<>();
		this.stableVectorClock = new VectorClock();
		this.lastestVectorClock = new VectorClock();
		this.observed = new MetrixTime();
		this.emited = new MetrixTime();
		connections = new HashMap<>();
	}
	
}
