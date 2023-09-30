package com.crdt.implement.opBaseCrdt.document.signal;

public enum LocationInstructor {
	Before(0),After(1);
	private final int value;
	
	LocationInstructor(int value) {
		this.value = value;
	}
	
}
