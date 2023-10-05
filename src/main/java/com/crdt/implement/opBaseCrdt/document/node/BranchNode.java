package com.crdt.implement.opBaseCrdt.document.node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;

import com.crdt.implement.opBaseCrdt.document.keyType.IndexK;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;
import com.crdt.implement.opBaseCrdt.document.keyType.StringK;
import com.crdt.implement.opBaseCrdt.document.signal.LocationInstructor;
import com.crdt.implement.opBaseCrdt.document.signal.Signal;
import com.crdt.implement.opBaseCrdt.document.signal.SignalTypes;
import com.crdt.implement.opBaseCrdt.document.signal.SignalTypes.AssignS;
import com.crdt.implement.opBaseCrdt.document.signal.SignalTypes.InsertS;
import com.crdt.implement.opBaseCrdt.document.signal.SignalTypes.MoveS;
import com.crdt.implement.opBaseCrdt.document.Id;
import com.crdt.implement.opBaseCrdt.document.Operation;
import com.crdt.implement.opBaseCrdt.document.cursor.Cursor;
import com.crdt.implement.opBaseCrdt.document.cursor.View;
import com.crdt.implement.opBaseCrdt.document.cursor.ViewTypes;
import com.crdt.implement.opBaseCrdt.document.cursor.ViewTypes.Branch;
import com.crdt.implement.opBaseCrdt.document.cursor.ViewTypes.Leaf;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes.ListT;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes.MapT;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes.RegT;
import com.crdt.implement.opBaseCrdt.document.typetag.TypeTag;
import com.crdt.implement.opBaseCrdt.document.values.BranchVal;
import com.crdt.implement.opBaseCrdt.document.values.EmptyMap;
import com.crdt.implement.opBaseCrdt.document.values.LeafVal;
import com.crdt.implement.opBaseCrdt.document.values.Val;
import com.crdt.implement.vectorClock.Ord;
import com.crdt.implement.vectorClock.VectorClock;

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
	
	public void addId(TypeTag tag,Id id,Signal signal) {
		if(!(signal instanceof SignalTypes.DeleteS)) {
			this.getPres(tag.getKey()).add(id);
		}
	}
	
	public Optional<Node> findChild(TypeTag tag){
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
	
	public Optional<Set<Id>> clearElem(Id opId, Key key){
		Optional<Set<Id>> pres = clearAny(opId,key);
		if(pres.isPresent()) {
			this.presentSets.put(key, pres.get());
		}
		return pres;
	}
	
	public Optional<Set<Id>> clearAny(Id opId, Key key){
		Set<Id> result = new HashSet<>();
		TypeTag tag = new MapT(key);
		Optional<Node> node = this.findChild(tag);
		if(node.isPresent()) {
			Optional<Set<Id>> result1 =  node.get().clear(opId);
			if(result1.isPresent()) {
				result.addAll(result1.get());
			}
		}
		
		tag = new ListT(key);
		node = this.findChild(tag);
		if(node.isPresent()) {
			Optional<Set<Id>> result2 =  node.get().clear(opId); 
			if(result2.isPresent()) {
				result.addAll(result2.get());
			}
		}
		
		
		tag = new RegT(key);
		node = this.findChild(tag);
		if(node.isPresent()) {
			Optional<Set<Id>> result3 =  node.get().clear(opId);
			if(result3.isPresent()) {
				result.addAll(result3.get());
			}
		}
		
		if(result.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(result);
	
	}
	

	@Override
	public Optional<Set<Id>> clear(Id opId) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}
	
	public Optional<View> getLeafView(Cursor cur) {
		View view = cur.view();
		if(view instanceof ViewTypes.Leaf) {
			return Optional.of(view);
		}else {
			ViewTypes.Branch branch = (Branch) view;
			Optional<Node> node = findChild(branch.getHead());
			if(node.isPresent() && (node.get() instanceof BranchNode)) {
				BranchNode child = (BranchNode) node.get();
				return child.getLeafView(branch.getTail());
			}else {
				return Optional.empty();
			}
		}
	}

	public Optional<Set<String>> keys(Cursor cur) {
		Optional<View> view = this.getLeafView(cur);
		if(view.isPresent() && view.get() instanceof ViewTypes.Leaf) {
			ViewTypes.Leaf leaf = (Leaf) view.get();
			Optional<Node> node = findChild(new TagTypes.MapT(leaf.getFinalKey()));
			if(node.isPresent()) {
				MapNode map = (MapNode) node.get();
				return Optional.of(map.keySet());
			}
		}
		
		return Optional.empty();
	}

	
	public Optional<List<LeafVal>> values(Cursor cur) {
		Optional<View> view = this.getLeafView(cur);
		if(view.isPresent() && view.get() instanceof ViewTypes.Leaf) {
			ViewTypes.Leaf leaf = (Leaf) view.get();
			Optional<Node> node = findChild(new TagTypes.RegT(leaf.getFinalKey()));
			if(node.isPresent()) {
				RegNode reg = (RegNode) node.get();
				return Optional.of(reg.getValues());
			}
		}
		return Optional.empty();
	}

	
	@Override
	public Optional<Node> applyOp(Operation op) {
		View view = op.getCur().view();
		if(view instanceof ViewTypes.Leaf) {
			return Optional.of(applyAtLeaf(op));
		}else {
			ViewTypes.Branch branch = (Branch) view;
			Optional<Node> result = applyOp(new Operation(op.getId(),branch.getTail(),op.getSignal()));
			addId(branch.getHead(),op.getId(),op.getSignal());
			return result;
		}
	}
	
	public Node applyAtLeaf(Operation op) {
		Key key = op.getCur().getFinalKey();
		Signal signal = op.getSignal();
		
		if(signal instanceof SignalTypes.AssignS) {
			SignalTypes.AssignS assignS = (AssignS) signal;
			Val value = assignS.getValue();
			if(value instanceof BranchVal) {
				TypeTag tag = (value instanceof EmptyMap)?new TagTypes.MapT(key):new TagTypes.ListT(key);
				clearElem(op.getId(),key);
				this.addId(tag, op.getId(), signal);
				this.children.put(tag, getChild(tag));
			}else {
				TypeTag tag = new TagTypes.RegT(key);
				clear(op.getId());
				this.addId(tag, op.getId(), signal);
				RegNode reg = (RegNode) this.children.get(tag);
				LeafVal leafVal = (LeafVal) value;
				reg.setRegValue(op.getId(),leafVal);
			}
			
		}else if(signal instanceof SignalTypes.DeleteS) {
			clearElem(op.getId(),key);
		}else if(signal instanceof SignalTypes.InsertS) {
			if(this instanceof ListNode) {
				SignalTypes.InsertS insert = (InsertS) signal;
				ListNode node = (ListNode) this;
				IndexK index = (IndexK) key;
				node.getOrder().insert(insert.getIndex(), index);
				applyAtLeaf(new Operation(op.getId(),new Cursor(index),new SignalTypes.AssignS(insert.getValue())));
			}
			
			
		}else if(signal instanceof SignalTypes.MoveS) {
			
			if(this instanceof ListNode) {
				SignalTypes.MoveS moveS = (MoveS) signal;
				Cursor targetCursor = moveS.getTarget();
				ListNode node = (ListNode) this;
				IndexK src = (IndexK) key; IndexK target = (IndexK) targetCursor.getFinalKey();
				node.getOrder().moveByKey(src, target);
			}
			
		}else {
			throw new RuntimeException();
		}
		
		return this;
	}
	
	@Override
	public Node clone() {
		return null;
	}
	
}
