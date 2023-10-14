package com.crdt.implement.opBaseCrdt.document.node.ordering;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.crdt.implement.opBaseCrdt.document.Id;
import com.crdt.implement.opBaseCrdt.document.keyType.IndexK;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;
import com.crdt.implement.opBaseCrdt.document.typetag.TypeTag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@AllArgsConstructor
@Getter
public class OrderList {
	private List<Block> list;
	private long lastSeqNr;
	
	public OrderList() {
		this.list = new ArrayList<>();
		this.lastSeqNr = 0;
	}
	
	public OrderList clone() {
		List<Block> newList = new ArrayList<>();
		newList.addAll(this.list);
		return new OrderList(newList,this.lastSeqNr);
	}
	
	public List<TypeTag> getOrderList(){
		return getList().stream().map(l -> l.getValue().getContent().get()).toList();
	}
	
	public List<Block> getList(){
		List<Block> result = new ArrayList<>();
		for(Block block : list) {
			if(!block.isTombstone()) {
				Content content = block.getValue();
				if(content.isMoved()) {
					OrderId from  = content.getMoved().get().getFrom();
					int index = indexOf(from);
					if(index != -1) {
						Block target = list.get(index);
						OrderId movedTo = target.getMovedTo().get();
						if(block.getId().equals(movedTo) && !target.isTombstone()) {
							result.add(target);
						}
					}
				}else {
					if(!block.getMovedTo().isPresent()) {
						result.add(block);
					}
				}
			}
		}
		return result;
	}
	
	public int indexOf(OrderId id) {
		int count = 0;
		for(Block block : list) {
			if(block.getId().equals(id)) {
				return count;
			}
			count +=1;
		}
		return -1;
	}
	
	public Optional<Block> findBlockByIndex(int index) {
		List<Block> l = getList();
		if(index >= l.size()) {
			return Optional.empty();
		}
		return Optional.of(l.get(index));
	}
	
	private Optional<OrderId> findPosition(int index) {
		List<Block> l = getList();
		if(index >= l.size()) {
			return Optional.empty();
		}
		return Optional.of(l.get(index).getId());
	}
	
	public int findInsertIndex(Block block) {
		
		boolean skip = false;
		int left = block.getLeft().isPresent() ? indexOf(block.getLeft().get()) : -1;
		int right = block.getRight().isPresent() ? indexOf(block.getRight().get()) : list.size();
		
		int dst = left + 1; int i = dst; OrderId id1 = block.getId(); 
		
		for(int index = i; index <=list.size(); index ++) {	
			dst = skip ? dst : index;
			
			if(index == right || index == this.list.size()) {
				return dst;
			}
			
			Block b = list.get(index);
			int bleft  = b.getLeft().isPresent() ? indexOf(b.getLeft().get()) : -1;
			int bright = b.getRight().isPresent() ? indexOf(b.getRight().get()) : list.size();
			OrderId id2 = b.getId();
			
			if(bleft < left || (bleft == left && bright == right && id1.compareTo(id2) <= 0) ) {
				return dst;
			}else {
				skip = bleft == left ? id1.compareTo(id2) <= 0 : skip;
			}
		}
		
		return dst;
		
	}
	
	public void delete(int index) {
		Optional<OrderId> target = findPosition(index);
		int i = target.isPresent() ? indexOf(target.get()) : -1;
		Block block = this.list.get(i);
		block.setTombstone();
	}
	
	public void deleteByTag(TypeTag tag) {
		for(Block block : this.getList()) {
			if(!block.isTombstone()) {
				if(block.getValue().getContent().get().equals(tag)) {
					block.setTombstone(); return;
				}
			}
		}
	}
	
	public Block insertByOrderId(OrderId orderId, TypeTag value, int location,OrderId id) {
		Optional<OrderId> right; int i = -1; Optional<OrderId> left;
		
		if(location == -1) {
			right = Optional.of(orderId);
			i = right.isPresent() ? indexOf(right.get()) : this.list.size();
			left = (i-1) >= 0 ? Optional.of(this.list.get(i-1).getId()) : Optional.empty();
		}else {
			left = Optional.of(orderId);
			i = left.isPresent() ? indexOf(left.get()) : this.list.size();
			right = (i+1) < this.list.size() ? Optional.of(this.list.get(i+1).getId()) : Optional.empty();
		}
		
		
		long seqNr = this.lastSeqNr + 1L;
		
		
		Key key = value.getKey();
		if(!(key instanceof IndexK)) {
			throw new RuntimeException();
		}
		
		IndexK indexK = (IndexK) key;
		Block newBlock = new Block(id ,left,right,Optional.empty(),new Content(Optional.of(value),Optional.empty()));
		
		integrateInsert(newBlock);
		return newBlock;
	}
	
	
	public Block insert(int index, TypeTag value) {
		Optional<OrderId> right = findPosition(index);
		int i = right.isPresent() ? indexOf(right.get()) : this.list.size();
		long seqNr = this.lastSeqNr + 1L;
		Optional<OrderId> left = (i-1) >= 0 ? Optional.of(this.list.get(i-1).getId()) : Optional.empty();
		
		Key key = value.getKey();
		if(!(key instanceof IndexK)) {
			throw new RuntimeException();
		}
		
		IndexK indexK = (IndexK) key;
		Block newBlock = new Block(new OrderId(indexK.getReplicaId(),seqNr)
				,left,right,Optional.empty(),new Content(Optional.of(value),Optional.empty()));
		
		integrateInsert(newBlock);
		return newBlock;
	}
	
	
	public Block insertByMetaData(BlockMetaData meta, TypeTag tag) {
		Key key = tag.getKey();
		if(!(key instanceof IndexK)) {
			throw new RuntimeException();
		}
		IndexK indexK = (IndexK) key;
		Block newBlock = new Block(new OrderId(indexK.getReplicaId(),meta.getSeqNr())
				,meta.getLeft(),meta.getRight(),Optional.empty(),new Content(Optional.of(tag),Optional.empty()));
		
		integrateInsert(newBlock);
		return newBlock;
	}
	
