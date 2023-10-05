package com.crdt.implement.opBaseCrdt.document.node;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.crdt.implement.opBaseCrdt.document.Id;
import com.crdt.implement.opBaseCrdt.document.Operation;

public interface Node {
	
	public Optional<Set<Id>> clear(Id opId);
	public Optional<Node> applyOp(Operation op);
	public Node clone();
}
