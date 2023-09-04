package com.crdt.implement.taggedStableCusalBroadcast;

import java.sql.Timestamp;

import com.crdt.implement.vectorClock.Ord;
import com.crdt.implement.vectorClock.VectorClock;

import lombok.Getter;

@Getter
public class PureOpBaseEvent<O> implements Comparable<PureOpBaseEvent<O>>{

	private final VectorClock vectorClock;
	private final Timestamp timestamp;
	private final String replicaId;
	private final O operation;
	
	public PureOpBaseEvent(String replicaId,VectorClock vectorClock,O operation) {
		this.timestamp = new Timestamp(System.currentTimeMillis());
		this.vectorClock = vectorClock.clone();
		this.replicaId = replicaId;
		this.operation = operation;
	}
	
	@Override
	public int compareTo(PureOpBaseEvent<O> otherEvent) {
		
		int cmp = this.vectorClock.compareTo(otherEvent.getVectorClock());
		
		if(cmp == Ord.Cc.getValue()) {
			
			cmp = this.timestamp.compareTo(otherEvent.getTimestamp());
			
			if(cmp == Ord.Eq.getValue()) {
				
				return this.replicaId.compareTo(otherEvent.getReplicaId());
			}
		}
		
		return cmp;
	} 
}
