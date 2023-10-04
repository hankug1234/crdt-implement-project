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
import com.crdt.implement.opBaseCrdt.document.list.ListRef;
import com.crdt.implement.opBaseCrdt.document.list.RefTypes;
import com.crdt.implement.opBaseCrdt.document.list.RefTypes.IndexR;
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
		TypeTag tag = new MapT(key);
		Optional<Node> node = this.findChild(tag);
		if(node.isPresent()) {
			return node.get().clear(opId); 
		}
		
		tag = new ListT(key);
		node = this.findChild(tag);
		if(node.isPresent()) {
			return node.get().clear(opId); 
		}
		
		
		tag = new RegT(key);
		node = this.findChild(tag);
		if(node.isPresent()) {
			return node.get().clear(opId); 
		}
		
		return Optional.empty();
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
	
	public Optional<ListNode> setNextRef(ListRef src, ListRef target) {
		if(this instanceof ListNode) {
			ListNode node = (ListNode) this;
			node.getOrder().put(src, target);
			return Optional.of(node);
		}
		return Optional.empty();
	}
	
	public Optional<ListRef> getPreviousRef(ListRef ref) {
		if(this instanceof ListNode) {
			ListNode node = (ListNode) this;
			return Optional.of(node.getInverseOrder().getOrDefault(ref, new RefTypes.HeadR()));
		}
		return Optional.empty();
	}
	
	public Optional<ListRef> getNextRef(ListRef ref) {
		if(this instanceof ListNode) {
			ListNode node = (ListNode) this;
			return Optional.of(node.getOrder().getOrDefault(ref, new RefTypes.TailR()));
		}
		return Optional.empty();
	}

	@Override
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

	@Override
	public Optional<List<LeafVal>> values(Cursor cur) {
		Optional<View> view = this.getLeafView(cur);
		if(view.isPresent() && view.get() instanceof ViewTypes.Leaf) {
			ViewTypes.Leaf leaf = (Leaf) view.get();
			Optional<Node> node = findChild(new TagTypes.RegT(leaf.getFinalKey()));
			if(node.isPresent()) {
				RegNode reg = (RegNode) node.get();
				return reg.values(cur);
			}
		}
		return Optional.empty();
	}

	public List<Operation> concurrentOpsSince(VectorClock vectorClock,List<Operation> ops){
		return ops.stream().filter((Operation op) -> {
			
			Key key = op.getCur().getFinalKey();
			
			if(op.getId().getVectorClock().compareTo(vectorClock) >= Ord.Eq.getValue() 
				&& !(op.getSignal() instanceof SignalTypes.AssignS)
				&& (findChild(new RegT(key)).isPresent() || findChild(new MapT(key)).isPresent() || findChild(new ListT(key)).isPresent())) {
				return true;
			}
			return false;}
			).toList();
	}
	
	@Override
	public Optional<Node> applyOp(Operation op, List<Operation> ops) {
		View view = op.getCur().view();
		if(view instanceof ViewTypes.Leaf) {
			ViewTypes.Leaf leaf = (Leaf) view;
			if(this instanceof ListNode) {
				List<Operation> concurrentOps = concurrentOpsSince(op.getId().getVectorClock(),ops);
				boolean moveOps = concurrentOps.stream().anyMatch(o->o.getSignal() instanceof SignalTypes.MoveS);
				if(!concurrentOps.isEmpty() && moveOps) {
					
					ListNode node = (ListNode) this;
					List<Map.Entry<Id, Map<ListRef,ListRef>>> newOrders =  node.getOrderArchive().entrySet().stream()
					.filter(e->e.getKey().compareTo(op.getId()) >= Ord.Eq.getValue()).toList();
					
					if(!newOrders.isEmpty()) {
						Id min = newOrders.get(0).getKey();
						for(Map.Entry<Id, Map<ListRef,ListRef>> e : newOrders) {
							Id n = e.getKey();
							int cmp = min.getVectorClock().compareTo(n.getVectorClock()); 
							if( cmp >= Ord.Gt.getValue()) {
								min = n;
							}else if(cmp >= Ord.Eq.getValue() && min.getReplicaId().compareTo(n.getReplicaId()) > 0){
								min = n;
							}
						}
						
						
						return Optional.of()
					}
					
				}
				
			}
			return Optional.of(applyAtLeaf(op));
		}else {
			ViewTypes.Branch branch = (Branch) view;
			Optional<Node> result = applyOp(new Operation(op.getId(),branch.getTail(),op.getSignal()),ops);
			
			addId(branch.getHead(),op.getId(),op.getSignal());
			
			return result;
			
		}
	}

	@Override
	public Optional<Cursor> next(Cursor cur) {
		Optional<View> view = this.getLeafView(cur);
		if(view.isPresent() && view.get() instanceof ViewTypes.Leaf) {
			ViewTypes.Leaf leaf = (Leaf) view.get();
			Optional<ListRef> next = getNextRef(RefTypes.keyToR(leaf.getFinalKey()));
			
			while(next.isPresent() && !(next.get() instanceof RefTypes.TailR)) {
				
				RefTypes.IndexR indexR = (IndexR) next.get();
				Key key = RefTypes.RtoKey(indexR);
				
				if(!this.getPres(key).isEmpty()) {
					Cursor newCur = new Cursor(key);
					return Optional.of(newCur);
				}
				
				next = getNextRef(RefTypes.keyToR(leaf.getFinalKey()));
			}
		}
		return Optional.empty();
	}
	
	public Node saveOrder(Operation op){
		if(this instanceof ListNode) {
			ListNode node = (ListNode) this;
			if(node.getOrderArchive().get(op.getId()).isEmpty()) {
				Map<ListRef,ListRef> saveOrder = new HashMap<>();
				for(Map.Entry<ListRef,ListRef> e : node.getOrder().entrySet()) {
					saveOrder.put(e.getKey().clone(), e.getValue().clone());
				}
				node.getOrderArchive().put(op.getId(), saveOrder);
			}
			
		}
		return this;
	}
	
	public Node applyMany(List<Operation> ops) {
		for(Operation op : ops) {
			applyAtLeaf(op);
		}
		return this;
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
				clear(op.getId(),key);
				this.addId(tag, op.getId(), signal);
				RegNode reg = (RegNode) this.children.get(tag);
				LeafVal leafVal = (LeafVal) value;
				reg.setRegValue(op.getId(),leafVal);
			}
			
		}else if(signal instanceof SignalTypes.DeleteS) {
			saveOrder(op);
			clearElem(op.getId(),key);
		}else if(signal instanceof SignalTypes.InsertS) {
			ListRef prevRef = RefTypes.keyToR(key);
			Optional<ListRef> nextRef = this.getNextRef(prevRef);
			SignalTypes.InsertS insertS = (InsertS) signal;
			
			if(nextRef.isPresent()) {
				ListRef ref = nextRef.get();
				
				while(ref instanceof IndexR) {
					IndexR indexR = (IndexR) ref;
					if(op.getId().compareTo(indexR.getIndex()) >= Ord.Eq.getValue()) {
						break;
					}
					nextRef = this.getNextRef(ref);
					prevRef = ref;
					ref = nextRef.get();
				}
				
				IndexR indexR = new IndexR(op.getId());
				applyAtLeaf(new Operation(op.getId(),new Cursor(new IndexK(op.getId())),new SignalTypes.AssignS(insertS.getValue())));
				
				saveOrder(op);
				setNextRef(prevRef,indexR);
				setNextRef(indexR,ref);
			}
			
		}else if(signal instanceof SignalTypes.MoveS) {
			SignalTypes.MoveS moveS = (MoveS) signal;
			LocationInstructor location = moveS.getLocation();
			Cursor targetCursor = moveS.getTarget();
			
			ListRef srcNodeRef = RefTypes.keyToR(op.getCur().getFinalKey());
			Optional<ListRef> srcNextNodeRef = getNextRef(srcNodeRef);
			
			ListRef targetNodeRef = RefTypes.keyToR(targetCursor.getFinalKey());
			Optional<ListRef> targetNextNodeRef = getNextRef(targetNodeRef);
			
			if( srcNextNodeRef.isPresent() && targetNextNodeRef.isPresent() &&
				!(location == LocationInstructor.Before && srcNextNodeRef.get().equals(targetNodeRef)) &&
				!(location == LocationInstructor.After && targetNextNodeRef.get().equals(srcNodeRef)) &&
				!this.findChild(new TagTypes.RegT(targetCursor.getFinalKey())).isEmpty()) {
				
				saveOrder(op);
				setNextRef(getPreviousRef(srcNodeRef).get(),srcNextNodeRef.get());
				
				if(location == LocationInstructor.Before) {
					
					setNextRef(getPreviousRef(targetNodeRef).get(),srcNodeRef);
					setNextRef(srcNodeRef,targetNodeRef);
					
				}else {
					
					setNextRef(targetNodeRef,srcNodeRef);
					setNextRef(srcNodeRef,targetNextNodeRef.get());
					
				}
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
