package com.crdt.implement.PureOpBaseCrdt;

import com.crdt.implement.taggedStableCusalBroadcast.PureOpBaseEvent;

public interface PureCrdtOperation<S,Q,O> {
	public S Default();
	public boolean Obsoletes(PureOpBaseEvent<O> e1, PureOpBaseEvent<O> e2);
	public Q Apply(PureReplicationState<S,O> crdt);
}
