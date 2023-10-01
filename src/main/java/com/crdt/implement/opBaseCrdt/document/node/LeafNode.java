package com.crdt.implement.opBaseCrdt.document.node;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.crdt.implement.opBaseCrdt.document.Id;
import com.crdt.implement.opBaseCrdt.document.Operation;
import com.crdt.implement.opBaseCrdt.document.cursor.Cursor;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;
import com.crdt.implement.opBaseCrdt.document.values.LeafVal;
import com.crdt.implement.vectorClock.Ord;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LeafNode implements Node{
	private Map<Id,LeafVal> values;

	public List<LeafVal> getValues(){
		return values.entrySet().stream().map(e->e.getValue()).toList();
	}
	
	public void setRegValue(Id id,LeafVal value) {
		this.values.put(id, value);
	}
	
	@Override
	public Optional<Set<Id>> clear(Id opId, Key key) {
		// TODO Auto-generated method stub
		Map<Id,LeafVal> concurrent = values.entrySet().stream()
				.filter(e->e.getKey().getVectorClock().compareTo(opId.getVectorClock())>=Ord.Eq.getValue())
				.collect(Collectors.toMap(e->e.getKey(), e->e.getValue()));
		this.values = concurrent;
		return Optional.of(concurrent.keySet());
	}

	@Override
	public Optional<Set<String>> keys(Cursor cur) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<List<LeafVal>> values(Cursor cur) {
		// TODO Auto-generated method stub
		return Optional.of(this.getValues());
	}

	@Override
	public Optional<Node> applyOp(Operation op, List<Operation> concurrentOps) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<Cursor> next(Cursor cur) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}
	
}
