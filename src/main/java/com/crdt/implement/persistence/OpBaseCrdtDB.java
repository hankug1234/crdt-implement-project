package com.crdt.implement.persistence;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

public interface OpBaseCrdtDB<S,E> {
	public Future<Boolean> SaveSnapshot(S state);
	public Future<Optional<S>> LoadSnapshot(String replicaId);
	public Future<List<E>> LoadEvents(String replicaId,long startSeqNr);
	public Future<Boolean> SaveEvents(String replicaId,List<E> events);
}
