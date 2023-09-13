package com.crdt.implement.opBaseCrdt.LSeq;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LSeqVPtr implements Comparable<LSeqVPtr>{
	private List<Byte> sequence;
	private String replicaId;
	
	
	public List<Byte> getSequence(){
		return this.sequence;
	}
	
	public String getReplicaId() {
		return this.replicaId;
	}
	
	public Byte getSeq(int index) {
		return this.sequence.get(index);
	}
	
	public int getSeqSize() {
		return sequence.size();
	}
	
	public LSeqVPtr(String replicaId,List<Byte> sequence) {
		this.replicaId = replicaId;
		this.sequence = sequence;
	}
	
	@Override
	public int compareTo(LSeqVPtr other) {
		int len = Integer.min(this.getSeqSize(),other.getSeqSize());
		int i = 0; int cmp = 0;
		while(cmp == 0 && i < len) {
			cmp = this.getSeq(i).compareTo(other.getSeq(i));
			i+=1;
		}
		
		if(cmp == 0) {
			cmp = this.getSeqSize() - other.getSeqSize();
			if(cmp == 0) {
				cmp = this.getReplicaId().compareTo(other.getReplicaId());
			}
		}
		
		return cmp;
	}
	
	@Override 
	public String toString() {
		return String.join(".", sequence.stream().map(b->(char)b.byteValue()).toArray(null)) + ":" + replicaId;
	}
}
