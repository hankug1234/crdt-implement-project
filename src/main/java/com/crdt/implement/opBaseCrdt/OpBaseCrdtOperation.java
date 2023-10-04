package com.crdt.implement.opBaseCrdt;

import com.crdt.implement.reliableBroadcast.OpBaseEvent;

public interface OpBaseCrdtOperation<S,Q,C,E> {
	public S Default();
	public Q Query(S crdt);
	public E Prepare(S crdt, C command);
	public S Effect(S crdt, OpBaseEvent<E> event);
	public S copy(S crdt);
}
