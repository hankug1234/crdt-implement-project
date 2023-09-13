package com.crdt.implement.opBaseCrdt.counter;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.crdt.implement.opBaseCrdt.OpBaseCrdtOperation;
import com.crdt.implement.persistence.OpBaseCrdtDB;
import com.crdt.implement.reliableBroadcast.EndPoint;
import com.crdt.implement.reliableBroadcast.OpBaseProtocal;
import com.crdt.implement.reliableBroadcast.OpBaseResponse;
import com.crdt.implement.reliableBroadcast.Protocal;
import com.crdt.implement.reliableBroadcast.Replicator;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.RecipientRef;
import akka.actor.typed.javadsl.AskPattern;
import akka.util.LineNumbers.Result;
import lombok.Data;

@Data
public class Counter {
	private OpBaseCrdtOperation<Long,Long,Long,Long> counterOperation;
	private ActorSystem<OpBaseProtocal> system;
	private String replicaId;
	
	public Counter(String replicaId,OpBaseCrdtDB<Long,Long> db,Duration timeInterval) {
		this.replicaId = replicaId;
		this.system = ActorSystem.create(Replicator.create(db, replicaId, new OpBaseCounterOperation(),timeInterval),replicaId);
	}
	
	public Long inc(long num) throws InterruptedException, ExecutionException, TimeoutException {
		CompletionStage<OpBaseResponse> stage = AskPattern
				.ask(system, replay-> new Protocal.Command<>(num, replay), Duration.ofSeconds(3), system.scheduler());
		
		Protocal.Res<Long> result = (Protocal.Res<Long>) stage.toCompletableFuture().get(1L, TimeUnit.SECONDS);
		
		return result.getRes(); 
	}
	
	public Long qeury() throws InterruptedException, ExecutionException, TimeoutException {
		CompletionStage<OpBaseResponse> stage = AskPattern
				.ask(system, replay-> new Protocal.Query(replay), Duration.ofSeconds(3), system.scheduler());
		
		Protocal.Res<Long> result = (Protocal.Res<Long>) stage.toCompletableFuture().get(1L, TimeUnit.SECONDS);
		
		return result.getRes();
		
	}
	
	public void connect(Counter counter) {
		system.tell(new Protocal.Connect(counter.getReplicaId(),new EndPoint(counter.getSystem())));
	}
	
}
