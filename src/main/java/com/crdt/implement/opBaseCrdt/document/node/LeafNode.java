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
	
	public LeafVal getValue() {
		Set<Id> ids = this.values.keySet();
		Iterator<Id> iter = ids.iterator();
		Id min = iter.next();
		while(iter.hasNext()) {
			Id next = iter.next();
			if(min.compareTo(next) > 0) {
				min = next;
			}
		}
		return values.get(min);
	}
	
	public Map<Id,LeafVal> getIdValueMap(){
		return this.values;
	}
	
	public void setRegValue(Id id,LeafVal value) {
		this.values.put(id, value);
	}
	
	@Override
	public Optional<Set<Id>> clear(Id opId) {
		// TODO Auto-generated method stub
		Map<Id,LeafVal> concurrent = values.entrySet().stream()
				.filter(e->e.getKey().getVectorClock().compareTo(opId.getVectorClock())>=Ord.Eq.getValue())
				.collect(Collectors.toMap(e->e.getKey(), e->e.getValue()));
		this.values = concurrent;
		return Optional.of(concurrent.keySet());
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
		if(this.values.size() > 1) {
			JSONObject result = new JSONObject();
			for(Map.Entry<Id,LeafVal> e : values.entrySet()) {
				result.put(e.getKey().getReplicaId(), e.getValue().toString());
			}
		}
		
		if(!values.isEmpty()) {
			return this.getValue();
		}
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
	public Optional<Block> getOrderBlock(Cursor cur, int index) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}
}
