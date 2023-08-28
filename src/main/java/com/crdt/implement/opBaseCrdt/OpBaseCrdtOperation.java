package com.crdt.implement.opBaseCrdt;


public interface OpBaseCrdtOperation<Crdt,S,C,E> {
	public Crdt Default();
	public S Query(Crdt crdt);
	public S Prepare(Crdt crdt, C command);
	public Crdt Effect(Crdt crdt, E evnet);
}
