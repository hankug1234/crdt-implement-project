package com.crdt.implement.opBaseCrdt.counter;

import com.crdt.implement.opBaseCrdt.OpBaseCrdtOperation;
import com.crdt.implement.reliableBroadcast.OpBaseEvent;

public class OpBaseCounterOperation implements OpBaseCrdtOperation<Long,Long,Long,Long>{
	public Long Default() {
		return 0L;
	}
	
	public Long Query(Long crdt) {
		return crdt;
	}
	
	public Long Prepare(Long crdt, Long command) {
		return command;
	}
	
	public Long Effect(Long crdt, OpBaseEvent<Long> event) {
		Long data = event.getData();
		return crdt+data;
	}
	
	public Long copy(Long crdt) {
		return Long.valueOf(crdt.longValue());
	}
}
