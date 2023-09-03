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
		this.metrixTime.merge(String.valueOf(replicaId), vectorClock.clone(), (a,b)->b);
	}
}
