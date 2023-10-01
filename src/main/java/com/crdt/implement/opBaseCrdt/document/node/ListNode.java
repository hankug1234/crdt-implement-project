package com.crdt.implement.opBaseCrdt.document.node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.crdt.implement.opBaseCrdt.document.Id;
import com.crdt.implement.opBaseCrdt.document.cursor.Cursor;
import com.crdt.implement.opBaseCrdt.document.cursor.View;
import com.crdt.implement.opBaseCrdt.document.cursor.ViewTypes;
import com.crdt.implement.opBaseCrdt.document.cursor.ViewTypes.Leaf;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;
import com.crdt.implement.opBaseCrdt.document.list.ListRef;
import com.crdt.implement.opBaseCrdt.document.list.RefTypes;
import com.crdt.implement.opBaseCrdt.document.list.RefTypes.IndexR;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes;
import com.crdt.implement.opBaseCrdt.document.typetag.TypeTag;
import com.crdt.implement.vectorClock.VectorClock;

import lombok.Getter;


@Getter
public class ListNode extends BranchNode{
	
	private Map<ListRef,ListRef> order;
	private Map<ListRef,ListRef> inverseOrder;
	private Map<VectorClock,Map<ListRef,ListRef>> orderArchive;
	
	public ListNode(Map<TypeTag,Node> children, Map<Key,Set<Id>> presentSets,Map<ListRef,ListRef> order,Map<VectorClock,Map<ListRef,ListRef>> orderArchive) {
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
	
	
	@Override
	public Optional<Set<Id>> clear(Id opId, Key key) {
		TypeTag tag = new TagTypes.ListT(key);
		Optional<Node> child = this.findChild(tag);
		if(this.findChild(tag).isPresent()) {
			ListNode node = (ListNode) child.get();
			return node.clearList(opId, new RefTypes.HeadR());
		}
		return Optional.empty();
	}
	
	public Optional<Set<Id>> clearList(Id opId, ListRef ref){
		Optional<ListRef> next = this.getNextRef(ref);
		if(next.isPresent() && (next.get() instanceof RefTypes.TailR)) {
			return Optional.empty();
		}
		
		Set<Id> result = new HashSet<>();
		while(next.isPresent() && !(next.get() instanceof RefTypes.TailR)) {
			Optional<Set<Id>> pres = clearElem(opId, RefTypes.RtoKey(next.get()));
			if(pres.isPresent()) {
				result.addAll(pres.get());
			}
		}
		return Optional.of(result);
	}

}
