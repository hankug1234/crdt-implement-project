package com.crdt.implement.opBaseCrdt.document.node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.crdt.implement.opBaseCrdt.document.Id;
import com.crdt.implement.opBaseCrdt.document.cursor.Cursor;
import com.crdt.implement.opBaseCrdt.document.cursor.View;
import com.crdt.implement.opBaseCrdt.document.cursor.ViewTypes;
import com.crdt.implement.opBaseCrdt.document.cursor.ViewTypes.Leaf;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;
import com.crdt.implement.opBaseCrdt.document.keyType.StringK;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes;
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
	
	@Override
	public Optional<Set<Id>> clear(Id opId) {
		Optional<Set<Id>> result =  clearMap(opId);
		if(result.isPresent()) {
			return result;
		}
		return Optional.empty();
	}
	
	public Optional<Set<Id>> clearMap(Id opId){
		Set<String> keys = keySet();
		if(keys.isEmpty()) {
			return Optional.empty();
		}
		
		Set<Id> result = new HashSet<>();
		for(String key : keys) {
			Optional<Set<Id>> pres = clearElem(opId,new StringK(key));
			if(pres.isPresent()) {
				result.addAll(pres.get());
			}
		}
		return Optional.of(result);
	}
}
