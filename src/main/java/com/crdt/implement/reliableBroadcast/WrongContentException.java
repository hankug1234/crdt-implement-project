package com.crdt.implement.reliableBroadcast;

public class WrongContentException extends RuntimeException{
	public WrongContentException() {
		super();
	}
	
	public WrongContentException(String message) {
		super(message);
	}

}
