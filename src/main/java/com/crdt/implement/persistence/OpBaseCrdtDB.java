package com.crdt.implement.persistence;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public interface OpBaseCrdtDB<S,E> {
	public CompletableFuture<Boolean> SaveSnapshot(S state);
	public CompletableFuture<Optional<S>> LoadSnapshot(String replicaId);
	public CompletableFuture<List<E>> LoadEvents(String replicaId,long startSeqNr);
	public CompletableFuture<Boolean> SaveEvents(String replicaId,List<E> events);
}
