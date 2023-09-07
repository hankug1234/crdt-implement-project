package com.crdt.implement.PureOpBaseCrdt;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import com.crdt.implement.taggedStableCusalBroadcast.PureOpBaseEvent;

public class PureOpBaseCounterOperation implements PureCrdtOperation<Long,Long,Long>{

	@Override
	public Long Default() {
		// TODO Auto-generated method stub
		return 0L;
	}

	@Override
	public boolean Obsoletes(PureOpBaseEvent<Long> e1, PureOpBaseEvent<Long> e2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Long Apply(Long crdt, Set<PureOpBaseEvent<Long>> ops) {
		return ops.stream().sorted(Comparator.comparing(e->e)).map(e->e.getOperation()).reduce(crdt,(a,b)->a+b);
	}

	@Override
	public Long Query(Long crdt, Set<PureOpBaseEvent<Long>> ops) {
		// TODO Auto-generated method stub
		return ops.stream().sorted(Comparator.comparing(e->e)).map(e->e.getOperation()).reduce(crdt,(a,b)->a+b);
	}

	@Override
	public Long Copy(Long source) {
		// TODO Auto-generated method stub
		return Long.valueOf(source);
	}

	

}
