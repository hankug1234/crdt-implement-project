package com.crdt.implement.opBaseCrdt.document.node;


import java.util.HashMap;
import java.util.Map;

import com.crdt.implement.opBaseCrdt.document.Id;
import com.crdt.implement.opBaseCrdt.document.values.LeafVal;

public class RegNode extends LeafNode{

	public RegNode(Map<Id,LeafVal> values) {
		super(values);
	}
	
	public RegNode() {
		super(new HashMap<>());
	}
}