	public BlockMetaData getInsertMetaData(int index) {
		Optional<OrderId> right = findPosition(index);
		int i = right.isPresent() ? indexOf(right.get()) : this.list.size();
		Optional<OrderId> left = (i-1) >= 0 ? Optional.of(this.list.get(i-1).getId()) : Optional.empty();
		
		BlockMetaData metaData = new BlockMetaData(left,right,this.lastSeqNr+1L);
		
		return metaData;
	}
	
	
	public void moveByKey(TypeTag src, int index) {
		List<TypeTag> orderList = getOrderList();
		int srcIndex = -1; int count = 0;
		for(TypeTag tag : orderList) {
			if(tag.equals(src)) {
				srcIndex = count;
			}
			count +=1;
		}
		moveByIndex(srcIndex,index);
	}
	
	
	public void integrateMoved(Block src ,Block target) {
		Content content = target.getValue();
		if(content.isMoved()) {
			Moved moved1 = content.getMoved().get();
			
			if(!src.getMovedTo().isPresent()) {
				src.setMovedTo(Optional.of(target.getId()));
			}else {
				int targetIndex = indexOf(src.getMovedTo().get());
				Block curTarget = this.list.get(targetIndex);
				
				Content tarContent = curTarget.getValue();
				if(tarContent.isMoved()) {
					Moved moved2 = tarContent.getMoved().get();
					int prio1 = moved1.getPriority(); int prio2 = moved2.getPriority();
					if(prio1 > prio2 || (prio1 == prio2 && target.getId().compareTo(curTarget.getId()) > 0 )) {
						log.info(target.getId().getReplicaId() +" : "+ target.getId().getSeq()+" : "+prio1);
						src.setMovedTo(Optional.of(target.getId()));
					}	
				}	
			}				
		}
	}
	
	public void move(OrderId src, OrderId dst,int priority ,int location,OrderId id) {
		int srcIndex = indexOf(src); int dstIndex = indexOf(dst);
		if(srcIndex == -1 || this.list.get(srcIndex).isTombstone()) {
			return;
		}
		else {
			if(dstIndex == -1) {
				return;
			}else {
				Block srcBlock = this.list.get(srcIndex);
				TypeTag value = srcBlock.getValue().getContent().get();
				Block moved = insertByOrderId(dst,value,location,id);
				
				moved.setMovedTo(Optional.empty());
				moved.getValue().setMoved(Optional.of(new Moved(srcBlock.getId(),priority)));
				
				integrateMoved(srcBlock,moved);
			}
			
		}
		
	}
	
	
	public void moveByIndex(int src, int target) {
	
		Optional<OrderId> srcId = findPosition(src);
		int i = srcId.isPresent() ? indexOf(srcId.get()) : -1;
		Block srcBlock = this.list.get(i);
		
		if(srcBlock.getValue().isTombstone()) {
			return;
		}else {
			Block moved = insert(target,srcBlock.getValue().getContent().get());
			int prio = 0;
			if(srcBlock.getMovedTo().isPresent()) {
				int moveToIndex = indexOf(srcBlock.getMovedTo().get());
				Block moveToBlock = this.list.get(moveToIndex);
				prio = moveToBlock.getValue().getMoved().get().getPriority() + 1;
			}
			
			moved.setMovedTo(Optional.empty());
			moved.getValue().setMoved(Optional.of(new Moved(srcBlock.getId(),prio)));
			
			integrateMoved(srcBlock,moved);
		}
	}
	
	
	public void integrateInsert(Block block) {
		OrderId id = block.getId();
		this.lastSeqNr = Long.max(lastSeqNr,id.getSeq());
		int index = findInsertIndex(block);
		this.list.add(index, block);
	}
	
	
}
