package com.crdt.implement.opBaseCrdt.RGA;

import lombok.Getter;

public class Command {

	@Getter
	public static class Insert<A> implements RgaCommand<A>{
		private A value; 
		private int index;
		
		public Insert(int index, A value) {
			this.index = index; this.value = value;
		}
	}
	
	@Getter
	public static class RemoveAt<A> implements RgaCommand<A>{
		private int index;
		
		public RemoveAt(int index) {
			this.index = index;
		}
	}
}
