package com.crdt.implement.opBaseCrdt.document.node;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.crdt.implement.opBaseCrdt.document.Id;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;
import com.crdt.implement.opBaseCrdt.document.keyType.StringK;
import com.crdt.implement.opBaseCrdt.document.typetag.TypeTag;

public class MapNode extends BranchNode{
	public MapNode(Map<TypeTag,Node> children, Map<Key,Set<Id>> presentSets) {
		super(children,presentSets);
	}
	
	public MapNode() {
		super(new HashMap<>(),new HashMap<>());
	}
	
	public Set<String> keySet(){
		return this.getPresentSets().entrySet().stream().map((Map.Entry<Key,Set<Id>> e)->{
			StringK key = (StringK) e.getKey();
			return key.getValue();
		}).collect(Collectors.toSet());
	}
	
	public BranchNode cloneWithChildren(Map<TypeTag,Node> children) {
		return new MapNode(children,this.getPresentSets());
	}
	
	public BranchNode cloneWithPresentSets(Map<Key,Set<Id>> presentSets) {
		return new MapNode(this.getChildren(),presentSets);
	}
}
