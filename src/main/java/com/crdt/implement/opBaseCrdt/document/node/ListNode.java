package com.crdt.implement.opBaseCrdt.document.node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.crdt.implement.opBaseCrdt.document.Id;
import com.crdt.implement.opBaseCrdt.document.keyType.IndexK;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;
import com.crdt.implement.opBaseCrdt.document.node.ordering.OrderList;
import com.crdt.implement.opBaseCrdt.document.typetag.TypeTag;

import lombok.Getter;

@Getter
public class ListNode extends BranchNode{
	
	OrderList order;
	
	public ListNode(Map<TypeTag,Node> children, Map<Key,Set<Id>> presentSets,OrderList order) {
		super(children,presentSets);
		this.order = order; 
	}
	
	public ListNode() {
		super(new HashMap<>(), new HashMap<>());
		this.order = new OrderList();
	}
	
	public Node clone() {
		return null;
	}
	
	public BranchNode cloneWithChildren(Map<TypeTag,Node> children) {
		return new ListNode(children,this.getPresentSets(),this.order);
	}
	
	public BranchNode cloneWithPresentSets(Map<Key,Set<Id>> presentSets) {
		return new ListNode(this.getChildren(),presentSets,this.order);
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
		List<IndexK> orderedList = this.order.getOrderList();
		for(IndexK id : orderedList) {
			Optional<Set<Id>> pres = clearElem(opId, id);
			if(pres.isPresent()) {
				result.addAll(pres.get());
			}
		}
		return Optional.of(result);
	}

}
