package com.crdt.implement.opBaseCrdt.document.node;

import java.util.ArrayList;
import java.util.List;

import com.crdt.implement.opBaseCrdt.document.values.LeafVal;

public class RegNode extends LeafNode{

	public RegNode(List<LeafVal> values) {
		super(values);
	}
	
	public RegNode() {
		super(new ArrayList<>());
	}
}
