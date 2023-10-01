package com.crdt.implement.opBaseCrdt.document.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.crdt.implement.opBaseCrdt.document.Id;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;
import com.crdt.implement.opBaseCrdt.document.list.RefTypes;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes;
import com.crdt.implement.opBaseCrdt.document.typetag.TypeTag;
import com.crdt.implement.opBaseCrdt.document.values.LeafVal;

public class RegNode extends LeafNode{

	public RegNode(Map<Id,LeafVal> values) {
		super(values);
	}
	
	public RegNode() {
		super(new HashMap<>());
	}
}
