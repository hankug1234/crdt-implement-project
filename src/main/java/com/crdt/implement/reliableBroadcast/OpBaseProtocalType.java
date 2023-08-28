package com.crdt.implement.reliableBroadcast;

public enum OpBaseProtocalType{
	Connect("connect"), 
	Replicate("replicate"), 
	ReplicateTimeout("timeout"),
	Replicated("replicated"),
	Query("query"),
	Command("command"),
	Loaded("loaded"),
	Snapshot("snapshat");
	
	private final String type;
	
	OpBaseProtocalType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}
}
