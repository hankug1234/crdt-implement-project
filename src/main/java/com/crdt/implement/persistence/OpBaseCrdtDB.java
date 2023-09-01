package com.crdt.implement.persistence;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

import com.crdt.implement.opBaseCrdt.ReplicationState;
import com.crdt.implement.reliableBroadcast.OpBaseEvent;

public interface OpBaseCrdtDB<S,E> {
	public CompletableFuture<Boolean> SaveSnapshot(ReplicationState<S> state);
	public CompletableFuture<Optional<ReplicationState<S>>> LoadSnapshot(Function<S,S> copy);
	public CompletableFuture<List<OpBaseEvent<E>>> LoadEvents(long startSeqNr);
	public CompletableFuture<Boolean> SaveEvents(List<OpBaseEvent<E>> events);

}
