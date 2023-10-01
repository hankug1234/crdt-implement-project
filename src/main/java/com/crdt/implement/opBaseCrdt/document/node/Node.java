package com.crdt.implement.opBaseCrdt.document.node;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.crdt.implement.opBaseCrdt.document.Id;
import com.crdt.implement.opBaseCrdt.document.Operation;
import com.crdt.implement.opBaseCrdt.document.cursor.Cursor;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;
import com.crdt.implement.opBaseCrdt.document.typetag.TypeTag;
import com.crdt.implement.opBaseCrdt.document.values.LeafVal;

public interface Node {
	
	public Optional<Set<Id>> clear(Id opId, Key key);
	public Optional<Set<String>> keys(Cursor cur);
	public Optional<List<LeafVal>> values(Cursor cur);
	public Optional<Node> applyOp(Operation op, List<Operation> concurrentOps);
	public Optional<Cursor> next(Cursor cur);
}
