package com.crdt.implement.opBaseCrdt.document.node.ordering;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.crdt.implement.opBaseCrdt.document.Id;
import com.crdt.implement.opBaseCrdt.document.keyType.IndexK;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OrderList {
	private List<Block> list;
	private long lastSeqNr;
	
	public OrderList() {
		this.list = new ArrayList<>();
		this.lastSeqNr = 0;
	}
	
	public List<IndexK> getOrderList(){
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
						if(from.equals(movedTo) && !target.isTombstone()) {
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
	
	public void integrateMoved(Block block) {
		Content content = block.getValue();
		if(content.isMoved()) {
			Moved moved1 = content.getMoved().get();
			int fromIndex = indexOf(moved1.getFrom());
			if(fromIndex != -1) {
				
				Block src = this.list.get(fromIndex);
				if(!src.getMovedTo().isPresent()) {
					src.setMovedTo(Optional.of(block.getId()));
				}else {
					int targetIndex = indexOf(src.getMovedTo().get());
					Block target = this.list.get(targetIndex);
					
					Content tarContent = target.getValue();
					if(tarContent.isMoved()) {
						Moved moved2 = tarContent.getMoved().get();
						int prio1 = moved1.getPriority(); int prio2 = moved2.getPriority();
						if(prio1 > prio2 || (prio1 == prio2 && block.getId().compareTo(target.getId()) > 0 )) {
							src.setMovedTo(Optional.of(block.getId()));
						}	
					}	
				}	
			}
		}
	}
	
	public void delete(int index) {
		Optional<OrderId> target = findPosition(index);
		int i = target.isPresent() ? indexOf(target.get()) : -1;
		Block block = this.list.get(i);
		block.setTombstone();
	}
	
	public Block insert(int index, IndexK value) {
		Optional<OrderId> right = findPosition(index);
		int i = right.isPresent() ? indexOf(right.get()) : this.list.size();
		long seqNr = this.lastSeqNr + 1L;
		Optional<OrderId> left = (i-1) >= 0 ? Optional.of(this.list.get(i-1).getId()) : Optional.empty();
		
		
		
		Block newBlock = new Block(new OrderId(value.getReplicaId(),seqNr)
				,left,right,Optional.empty(),new Content(Optional.of(value),Optional.empty()));
		
		integrateInsert(newBlock);
		return newBlock;
	}
	
	public void moveByKey(IndexK src, IndexK target) {
		List<IndexK> orderList = getOrderList();
		int srcIndex = -1; int targetIndex = -1; int count = 0;
		for(IndexK index : orderList) {
			if(index.equals(src)) {
				srcIndex = count;
			}
			
			if(index.equals(target)) {
				targetIndex = count;
			}
			count +=1;
		}
		moveByIndex(srcIndex,targetIndex);
	}
	
	public void moveByIndex(int src, int target) {
		Block moved = insert(target,null);
		Optional<OrderId> srcId = findPosition(src);
		int i = srcId.isPresent() ? indexOf(srcId.get()) : -1;
		Block srcBlock = this.list.get(i);
		
		int prio = 0;
		if(srcBlock.getMovedTo().isPresent()) {
			int moveToIndex = indexOf(srcBlock.getMovedTo().get());
			Block moveToBlock = this.list.get(moveToIndex);
			prio = moveToBlock.getValue().getMoved().get().getPriority() + 1;
		}
		
		srcBlock.setMovedTo(Optional.empty());
		srcBlock.getValue().setMoved(Optional.of(new Moved(srcBlock.getId(),prio)));
		
		integrateMoved(moved);
	}
	
	public void integrateInsert(Block block) {
		OrderId id = block.getId();
		if(this.lastSeqNr == id.getSeq()-1L) {
			this.lastSeqNr = Long.max(lastSeqNr,id.getSeq());
			int index = findInsertIndex(block);
			this.list.add(index, block);
		}else {
			throw new RuntimeException();
		}
	}
	
	
}
