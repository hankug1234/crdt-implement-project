package com.crdt.implement.opBaseCrdt.document.values;

import java.util.Optional;

import com.crdt.implement.opBaseCrdt.document.Document;
import com.crdt.implement.opBaseCrdt.document.Operation;
import com.crdt.implement.opBaseCrdt.document.command.Command;
import com.crdt.implement.opBaseCrdt.document.signal.Signal;
import com.crdt.implement.reliableBroadcast.OpBaseEvent;


public class ObjectVal extends LeafVal{
	
	public Optional<Object> prepare(Object behavior) {
		return Optional.empty();
	}
	
	public void effect(Object data) {
		
	}
}
