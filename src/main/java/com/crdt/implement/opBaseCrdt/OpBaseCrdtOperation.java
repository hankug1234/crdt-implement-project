package com.crdt.implement.opBaseCrdt;


public interface OpBaseCrdtOperation<S,Q,C,E> {
	public S Default();
	public Q Query(S crdt);
	public E Prepare(S crdt, C command);
	public S Effect(S crdt, E evnet);
	public S copy(S crdt);
}
