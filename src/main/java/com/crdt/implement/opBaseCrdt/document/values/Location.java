package com.crdt.implement.opBaseCrdt.document.values;

public enum Location {
	
	before(-1),after(1);
	
	private int value;
	
	Location(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
}
