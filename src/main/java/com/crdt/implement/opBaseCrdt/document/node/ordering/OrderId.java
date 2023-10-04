package com.crdt.implement.opBaseCrdt.document.node.ordering;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OrderId {
	private String replicaId;
	private long seq;
	
	
	public int compareTo(OrderId other) {
		int cmp = Long.compare(seq, other.getSeq());
		if(cmp == 0) {
			return replicaId.compareTo(other.getReplicaId());
		}
		return cmp;
	}
	
	public boolean equals(OrderId other) {
		if(this.replicaId.equals(other.getReplicaId()) && this.seq == other.getSeq()) {
			return true;
		}
		return false;
	}
}
