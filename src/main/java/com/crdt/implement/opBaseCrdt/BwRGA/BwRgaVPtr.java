package com.crdt.implement.opBaseCrdt.BwRGA;

import lombok.Getter;

@Getter
public class BwRgaVPtr {
	private int index;
	private String replicaId;
	
	public BwRgaVPtr(int index,String replicaId) {
		this.index = index; 
		this.replicaId = replicaId;
	}
	
	public int compareTo(BwRgaVPtr other) {
		int cmp = Integer.compare(this.index, other.getIndex());
		if(cmp == 0) {
			return this.replicaId.compareTo(other.getReplicaId());
		}
		return cmp;
	}
	
	public boolean equals(BwRgaVPtr other) {
		if(other.getReplicaId().equals(this.replicaId) && this.index == other.getIndex()) {
			return true;
		}
		return false;
	}
}
