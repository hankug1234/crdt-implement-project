package com.crdt.implement.vectorClock;

public enum Ord {
	Lt(-1),
	Eq(0),
	Gt(1),
	Cc(2);
	
	private final int value;
	
	Ord(int value){
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
}
