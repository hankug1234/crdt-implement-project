package com.crdt.implement.PureOpBaseCrdt;

import java.util.Set;

import com.crdt.implement.taggedStableCusalBroadcast.PureOpBaseEvent;

public interface PureCrdtOperation<S,Q,O> {
	public S Default();
	public boolean Obsoletes(O e1, O e2);
	public S Apply(S crdt, Set<O> ops);
	public Q Query(S crdt, Set<O> ops);
	
	public S Copy(S source);
}
