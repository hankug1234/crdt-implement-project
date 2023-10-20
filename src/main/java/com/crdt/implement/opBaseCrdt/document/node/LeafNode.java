package com.crdt.implement.opBaseCrdt.document.node;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.crdt.implement.opBaseCrdt.document.Id;
import com.crdt.implement.opBaseCrdt.document.Operation;
import com.crdt.implement.opBaseCrdt.document.cursor.Cursor;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;
import com.crdt.implement.opBaseCrdt.document.node.ordering.Block;
import com.crdt.implement.opBaseCrdt.document.node.ordering.BlockMetaData;
import com.crdt.implement.opBaseCrdt.document.node.ordering.MoveMetaData;
import com.crdt.implement.opBaseCrdt.document.values.LeafVal;
import com.crdt.implement.vectorClock.Ord;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LeafNode implements Node{
	
	
	@Override
	public Optional<Set<Id>> clear(Id opId) {
		return Optional.empty();
	}

	@Override
	public Optional<Node> applyOp(Operation op) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}
	
	@Override
	public Node clone() {
		return null;
	}

	@Override
	public Object toJson() {
		return null;
	}

	@Override
	public Optional<Node> query(Cursor cur) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<BlockMetaData> getInsertMetaData(Cursor cur, int index) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<MoveMetaData> getMoveMetaData(Cursor cur, int src, int dst) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}
}
