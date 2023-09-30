package com.crdt.implement.opBaseCrdt.document.node;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.crdt.implement.opBaseCrdt.document.Id;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;
import com.crdt.implement.opBaseCrdt.document.list.ListRef;
import com.crdt.implement.opBaseCrdt.document.list.RefTypes;
import com.crdt.implement.opBaseCrdt.document.typetag.TypeTag;

import lombok.Getter;


@Getter
public class ListNode extends BranchNode{
	
	private Map<ListRef,ListRef> order;
	private Map<ListRef,ListRef> inverseOrder;
	private Map<Long,Map<ListRef,ListRef>> orderArchive;
	
	public ListNode(Map<TypeTag,Node> children, Map<Key,Set<Id>> presentSets,Map<ListRef,ListRef> order,Map<Long,Map<ListRef,ListRef>> orderArchive) {
		super(children,presentSets);
		this.order = order; this.orderArchive = orderArchive;
		this.inverseOrder = new HashMap<>();
		for(Map.Entry<ListRef,ListRef> o : order.entrySet()) {
			this.inverseOrder.put(o.getValue(), o.getKey());
		}
	}
	
	public ListNode() {
		super(new HashMap<>(), new HashMap<>());
		this.order = new HashMap<>();
		this.order.put(new RefTypes.HeadR(), new RefTypes.TailR());
		this.orderArchive = new HashMap<>();
		this.inverseOrder = new HashMap<>();
		for(Map.Entry<ListRef,ListRef> o : order.entrySet()) {
			this.inverseOrder.put(o.getValue(), o.getKey());
		}
		
	}
	
	public BranchNode cloneWithChildren(Map<TypeTag,Node> children) {
		return new ListNode(children,this.getPresentSets(),this.order,this.orderArchive);
	}
	
	public BranchNode cloneWithPresentSets(Map<Key,Set<Id>> presentSets) {
		return new ListNode(this.getChildren(),presentSets,this.order,this.orderArchive);
	}
	
	public ListNode setNextRef(ListRef src, ListRef target) {
		this.order.put(src, target);
		return this;
	}
	
	public ListRef getPreviousRef(ListRef ref) {
		return this.inverseOrder.getOrDefault(ref, new RefTypes.HeadR());
	}
	
	public ListRef getNextRef(ListRef ref) {
		return this.order.getOrDefault(ref, new RefTypes.TailR());
	}

}
