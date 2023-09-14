package com.crdt.implement.opBaseCrdt.RGA;

import lombok.Getter;

@Getter
public class RgaVPtr {

	private int index;
	private String replicaId;
	
	public RgaVPtr(String replicaId, int index) {
		this.replicaId = replicaId; this.index = index;
	}
	
	public int compareTo(RgaVPtr other) {
		int cmp = Integer.compare(this.index, other.getIndex()); 
		if(cmp == 0) {
			cmp = other.getReplicaId().compareTo(this.replicaId);
			
		}
		return cmp;
	}
	
	public boolean equals(RgaVPtr other) {
		if(this.index == other.getIndex() && this.replicaId.equals(other.getReplicaId())) {
			return true;
		}
		return false;
	}
}
