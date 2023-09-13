package com.crdt.implement.opBaseCrdt.LSeq;

import lombok.Getter;

public class Command {
	
	@Getter
	public static class Insert<A> implements LSeqCommand<A>{
		
		private int index; private A value; private String replicaId;
		
		public Insert(int index, A value, String replicaId) {
			this.index = index; this.value = value; this.replicaId = replicaId;
		}
	}
	
	@Getter
	public static class RemoveAt<A> implements LSeqCommand<A>{
		private int index;
		
		public RemoveAt(int index) {
			this.index = index;
		}
	}
	
	
}
