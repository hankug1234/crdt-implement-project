package com.crdt.implement.vectorClock;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class VectorClock implements Comparable<VectorClock> {
	private Map<String,Long> vectorClock;
	
	
	public VectorClock() {
		this.vectorClock = new HashMap<>();
	}
	
	public VectorClock(Map<String,Long> vectorClock) {
		this.vectorClock = new HashMap<>();
		for(Entry<String,Long> e : vectorClock.entrySet()) {
			this.vectorClock.put(String.valueOf(e.getKey()), Long.valueOf(e.getValue()));
		}
	}
	
	public VectorClock clone() {
		return new VectorClock(this.vectorClock);
	}
	
	private void upsertClock(String replicaId,long logicalTime,BiFunction<Long,Long,Long> f) {
		vectorClock.merge(replicaId, Long.valueOf(logicalTime), f);
	}
	
	public void inc(String replicaId) {
		this.upsertClock(replicaId, 1L, (a,b)->a+b);
	}
	
	public void setLogicalTime(String replicaId, long logicalTime) {
		this.vectorClock.put(replicaId, Long.valueOf(logicalTime));
	}
	
	public long getLogicalTime(String replicaId) {
		return this.vectorClock.getOrDefault(replicaId, 0L);
	}
	
	public Set<Entry<String,Long>> getLogicalTimes(){
		return this.vectorClock.entrySet();
	}
	
	public Set<String> getReplicaIds(){
		return this.vectorClock.keySet();
	} 
	
	public void merge(VectorClock otherVectorClock) {
		otherVectorClock.getLogicalTimes().stream().forEach((Entry<String,Long> e)->{
			String replicaId = e.getKey(); Long logicalTime = e.getValue();
			this.vectorClock.merge(replicaId, Long.valueOf(logicalTime), (a,b)->Long.max(a,b));
		});
	}
	
	public void intersaction(VectorClock otherVectorClock) {
		Set<String> replicaIds = new HashSet<>();
		replicaIds.addAll(otherVectorClock.getReplicaIds());
		replicaIds.addAll(this.getReplicaIds());
		
		for(String id : replicaIds) {
			long a = otherVectorClock.getLogicalTime(id);
			long b = this.getLogicalTime(id);
			this.vectorClock.put(id, Long.min(a,b));
		}
	}
	
	@Override
	public String toString() {
		return this.vectorClock.entrySet().stream()
		.sorted(Comparator.comparing((Map.Entry<String,Long> e)->{return e.getKey();})).map(e->e.getKey()+" : "+e.getValue().toString()+" ").collect(Collectors.joining());
	}
	
	@Override
	public int compareTo(VectorClock otherVectorClock) {
		Set<String> replicaIds = new HashSet<>();
		replicaIds.addAll(otherVectorClock.getReplicaIds());
		replicaIds.addAll(this.getReplicaIds());
		
		return replicaIds.stream().map((String r)->{
			long own = this.getLogicalTime(r); long other = otherVectorClock.getLogicalTime(r);
			if(own == other) return Ord.Eq;
			else if(own < other) return Ord.Lt;
			else return Ord.Gt;
		}).reduce(Ord.Eq,(past,cur)->{
			if(past == Ord.Eq && cur == Ord.Gt) return Ord.Gt;
			else if(past == Ord.Eq && cur == Ord.Lt) return Ord.Lt;
			else if(past == Ord.Gt && cur == Ord.Lt) return Ord.Cc;
			else if(past == Ord.Lt && cur == Ord.Gt) return Ord.Cc;
			else return past;
		}).getValue();
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof VectorClock) {
			VectorClock clock = (VectorClock) o;
			if(clock.compareTo(this) == 0) {
				return true;
			}
			return false;
		}
		return false;
	}
}
