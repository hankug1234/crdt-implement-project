package com.crdt.implement.opBaseCrdt.RGA;

import lombok.Getter;

public class Data {
	
	@Getter
	public static class Inserted<A> implements RgaData<A>{
		private RgaVPtr after;
		private RgaVPtr at;
		private A value;
		
		public Inserted(RgaVPtr after, RgaVPtr at, A value) {
			this.after = after; this.at = at; this.value = value;
		}
	}
	
	@Getter
	public static class Removed<A> implements RgaData<A>{
		private RgaVPtr at;
		public Removed(RgaVPtr at) {
			this.at = at;
		}
	}
}
