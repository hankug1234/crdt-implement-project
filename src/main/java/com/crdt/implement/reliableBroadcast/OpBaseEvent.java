package com.crdt.implement.reliableBroadcast;

import com.crdt.implement.vectorClock.VectorClock;

import lombok.Getter;
@Getter
public class OpBaseEvent <T> {
	private final String originReplicaId; 
	private final long originSeqNr; 
	private final long localSeqNr;
	private final VectorClock vectorClock;
	private final T data;
	
	public OpBaseEvent(String originReplicaId, long originSeqNr, long localSeqNr, VectorClock vectorClock, T data) {
		this.originReplicaId = originReplicaId;
		this.originSeqNr = originSeqNr;
		this.localSeqNr = localSeqNr;
		this.vectorClock = vectorClock;
		this.data = data;
	}
}
