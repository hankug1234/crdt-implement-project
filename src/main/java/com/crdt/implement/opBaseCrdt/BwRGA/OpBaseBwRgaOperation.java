package com.crdt.implement.opBaseCrdt.BwRGA;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.crdt.implement.opBaseCrdt.OpBaseCrdtOperation;
import com.crdt.implement.reliableBroadcast.OpBaseEvent;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class OpBaseBwRgaOperation<A> implements OpBaseCrdtOperation<BwRgaState<A>,List<Block<A>>,BwRgaCommand<A>,BwRgaData<A>>{

	private String replicaId;
	
	public OpBaseBwRgaOperation(String replicaId) {
		this.replicaId = replicaId;
	}
	
	@Override
	public BwRgaState<A> Default() {
		// TODO Auto-generated method stub
		return new BwRgaState<A>(this.replicaId);
	}

	@Override
	public List<Block<A>> Query(BwRgaState<A> crdt) {
		// TODO Auto-generated method stub
		return crdt.getBlocks().stream().filter(b->!b.isTombstone()).collect(Collectors.toList());
	}

	@Override
	public BwRgaData<A> Prepare(BwRgaState<A> crdt, BwRgaCommand<A> command) {
		// TODO Auto-generated method stub
		if(command instanceof Command.Insert) {
			Command.Insert<A> insert = (Command.Insert<A>) command;
			return createInserted(insert.getIndex(),insert.getValues(),crdt);
			
		}else {
			Command.RemoveAt<A> removeAt = (Command.RemoveAt<A>) command;
			return createRemoved(removeAt.getIndex(),removeAt.getCount(),crdt.getBlocks());
		}
	}

	@Override
	public BwRgaState<A> Effect(BwRgaState<A> crdt, OpBaseEvent<BwRgaData<A>> event) {
		// TODO Auto-generated method stub
		
		BwRgaState<A> result = null;
		BwRgaData<A> data = event.getData();
		
		if(data instanceof Data.Inserted) {
			
			Data.Inserted<A> inserted = (Data.Inserted<A>) data;
			log.info(replicaId+" : after -> "+inserted.getAfter().toString()+" / at -> "+inserted.getAt().toString());
			result = applyInserted(inserted.getAfter(), inserted.getAt(), inserted.getValues(), crdt);
			
		}else {
			
			Data.Removed<A> removed = (Data.Removed<A>) data;
			result = applyRemoved(removed.getRemoveds(), crdt);
		}
		
		return result;
	}

	@Override
	public BwRgaState<A> copy(BwRgaState<A> crdt) {
		// TODO Auto-generated method stub
		return crdt.clone();
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
					continue;
				}else {
					int remain = index - consumed;
					if(remain <= block.length()) {
						return new int[] {currentIndex,remain};
					}else {
						consumed += block.length();
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
			
			if(block.isTombstone()) {
				continue;
			}
			
			BwRgaVPtrOff vptrOff = block.getVptrOff();
			BwRgaVPtrOff newOff = new BwRgaVPtrOff(vptrOff.getVptr(),vptrOff.getOffset() + offset);
			int len = block.length() - offset;
			
			// 지워야 하는 블록이 범위 내에 있는지 평가 
			if(len > count) {
				removeds.add(new Data.RemovedRange(newOff, count));
				return new Data.Removed<>(removeds);
			}else if(len == 0) {
				offset = 0;
			}
			else {
				removeds.add(new Data.RemovedRange(newOff, len));
				count-=len; offset = 0;
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
								continue;
							}else {
								
								if(length <= tombstone.length()) {
									Block<A>[] split = block.split(length);
									
									split[0].setTombstone();
									newBlocks.add(split[0]);
									
									if(split[1] != null) {
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
			
			
			for(int i=index; i<blocks.size(); i++) {
				newBlocks.add(blocks.get(i));
			}
			
			return new BwRgaState<A>(rga.getSequencer(),newBlocks);
		}
	}
	
	
	public static <A> Data.Inserted<A> createInserted(int index, List<A> values, BwRgaState<A> rga){
		int[] indexAndOffset = findByIndex(index,rga.getBlocks());
		BwRgaVPtrOff vptrOff = rga.getBlocks().get(indexAndOffset[0]).getVptrOff();
		BwRgaVPtr at = nextSeqNr(rga.getSequencer());
		BwRgaVPtrOff after = new BwRgaVPtrOff(vptrOff.getVptr(),indexAndOffset[1] + vptrOff.getOffset()); 
		log.info("after : "+after.toString()+", value : "+values.toString());
		return new Data.Inserted<>(after,at, values);
		
	}
	
	public static <A> BwRgaState<A> applyInserted(BwRgaVPtrOff predecessor, BwRgaVPtr at, List<A> values, BwRgaState<A> rga){
		int[] indexAndBlockIndex = findByPositionOffset(predecessor,rga.getBlocks());
		Block<A> block = rga.getBlocks().get(indexAndBlockIndex[0]);
		Block<A> newBlock = new Block<A>(new BwRgaVPtrOff(at,0),new Content<A>(values));
		Block<A>[] split = block.split(indexAndBlockIndex[1]);
		rga.getBlocks().remove(indexAndBlockIndex[0]);
		rga.getBlocks().add(indexAndBlockIndex[0], split[0]);
		
		int indexAdjust = shift(indexAndBlockIndex[0]+1,at,rga.getBlocks());
		rga.getBlocks().add(indexAdjust, newBlock);
		
		if(split[1] != null) {
			rga.getBlocks().add(indexAdjust+1,split[1]);
		}
		BwRgaVPtr sequencer = rga.getSequencer();
		BwRgaVPtr nextSeqNr = new BwRgaVPtr(Integer.max(sequencer.getIndex(),at.getIndex()),sequencer.getReplicaId());
		
		return new BwRgaState<A>(nextSeqNr,rga.getBlocks());
	}

}
