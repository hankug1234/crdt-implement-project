package com.crdt.implement.opBaseCrdt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;

import com.crdt.implement.reliableBroadcast.OpBaseEvent;
import com.crdt.implement.vectorClock.Ord;
import com.crdt.implement.vectorClock.VectorClock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@Data
public class ReplicationState <S> {
	private final String replicaId;
	private boolean isDirty;
	private long seqNr;
	private final VectorClock vectorClock;
	private final Map<String,Long> observed;
	private S crdt;
	
	public ReplicationState(String replicaId,S crdt) {
		this.replicaId = replicaId;
		this.isDirty = false;
		this.seqNr = 0L;
		this.vectorClock = new VectorClock();
		this.observed = new HashMap<>();
		this.crdt = crdt;
	}
	
	public ReplicationState() {
		this.replicaId = null; this.vectorClock = null; this.observed = null;
	}
	
	public ReplicationState<S> clone(S newCrdt){
		return new ReplicationState<S>(replicaId,false,seqNr,vectorClock.clone(),new HashMap<>(observed),newCrdt);
	}
	
	public boolean isDirty() {
		return this.isDirty;
	}
	
	public <E> boolean unseen(OpBaseEvent<E> event) {
		long observedSeqNr = this.observed.getOrDefault(event.getOriginReplicaId(),0L);
		if(observedSeqNr >= event.getOriginSeqNr()) return false;
		else return event.getVectorClock().compareTo(this.vectorClock) > Ord.Eq.getValue(); 
	}
}
