package com.crdt.implement.persistence;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import com.crdt.implement.opBaseCrdt.ReplicationState;
import com.crdt.implement.reliableBroadcast.OpBaseEvent;

public interface OpBaseCrdtDB<S,E> {
	public CompletableFuture<Boolean> SaveSnapshot(S state);
	public CompletableFuture<Optional<ReplicationState<S>>> LoadSnapshot(String replicaId);
	public CompletableFuture<List<OpBaseEvent<E>>> LoadEvents(String replicaId,long startSeqNr);
	public CompletableFuture<Boolean> SaveEvents(String replicaId,List<OpBaseEvent<E>> events);
}
