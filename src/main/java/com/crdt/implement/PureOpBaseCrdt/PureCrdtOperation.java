package com.crdt.implement.PureOpBaseCrdt;

import java.util.Set;

import com.crdt.implement.taggedStableCusalBroadcast.PureOpBaseEvent;

public interface PureCrdtOperation<S,Q,O> {
	public S Default();
	public boolean Obsoletes(PureOpBaseEvent<O> e1, PureOpBaseEvent<O> e2);
	public S Apply(S crdt, Set<PureOpBaseEvent<O>> ops);
	public Q Query(S crdt, Set<PureOpBaseEvent<O>> ops);
	
	public S Copy(S source);
}
