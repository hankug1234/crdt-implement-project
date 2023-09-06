package com.crdt.implement.vectorClock;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MetrixTime {
	Map<String,VectorClock> metrixTime;
	
	public MetrixTime() {
		metrixTime = new HashMap<>();
	}
	
	public MetrixTime(Map<String,VectorClock> metrixTime) {
		this.metrixTime = new HashMap<>();
		Set<String> keys = metrixTime.keySet();
		for(String key : keys) {
			this.metrixTime.put(String.valueOf(key), metrixTime.get(key).clone());
		}
	}
	
	public void removeVectorClock(String replicaId) {
		this.metrixTime.remove(replicaId);
	}
	
	public VectorClock getVectorClock(String replicaId) {
		return this.metrixTime.getOrDefault(replicaId, new VectorClock());
	}
	
	public MetrixTime clone() {
		return new MetrixTime(this.metrixTime);
	}
	
	public VectorClock min() {
		VectorClock foldedMetrix = new VectorClock();
		this.metrixTime.entrySet().stream().forEach(e->foldedMetrix.intersaction(e.getValue()));
		return foldedMetrix;
	}
	
	public VectorClock max() {
		VectorClock foldedMetrix = new VectorClock();
		this.metrixTime.entrySet().stream().forEach(e->foldedMetrix.merge(e.getValue()));
		return foldedMetrix;
	}
	
	public Set<Entry<String,VectorClock>> getVectorClocks(){
		return this.metrixTime.entrySet();
	}
	
	public void merge(MetrixTime otherMetrixTime) {
		otherMetrixTime.getVectorClocks().stream().forEach(e->{
			this.metrixTime.merge(e.getKey(),e.getValue().clone(),(a,b)->{a.merge(b); return a;});
		});
	}
	
	public void update(String replicaId,VectorClock vectorClock) {
		this.metrixTime.merge(String.valueOf(replicaId), vectorClock.clone(), (a,b)->{a.merge(b); return a;});
	}
}
