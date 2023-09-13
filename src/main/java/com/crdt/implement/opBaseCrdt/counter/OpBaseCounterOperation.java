package com.crdt.implement.opBaseCrdt.counter;

import com.crdt.implement.opBaseCrdt.OpBaseCrdtOperation;

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
	
	public Long Effect(Long crdt, Long event) {
		return crdt+event;
	}
	
	public Long copy(Long crdt) {
		return Long.valueOf(crdt.longValue());
	}
}
