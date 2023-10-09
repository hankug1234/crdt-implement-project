package com.crdt.implement.opBaseCrdt.document.node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;

import org.json.JSONObject;

import com.crdt.implement.opBaseCrdt.document.keyType.Dock;
import com.crdt.implement.opBaseCrdt.document.keyType.IndexK;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;
import com.crdt.implement.opBaseCrdt.document.keyType.StringK;
import com.crdt.implement.opBaseCrdt.document.node.ordering.Block;
import com.crdt.implement.opBaseCrdt.document.node.ordering.BlockMetaData;
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
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes.ListT;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes.MapT;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes.RegT;
import com.crdt.implement.opBaseCrdt.document.typetag.TypeTag;
import com.crdt.implement.opBaseCrdt.document.values.BranchVal;
import com.crdt.implement.opBaseCrdt.document.values.EmptyList;
import com.crdt.implement.opBaseCrdt.document.values.EmptyMap;
import com.crdt.implement.opBaseCrdt.document.values.LeafVal;
import com.crdt.implement.opBaseCrdt.document.values.Val;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		Key key = tag.getKey();
		if(key instanceof Dock) {
			return Optional.of(this);
		}
		
		if(children.containsKey(tag)) {
			return Optional.of(this.children.get(tag));
		}
		return Optional.empty();
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
	
	public Optional<Set<Id>> clearElem(Id opId, TypeTag tag){
		Optional<Set<Id>> pres = clearAny(opId,tag);
		if(pres.isPresent()) {
			this.presentSets.put(tag.getKey(), pres.get());
		}
		return pres;
	}
	
	public Optional<Set<Id>> clearAny(Id opId, TypeTag tag){
		Optional<Node> node = this.findChild(tag);
		if(node.isPresent()) {
			Optional<Set<Id>> result =  node.get().clear(opId);
			if(result.isPresent()) {
				return result;
			}
		}
		return Optional.empty();
	}
	

	@Override
	public Optional<Set<Id>> clear(Id opId) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}
	
	public Optional<Node> getLeafViewNode(Cursor cur) {
		View view = cur.view();
		if(view instanceof ViewTypes.Leaf) {
			ViewTypes.Leaf leaf = (ViewTypes.Leaf) view;
			return Optional.of(this.findChild(leaf.getFinalKey()).get());
		}else {
			ViewTypes.Branch branch = (Branch) view;
			Optional<Node> node = findChild(branch.getHead());
			if(node.isPresent() && (node.get() instanceof BranchNode)) {
				BranchNode child = (BranchNode) node.get();
				return child.getLeafViewNode(branch.getTail());
			}else {
				return Optional.empty();
			}
		}
	}
	
	@Override
	public Optional<Node> applyOp(Operation op) {
		View view = op.getCur().view();
		if(view instanceof ViewTypes.Leaf) {
			if(op.getCur().getFinalKey().getKey() instanceof Dock) {
				throw new RuntimeException();
			}
			return Optional.of(applyAtLeaf(op));
		}else {
			ViewTypes.Branch branch = (Branch) view;
			Optional<Node> node = this.findChild(branch.getHead());
			if(!node.isPresent()) {
				throw new RuntimeException();
			}
			Optional<Node> result = node.get().applyOp(new Operation(op.getId(),branch.getTail(),op.getSignal()));
			addId(branch.getHead(),op.getId(),op.getSignal());
			return result;
		}
	}
	
	public Node applyAtLeaf(Operation op) {
		TypeTag key = op.getCur().getFinalKey();
		Signal signal = op.getSignal();
		
		if(signal instanceof SignalTypes.AssignS) {
			SignalTypes.AssignS assignS = (AssignS) signal;
			Val value = assignS.getValue();
			if(value instanceof BranchVal) {
				clearElem(op.getId(),key);
				this.addId(key, op.getId(), signal);
				BranchNode node = (BranchNode) getChild(key);
				this.children.put(key, getChild(key));
			}else {
				this.addId(key, op.getId(), signal);
				RegNode reg = (RegNode) this.getChild(key);
				reg.clear(op.getId());
				LeafVal leafVal = (LeafVal) value;
				reg.setRegValue(op.getId(),leafVal);
				this.children.put(key, reg);
				
			}
			
		}else if(signal instanceof SignalTypes.DeleteS) {
			clearElem(op.getId(),key);
		}else if(signal instanceof SignalTypes.InsertS) {
			
			Optional<Node> node = this.findChild(key);
			if(node.isPresent() && (node.get() instanceof ListNode)) {
				SignalTypes.InsertS insert = (InsertS) signal;
				ListNode list = (ListNode) node.get();
				
				Val value = insert.getValue();
				Key indexK = insert.getKey();
				TypeTag tag;
				if(value instanceof EmptyList) {
					tag = new ListT(indexK);
				}else if(value instanceof EmptyMap) {
					tag = new MapT(indexK);
				}else {
					tag = new RegT(indexK);
				}
				list.getOrder().insertByMetaData(insert.getMeta(), tag);
				list.applyAtLeaf(new Operation(op.getId(),new Cursor(tag),new SignalTypes.AssignS(value)));
			}
			
		}else if(signal instanceof SignalTypes.MoveS) {
			
			Optional<Node> node = this.findChild(key);
			if(node.isPresent() && (node.get() instanceof ListNode)) {
				SignalTypes.MoveS moveS = (MoveS) signal;
				ListNode list = (ListNode) node.get();
				list.getOrder().move(moveS.getFrom(), moveS.getTo(), moveS.getLocation());
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

	@Override
	public Object toJson() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Node> query(Cursor cur) {
		return this.getLeafViewNode(cur);
	}

	@Override
	public Optional<BlockMetaData> getInsertMetaData(Cursor cur, int index) {
		Optional<Node> node = query(cur);
		if(node.isPresent()) {
			if(node.get() instanceof ListNode) {
				ListNode listNode = (ListNode) node.get();
				return Optional.of(listNode.getOrder().getInsertMetaData(index));
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<Block> getOrderBlock(Cursor cur, int index) {
		Optional<Node> node = query(cur);
		if(node.isPresent()) {
			if(node.get() instanceof ListNode) {
				ListNode listNode = (ListNode) node.get();
				return listNode.getOrder().findBlockByIndex(index);
			}
		}
		return Optional.empty();
	}

	
}
