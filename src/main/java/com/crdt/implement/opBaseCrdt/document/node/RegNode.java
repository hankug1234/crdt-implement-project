package com.crdt.implement.opBaseCrdt.document.node;


import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.crdt.implement.opBaseCrdt.document.Id;
import com.crdt.implement.opBaseCrdt.document.values.LeafVal;

public class RegNode extends LeafNode{

	public RegNode(Map<Id,LeafVal> values) {
		super(values);
	}
	
	public RegNode() {
		super(new HashMap<>());
	}
	
	@Override
	public Node clone() {
		Map<Id,LeafVal> newValues = new HashMap<>();
		for(Map.Entry<Id, LeafVal> e : this.getIdValueMap().entrySet()) {
			newValues.put(e.getKey(),e.getValue());
		}
		return new RegNode(newValues);
	}
	
}
