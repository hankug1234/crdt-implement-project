package com.crdt.implement.opBaseCrdt.RGA;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.crdt.implement.opBaseCrdt.OpBaseCrdtOperation;

public class OpBaseRgaOperation<A> implements OpBaseCrdtOperation<RgaState<A>,List<A>,RgaCommand<A>,RgaData<A>>{
	
	private String replicaId;
	
	public OpBaseRgaOperation(String replicaId) {
		this.replicaId = replicaId;
	}
	
	@Override
	public RgaState<A> Default() {
		// TODO Auto-generated method stub
		return new RgaState<A>(this.replicaId);
	}

	@Override
	public List<A> Query(RgaState<A> crdt) {
		// TODO Auto-generated method stub
		return crdt.getState().stream().filter(v->!v.isTumbstone()).map(v->v.getValue().get()).collect(Collectors.toList());
	}

	@Override
	public RgaData<A> Prepare(RgaState<A> crdt, RgaCommand<A> command) {
		// TODO Auto-generated method stub
		if(command instanceof Command.Insert) {
			Command.Insert<A> insert = (Command.Insert<A>) command;
			int index = insert.getIndex(); A value = insert.getValue();
			return createInserted(index,value,crdt.getState(),crdt.getSequencer());
		}else {
			Command.RemoveAt<A> removeAt = (Command.RemoveAt<A>) command; 
			int index = removeAt.getIndex();
			return createRemoved(index,crdt.getState());
			
		}
	}

	@Override
	public RgaState<A> Effect(RgaState<A> crdt, RgaData<A> event) {
		
		if(event instanceof Data.Inserted<A>) {
			Data.Inserted<A> inserted = (Data.Inserted<A>) event;
			RgaVPtr predecessor = inserted.getAfter();
			RgaVPtr at = inserted.getAt();
			A value = inserted.getValue();
			
			return applyInserted(predecessor,at,value,crdt);
		}else {
			
			Data.Removed<A> removed = (Data.Removed<A>) event;
			RgaVPtr at = removed.getAt();
			
			return applyRemoved(at,crdt);
		}
	}

	@Override
	public RgaState<A> copy(RgaState<A> crdt) {
		// TODO Auto-generated method stub
		List<Vertex<A>> state = crdt.getState();
		RgaVPtr sequencer = crdt.getSequencer();
		
		RgaVPtr newSequencer = new RgaVPtr(sequencer.getReplicaId(),sequencer.getIndex());
		List<Vertex<A>> newState = new ArrayList<>();
		for(Vertex<A> v : state) {
			newState.add(v.clone());
		}
		
		return new RgaState<A>(newState,newSequencer);
	}
	
	//tombstone 들을 포함한 실제 데이터의 index // 세그먼 테이션 활용 하면 더 빠르게 가능 -> 차후 수정 할 것 
	public static <A> int indexWithTombstones(int index, List<Vertex<A>> crdt) {
		int offset = 1;
		
		while(offset < crdt.size()) {
			if(crdt.get(offset).isTumbstone()) {
				offset+=1;
			}else {
				index-=1;
				if(index < 0) {
					return offset;
				}
				offset+=1;
			}
		}
		return offset;
	}
	
	//vptr 이 같은 것을 찾음 그리고 해당 위치를 반환 // 이진 트리로 
	public static <A> int indexOfVPtr(RgaVPtr vptr ,List<Vertex<A>> crdt) {
		int offset = 0;
		while(!vptr.equals(crdt.get(offset).getVptr())) {
			offset+=1;
		}
		return offset;
	}
	
	// offset 위치의 데이터가 내 포인터 보다 작으면 해당 위치에 삽입 아니면 한칸 미룸
	public static <A> int shift(int offset,RgaVPtr vptr ,List<Vertex<A>> crdt) {
		while(offset < crdt.size()){
			RgaVPtr next = crdt.get(offset).getVptr();
			if(next.compareTo(vptr) < 0) {
				return offset;
			}else {
				offset+=1;
			}
		}
		return offset;
	}
	
	public static RgaVPtr nextSeqNr(RgaVPtr vptr) {
		return new RgaVPtr(vptr.getReplicaId(),vptr.getIndex()+1);
	}
	
	public static <A> Data.Inserted<A> createInserted(int index, A value,List<Vertex<A>> crdt, RgaVPtr sequencer){
		int realIndex = indexWithTombstones(index,crdt);// tombstone을 고려한 실제 위치 
		RgaVPtr prev = crdt.get(realIndex-1).getVptr(); // 내 현재 위치 왼쪽 포인터 값 
		RgaVPtr at =  nextSeqNr(sequencer);// 새 포인터 번호 
		return new Data.Inserted<A>(prev, at, value);
	}
	
	public static <A> Data.Removed<A> createRemoved(int index,List<Vertex<A>> crdt){
		int realIndex = indexWithTombstones(index,crdt);
		RgaVPtr at = crdt.get(realIndex).getVptr();
		return new Data.Removed<>(at);
	}
	
	public static <A> RgaState<A> applyInserted(RgaVPtr predecessor, RgaVPtr vptr, A value, RgaState<A> rga){
		List<Vertex<A>> crdt = rga.getState(); RgaVPtr sequencer = rga.getSequencer();
		
		int predecessorIndex = indexOfVPtr(predecessor,crdt); // 내가 삭입한 시전에서 바로 왼쪽 포인터 인덱스 획득
		int insertIndex = shift(predecessorIndex+1,vptr,crdt); // 바로 왼쪽 포인터 부터 내가 오른쪽 포인터 보다 클 때 까지 오른쪽 으로 이동 
		RgaVPtr nextSequnecer = new RgaVPtr(sequencer.getReplicaId(),Integer.max(sequencer.getIndex(),vptr.getIndex()));
		crdt.add(insertIndex,new Vertex<>(vptr,value));
		rga.setSequencer(nextSequnecer);
		return rga;
		
	}
	
	public static <A> RgaState<A> applyRemoved(RgaVPtr vptr,RgaState<A> rga){
		List<Vertex<A>> crdt = rga.getState();
		int realIndex = indexOfVPtr(vptr,crdt);
		crdt.get(realIndex).setTumbstone();
		return rga;
	}

}
