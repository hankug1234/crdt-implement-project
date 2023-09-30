package com.crdt.implement.opBaseCrdt.document;

import com.crdt.implement.vectorClock.VectorClock;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Id {

	private String replicaId;
	private VectorClock vectorClock;
	
	public int compareTo(Id other) {
		int cmp = this.vectorClock.compareTo(other.getVectorClock());
		if(cmp == 0) {
			cmp = other.getReplicaId().compareTo(this.replicaId);
		}
		return cmp;
	}
}
