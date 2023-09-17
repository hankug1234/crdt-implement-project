package com.crdt.implement.opBaseCrdt.BwRGA;

import java.util.ArrayList;
import java.util.List;

import com.crdt.implement.opBaseCrdt.OpBaseCrdtOperation;

public class OpBaseBwRgaOperation<A> implements OpBaseCrdtOperation<BwRgaState<A>,List<A>,BwRgaCommand<A>,BwRgaData<A>>{

	private String replicaId;
	
	public OpBaseBwRgaOperation(String replicaId) {
		this.replicaId = replicaId;
	}
	
	
	@Override
	public BwRgaState<A> Default() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<A> Query(BwRgaState<A> crdt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BwRgaData<A> Prepare(BwRgaState<A> crdt, BwRgaCommand<A> command) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BwRgaState<A> Effect(BwRgaState<A> crdt, BwRgaData<A> event) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BwRgaState<A> copy(BwRgaState<A> crdt) {
		// TODO Auto-generated method stub
		return null;
	}
	
	//주어진 인덱스 번호가 실제 데이터 블록 상에서 {indexnum,inner offset} 을 반환 
	public static <A> int[] findByIndex(int index,List<Block<A>> blocks) {
		int consumed = 0;
		for(int currentIndex =0; currentIndex < blocks.size(); currentIndex ++) {
			if(index == consumed) {
				return new int[] {currentIndex,0};
			}else {
				Block<A> block = blocks.get(currentIndex);
				if(block.isTombstone()) {
					currentIndex+=1;
				}else {
					int remain = index - consumed;
					if(remain <= block.length()) {
						return new int[] {currentIndex,remain};
					}else {
						currentIndex+=1; consumed += block.length();
					}
				}
			}
		}
		
		return null;
	}
	
	public static <A> int[] findByPositionOffset(BwRgaVPtrOff p, List<Block<A>> blocks) { 
		for(int index =0; index < blocks.size(); index++) {
			Block<A> block = blocks.get(index);
			if(block.getVptrOff().getVptr().equals(p.getVptr())) {
				if(block.containOffset(p.getOffset())) {
					return new int[] {index,p.getOffset() - block.getVptrOff().getOffset()};
				}
			}
		}		
		return null;
	}
	
	public static <A> int shift(int offset, BwRgaVPtr vptr, List<Block<A>> blocks) {
		for(int i = offset; i< blocks.size(); i++) {
			BwRgaVPtr next = blocks.get(i).getVptrOff().getVptr();
			if(next.compareTo(vptr) < 0) {
				return offset;
			}
		}
		return blocks.size();
	}
	
	public static BwRgaVPtr nextSeqNr(BwRgaVPtr vptr) {
		return new BwRgaVPtr(vptr.getIndex()+1,vptr.getReplicaId());
	}
	
	public static<A> Data.Removed<A> createRemoved(int index, int count, List<Block<A>> blocks){
		int[] indexAndOffset = findByIndex(index,blocks);
		int idx = indexAndOffset[0]; int offset = indexAndOffset[1];
		List<Data.RemovedRange> removeds = new ArrayList<>();
		for(int i=idx; i<blocks.size(); i++) {
			Block<A> block = blocks.get(i);
			BwRgaVPtrOff vptrOff = block.getVptrOff();
			BwRgaVPtrOff newOff = new BwRgaVPtrOff(vptrOff.getVptr(),vptrOff.getOffset() + offset);
			int len = block.length() - offset;
			
			// 지워야 하는 블록이 범위 내에 있는지 평가 
			if(len > count) {
				removeds.add(new Data.RemovedRange(newOff, count));
				break;
			}else if(len == 0) {
				offset = 0;
			}else {
				removeds.add(new Data.RemovedRange(newOff, len));
				count-=len;
			}
			
		}
		return new Data.Removed<>(removeds);
	}
	
	
	
	public static <A> BwRgaState<A> applyRemoved(List<Data.RemovedRange> removeds, BwRgaState<A> rga){
		if(removeds.isEmpty()) {
			return rga;
		}else {
			
			int index = 1; List<Block<A>> newBlocks = new ArrayList<>(); List<Block<A>> blocks = rga.getBlocks();
			
			for(Data.RemovedRange removed : removeds) {
				BwRgaVPtrOff vptrOff = removed.getStartOff();
				int length = removed.getRange();
				
				while(length > 0) {
					Block<A> block = blocks.get(index);
					if(block.getVptrOff().getVptr().equals(vptrOff.getVptr())) {
						if(block.containOffset(vptrOff.getOffset())) {
							
							int splitIndex = vptrOff.getOffset() - block.getVptrOff().getOffset();
							
							Block<A> tombstone;
							
							if(splitIndex == 0) {
								tombstone = block;
							}else {
								Block<A>[] split = block.split(splitIndex);
								newBlocks.add(split[0]);
								tombstone = split[1];
							}
							
							if(tombstone == null) {
								return rga;
							}else {
								
								if(length <= tombstone.length()) {
									Block<A>[] split = block.split(splitIndex);
									newBlocks.add(split[0]);
									if(split[1] != null) {
										split[1].setTombstone();
										newBlocks.add(split[1]);
									}
									index+=1; length = 0;
								}else {
									tombstone.setTombstone();
									newBlocks.add(tombstone);
									length -= tombstone.length();
									index+=1;
								}
								
							}
							
						}else {
							newBlocks.add(block);
							index+=1;
						}
						
					}else {
						newBlocks.add(block);
						index+=1;
					}
					
				}
			}
		}
		
		return rga;
	}
	
	
	public static <A> Data.Inserted<A> createInserted(int index, List<A> values, BwRgaState<A> rga){
		int[] indexAndOffset = findByIndex(index,rga.getBlocks());
		BwRgaVPtrOff vptrOff = rga.getBlocks().get(indexAndOffset[0]).getVptrOff();
		BwRgaVPtr at = nextSeqNr(rga.getSequencer());
		BwRgaVPtrOff after = new BwRgaVPtrOff(vptrOff.getVptr(),indexAndOffset[1] + vptrOff.getOffset()); 
		return new Data.Inserted<>(after,at, values);
		
	}
	
	public static <A> BwRgaState<A> applyInserted(BwRgaVPtrOff predecessor, BwRgaVPtr at, List<A> values, BwRgaState<A> rga){
		int[] indexAndBlockIndex = findByPositionOffset(predecessor,rga.getBlocks());
		int indexAdjust = shift(indexAndBlockIndex[0]+1,at,rga.getBlocks());
		Block<A> block = rga.getBlocks().get(indexAndBlockIndex[0]);
		Block<A> newBlock = new Block<A>(new BwRgaVPtrOff(at,0),new Content<A>(values));
		Block<A>[] split = block.split(indexAndBlockIndex[1]);
		rga.getBlocks().remove(indexAndBlockIndex[0]);
		rga.getBlocks().add(indexAndBlockIndex[0], split[0]);
		rga.getBlocks().add(indexAdjust, newBlock);
		
		if(split[1] != null) {
			rga.getBlocks().add(indexAdjust+1,split[1]);
		}
		BwRgaVPtr sequencer = rga.getSequencer();
		BwRgaVPtr nextSeqNr = new BwRgaVPtr(Integer.max(sequencer.getIndex(),at.getIndex()),sequencer.getReplicaId());
		
		return new BwRgaState<A>(nextSeqNr,rga.getBlocks());
	}

}
