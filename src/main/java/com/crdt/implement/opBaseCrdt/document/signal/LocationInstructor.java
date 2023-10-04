package com.crdt.implement.opBaseCrdt.document.signal;

public enum LocationInstructor {
	Before(0),After(1);
	private int value;
	
	LocationInstructor(int value) {
		this.value = value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
}
