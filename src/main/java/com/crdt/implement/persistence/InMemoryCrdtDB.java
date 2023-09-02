package com.crdt.implement.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.crdt.implement.opBaseCrdt.ReplicationState;
import com.crdt.implement.reliableBroadcast.OpBaseEvent;

public class InMemoryCrdtDB<S,E> implements OpBaseCrdtDB<S,E>{
	
	private ReplicationState<S> snapshot;
	private ConcurrentNavigableMap<Long,OpBaseEvent<E>> events;
	private final Executor executor; // Executors.newFixedThreadPool(100,new ThreadFactory())
	
	public InMemoryCrdtDB(Executor executor) {
		this.events = new ConcurrentSkipListMap<>();
		this.executor = executor; 
		this.snapshot = new ReplicationState<>();
	}
	
	
	public CompletableFuture<Boolean> SaveSnapshot(ReplicationState<S> state){
		return CompletableFuture.supplyAsync(()->{
	
		   synchronized(snapshot){
			   snapshot = state;
		   	}
			
			return true;
		},this.executor);
	}
	
	public CompletableFuture<Optional<ReplicationState<S>>> LoadSnapshot(Function<S,S> copy){
		return CompletableFuture.supplyAsync(()->{
			Optional<ReplicationState<S>> state;
			synchronized(snapshot){
				if(snapshot.getReplicaId() == null) {
					state = Optional.empty();
				}else {
					state = Optional.of(snapshot.clone(copy.apply(snapshot.getCrdt())));
				}
			}
			return state;
		});
	}
	
	public CompletableFuture<List<OpBaseEvent<E>>> LoadEvents(long startSeqNr){
		return CompletableFuture.supplyAsync(()->{
			List<OpBaseEvent<E>> resultBuffers = new ArrayList<>();
			ConcurrentNavigableMap<Long,OpBaseEvent<E>> subEvents = events.tailMap(startSeqNr);
			
			if(subEvents == null) {
				return resultBuffers;
			}
			
			for(long key : subEvents.keySet()) {
				resultBuffers.add(subEvents.get(key));
			}
			return resultBuffers;
		});
	}
	
	public CompletableFuture<Boolean> SaveEvents(List<OpBaseEvent<E>> newEvents){
		return CompletableFuture.supplyAsync(()->{
			for(OpBaseEvent<E> event : newEvents) {
				events.put(event.getLocalSeqNr(), event);
			}
			return true;
		});
	}
}
