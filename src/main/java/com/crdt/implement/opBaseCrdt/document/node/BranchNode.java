package com.crdt.implement.opBaseCrdt.document.node;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.crdt.implement.opBaseCrdt.document.keyType.Key;
import com.crdt.implement.opBaseCrdt.document.Id;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes;
import com.crdt.implement.opBaseCrdt.document.typetag.TypeTag;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class BranchNode implements Node{

	private Map<TypeTag,Node> children;
	private Map<Key,Set<Id>> presentSets;
	
	public void setPres(Key key, Set<Id> pres) {
		this.presentSets.put(key, pres);
	}
	
	public Set<Id> getPres(Key key){
		return this.presentSets.getOrDefault(key, new HashSet<>());
	}
	
	private Optional<Node> findChild(TypeTag tag){
		return Optional.of(this.children.get(tag));
	}
	
	public Node getChild(TypeTag tag) {
		Optional<Node> node = this.findChild(tag);
		if(!node.isPresent()) {
			if(tag instanceof TagTypes.MapT) {
				return new MapNode();
			}else if(tag instanceof TagTypes.ListT) {
				return new ListNode();
			}else {
				return new RegNode();
			}
			
		}
		return node.get();
	}
	
	
}
