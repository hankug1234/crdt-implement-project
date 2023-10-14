package com.crdt.implement.opBaseCrdt.document.node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

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
	
	public MapNode clone() {
		Map<TypeTag,Node> newChildren = new HashMap<>();
		Map<Key,Set<Id>> newPresentSets = new HashMap<>();
		for(Map.Entry<TypeTag, Node> e : this.getChildren().entrySet()) {
			newChildren.put(e.getKey(), e.getValue().clone());
		}
		for(Map.Entry<Key, Set<Id>> e : this.getPresentSets().entrySet()) {
			newPresentSets.put(e.getKey(), new HashSet<>(e.getValue()));
		}
		return new MapNode(newChildren,newPresentSets);
	}
	
	public Set<TypeTag> keySet(){
		return this.getChildren().entrySet().stream().map((Map.Entry<TypeTag,Node> e)->{
			TypeTag key = (TypeTag) e.getKey();
			return key;
		}).collect(Collectors.toSet());
	}
	
	@Override 
	public Object toJson() {
		Set<TypeTag> keys = keySet();
		JSONObject result = new JSONObject();
		Map<String,JSONArray> temp = new HashMap<>();
		for(TypeTag key : keys) {
			StringK sk = (StringK) key.getKey();
			String name = sk.getValue();
			
			Node node = this.getChild(key);
			
			Object json = node.toJson();
			
			if(json != null) {
				if(temp.containsKey(name)) {
					temp.get(name).put(json);
				}else {
					JSONArray arr = new JSONArray();
					arr.put(json);
					temp.put(name, arr);
				}
			}
		}
		
		for(Map.Entry<String, JSONArray> e : temp.entrySet()) {
			if(e.getValue().length() > 1) {
				result.put(e.getKey(), e.getValue());
			}else if(e.getValue().length() == 1){
				result.put(e.getKey(), e.getValue().get(0));
			}
		}
		
		return result;
	
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
		Set<TypeTag> keys = keySet();
		if(keys.isEmpty()) {
			return Optional.empty();
		}
		
		Set<Id> result = new HashSet<>();
		for(TypeTag key : keys) {
			Optional<Set<Id>> pres = clearElem(opId,key);
			if(pres.isPresent()) {
				result.addAll(pres.get());
			}
		}
		return Optional.of(result);
	}
}
