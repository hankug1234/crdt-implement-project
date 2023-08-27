package com.crdt.implement.vectorClock;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class VectorClock {
	private Map<String,Long> vectorClock;
	
	public Long upsertClock(String replicaId,long logicalTime,BiFunction<Long,Long,Long> f) {
		return vectorClock.merge(replicaId, logicalTime, f);
	}
	
	public VectorClock() {
		this.vectorClock = new HashMap<>();
	}
	
	
}
