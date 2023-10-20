package com.crdt.implement.reliableBroadcast;

import com.crdt.implement.vectorClock.VectorClock;

import lombok.Getter;
@Getter
public class OpBaseEvent <E> {
	private final String originReplicaId; 
	private final long originSeqNr; 
	private final long localSeqNr;
	private final VectorClock vectorClock;
	private final E data;
	
	public OpBaseEvent(String originReplicaId, long originSeqNr, long localSeqNr, VectorClock vectorClock, E data) {
		this.originReplicaId = originReplicaId;
		this.originSeqNr = originSeqNr;
		this.localSeqNr = localSeqNr;
		this.vectorClock = vectorClock;
		this.data = data;
	}
	
	public OpBaseEvent(E data) {
		this.originReplicaId = null;
		this.originSeqNr = 0;
		this.localSeqNr = 0;
		this.vectorClock = null;
		this.data = data;
	}
}
