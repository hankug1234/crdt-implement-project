package com.crdt.implement.opBaseCrdt.document.node;

import java.util.Optional;
import java.util.Set;

import org.json.JSONObject;

import com.crdt.implement.opBaseCrdt.document.Id;
import com.crdt.implement.opBaseCrdt.document.Operation;
import com.crdt.implement.opBaseCrdt.document.cursor.Cursor;
import com.crdt.implement.opBaseCrdt.document.node.ordering.Block;
import com.crdt.implement.opBaseCrdt.document.node.ordering.BlockMetaData;
import com.crdt.implement.opBaseCrdt.document.node.ordering.MoveMetaData;

public interface Node {
	
	public Optional<Set<Id>> clear(Id opId);
	public Optional<Node> applyOp(Operation op);
	public Optional<Node> query(Cursor cur);
	public Optional<BlockMetaData> getInsertMetaData(Cursor cur, int index);
	public Optional<MoveMetaData> getMoveMetaData(Cursor cur, int src, int dst);
	public Object toJson();
	public Node clone();
}
