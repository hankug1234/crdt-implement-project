package com.crdt.implement.opBaseCrdt.LSeq;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.crdt.implement.opBaseCrdt.OpBaseCrdtOperation;
import com.crdt.implement.reliableBroadcast.OpBaseEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpBaseLSeqOperation<A> implements OpBaseCrdtOperation<List<Vertex<A>>,List<A>,LSeqCommand<A>,LSeqData<A>>{

	@Override
	public List<Vertex<A>> Default() {
		// TODO Auto-generated method stub
		return new ArrayList<Vertex<A>>();
	}

	@Override
	public List<A> Query(List<Vertex<A>> crdt) {
		return crdt.stream().map(v->v.getData()).collect(Collectors.toList());
	}

	@Override
	public LSeqData<A> Prepare(List<Vertex<A>> crdt, LSeqCommand<A> command) {
		
		if(command instanceof Command.Insert) {
			
			Command.Insert<A> insert = (Command.Insert<A>) command;
			int index = insert.getIndex(); A value = insert.getValue(); String replicaId = insert.getReplicaId();
			
			if(0> index || index > crdt.size()) {
				return new Data.OutofBound<A>();
			}
			
			List<Byte> left = index == 0 ? new ArrayList<>() : crdt.get(index-1).getVptr().getSequence();
			List<Byte> right = index == crdt.size() ? new ArrayList<>() : crdt.get(index).getVptr().getSequence();
			
			LSeqVPtr vptr = new LSeqVPtr(replicaId,generateSeq(left, right));
			
			log.info("insert " + vptr.toString());
			
			return new Data.Inserted<A>(vptr, value); 	
		}else {
			Command.RemoveAt<A> removeAt = (Command.RemoveAt<A>) command;
			int index = removeAt.getIndex(); String replicaId = removeAt.getReplicaId();
			
			if(0> index || index > crdt.size()) {
				return new Data.OutofBound<A>();
			}
			
			List<Byte> left = index == 0 ? new ArrayList<>() : crdt.get(index-1).getVptr().getSequence();
			List<Byte> right = index == crdt.size() ? new ArrayList<>() : crdt.get(index).getVptr().getSequence();
			
			LSeqVPtr vptr = new LSeqVPtr(replicaId,generateSeq(left, right));
			
			log.info("delete " + vptr.toString());
			
			return new Data.Removed<>(vptr);
		}
		
	}

	@Override
	public List<Vertex<A>> Effect(List<Vertex<A>> crdt, OpBaseEvent<LSeqData<A>> event) {
		// TODO Auto-generated method stub
		
		LSeqData<A> data = event.getData();
		
		if( data instanceof Data.Inserted) {
			Data.Inserted<A> inserted = (Data.Inserted<A>) data;
			LSeqVPtr vptr = inserted.getVptr(); A value = inserted.getValue();
			int index = binarySearch(crdt,vptr);
			crdt.add(index,new Vertex(vptr,value));
		}else if(data instanceof Data.Removed){
			Data.Removed<A> removed = (Data.Removed<A>) data;
			LSeqVPtr vptr = removed.getVptr();
			int index = binarySearch(crdt,vptr);
			crdt.remove(index);
		}
		
		return crdt;
	}

	@Override
	public List<Vertex<A>> copy(List<Vertex<A>> crdt) {
		// TODO Auto-generated method stub
		
		List<Vertex<A>> result = new ArrayList<>();
		result.addAll(crdt);
		
		return result;
	}
	
	public static <A> int binarySearch(List<Vertex<A>> crdt,LSeqVPtr vptr) {
		
	   int start =0; int end = crdt.size(); int mid = 0;
	   while(start < end) {
		   mid = (start + end)/2;
		   if(crdt.get(mid).getVptr().compareTo(vptr) >= 0) {
			   end = mid;
		   }else {
			   start = mid+1;
		   }
	   }
		
		return start;
	}
	
	
	public static List<Byte> generateSeq(List<Byte> lo, List<Byte> hi) {
		List<Byte> result = new ArrayList<>();
		int i = 0; Byte min; Byte max; int end = Integer.max(lo.size(),hi.size());
		while(i<=end) {
			min = i >= lo.size() ? Byte.valueOf((byte) 128) : Byte.valueOf(lo.get(i));
			max = i >= hi.size() ? Byte.valueOf((byte) 127) : Byte.valueOf(hi.get(i));
			
			Byte newByte = Byte.valueOf((byte) (min + (byte) 1));
			
			if(newByte.compareTo(max) < 0) {
				result.add(newByte);
				return result;
			}else {
				result.add(min); i+=1;
			}
		}
		return result;
	}

}
