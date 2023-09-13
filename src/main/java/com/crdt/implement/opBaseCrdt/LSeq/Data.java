package com.crdt.implement.opBaseCrdt.LSeq;

import lombok.Getter;

public class Data {

	@Getter
	public static class Inserted<A> implements LSeqData<A>{
		private LSeqVPtr vptr; private A value;
		
		public Inserted(LSeqVPtr vptr, A value) {
			this.vptr = vptr; this.value = value;
		}
		
	}
	
	@Getter
	public static class Removed<A> implements LSeqData<A>{
		private LSeqVPtr vptr; 
		
		public Removed(LSeqVPtr vptr) {
			this.vptr = vptr;
		}
		
	}
	
	
	public static class OutofBound<A> implements LSeqData<A>{
		
	}
}
