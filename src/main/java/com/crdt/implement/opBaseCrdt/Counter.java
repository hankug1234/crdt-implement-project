package com.crdt.implement.opBaseCrdt;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import com.crdt.implement.reliableBroadcast.OpBaseProtocal;
import com.crdt.implement.reliableBroadcast.OpBaseResponse;
import com.crdt.implement.reliableBroadcast.Protocal;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.RecipientRef;
import akka.actor.typed.javadsl.AskPattern;
import akka.util.LineNumbers.Result;

public class Counter {
	private OpBaseCrdtOperation<Long,Long,Long,Long> counterOperation;
	private ActorRef<OpBaseProtocal> replicator;
	private ActorSystem<OpBaseProtocal> system;
	
	public Counter(ActorRef<OpBaseProtocal> replicator,ActorSystem<OpBaseProtocal> system) {
		this.counterOperation = new OpBaseCounterOperation();
		this.replicator = replicator;
		this.system = system;
	}
	
	public Long inc(Long num) throws InterruptedException, ExecutionException {
		CompletionStage<OpBaseResponse> stage = AskPattern
				.ask(replicator, replay-> new Protocal.Command<>(num, replay), Duration.ofSeconds(3), system.scheduler());
		
		Protocal.Res<Long> result = (Protocal.Res<Long>) stage.toCompletableFuture().get();
		
		return result.getRes(); 
	}
	
	public Long qeury() throws InterruptedException, ExecutionException {
		CompletionStage<OpBaseResponse> stage = AskPattern
				.ask(replicator, replay-> new Protocal.Query(replay), Duration.ofSeconds(3), system.scheduler());
		
		Protocal.Res<Long> result = (Protocal.Res<Long>) stage.toCompletableFuture().get();
		
		return result.getRes();
		
	}
	
}
