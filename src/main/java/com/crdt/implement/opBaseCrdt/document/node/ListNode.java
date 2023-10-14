package com.crdt.implement.opBaseCrdt.document.node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.crdt.implement.opBaseCrdt.document.Id;
import com.crdt.implement.opBaseCrdt.document.keyType.IndexK;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;
import com.crdt.implement.opBaseCrdt.document.node.ordering.OrderList;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes.ListT;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes.MapT;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes.RegT;
import com.crdt.implement.opBaseCrdt.document.typetag.TypeTag;

import lombok.Getter;

@Getter
public class ListNode extends BranchNode{
	
	private OrderList order;
	
	public ListNode(Map<TypeTag,Node> children, Map<Key,Set<Id>> presentSets,OrderList order) {
		super(children,presentSets);
		this.order = order; 
	}
	
	public ListNode() {
		super(new HashMap<>(), new HashMap<>());
		this.order = new OrderList();
	}
	
	public Node clone() {
		Map<TypeTag,Node> newChildren = new HashMap<>();
		Map<Key,Set<Id>> newPresentSets = new HashMap<>();
		for(Map.Entry<TypeTag, Node> e : this.getChildren().entrySet()) {
			newChildren.put(e.getKey(), e.getValue().clone());
		}
		for(Map.Entry<Key, Set<Id>> e : this.getPresentSets().entrySet()) {
			newPresentSets.put(e.getKey(), new HashSet<>(e.getValue()));
		}
		
		
		return new ListNode(newChildren, newPresentSets, this.order.clone());
	}
	
	@Override 
	public Object toJson() {
		List<TypeTag> orderedList = this.order.getOrderList();
		JSONArray result = new JSONArray();
		for(TypeTag k : orderedList) {
			Optional<Node> node = this.findChild(k);
			if(node.isPresent()) {
				result.put(node.get().toJson());
			}
		}
		return result;
	}
	
	@Override
	public Optional<Set<Id>> clear(Id opId) {
		Optional<Set<Id>> result =  clearList(opId);
		if(result.isPresent()) {
			return result;
		}
		return Optional.empty();
	}
	
	public Optional<Set<Id>> clearList(Id opId){
		
		Set<Id> result = new HashSet<>();
		List<TypeTag> orderedList = this.order.getOrderList();
		for(TypeTag id : orderedList) {
			Optional<Set<Id>> pres = clearElem(opId, id);
			if(pres.isPresent()) {
				result.addAll(pres.get());
			}else {
				this.getOrder().deleteByTag(id);
			}
		}
		return Optional.of(result);
	}

}
