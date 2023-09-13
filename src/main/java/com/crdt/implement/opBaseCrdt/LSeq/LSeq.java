package com.crdt.implement.opBaseCrdt.LSeq;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.crdt.implement.opBaseCrdt.OpBaseCrdtOperation;
import com.crdt.implement.opBaseCrdt.counter.Counter;
import com.crdt.implement.opBaseCrdt.counter.OpBaseCounterOperation;
import com.crdt.implement.persistence.OpBaseCrdtDB;
import com.crdt.implement.reliableBroadcast.EndPoint;
import com.crdt.implement.reliableBroadcast.OpBaseProtocal;
import com.crdt.implement.reliableBroadcast.OpBaseResponse;
import com.crdt.implement.reliableBroadcast.Protocal;
import com.crdt.implement.reliableBroadcast.Replicator;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import lombok.Getter;

@Getter
public class LSeq<A> {
	
	private ActorSystem<OpBaseProtocal> system;
	private String replicaId;
	
	public LSeq(String replicaId,OpBaseCrdtDB<List<Vertex<A>>,LSeqData<A>> db,Duration timeInterval) {
		this.replicaId = replicaId;
		this.system = ActorSystem.create(Replicator.create(db, replicaId,new OpBaseLSeqOperation<A>(),timeInterval),replicaId);
	}
	
	public List<A> query() throws InterruptedException, ExecutionException, TimeoutException{
		
		CompletionStage<OpBaseResponse> stage = AskPattern
				.ask(system, replay-> new Protocal.Query(replay), Duration.ofSeconds(3), system.scheduler());
		
		Protocal.Res<List<A>> result = (Protocal.Res<List<A>>) stage.toCompletableFuture().get(1L, TimeUnit.SECONDS);
		
		return result.getRes();
	}
	
	
	public List<A> insert(int index, A value) throws InterruptedException, ExecutionException, TimeoutException{
		CompletionStage<OpBaseResponse> stage = AskPattern
				.ask(system, replay-> new Protocal.Command<LSeqCommand>(new Command.Insert<A>(index, value, replicaId), replay), Duration.ofSeconds(3), system.scheduler());
		
		Protocal.Res<List<A>> result = (Protocal.Res<List<A>>) stage.toCompletableFuture().get(1L, TimeUnit.SECONDS);
		
		return result.getRes();
	}
	
	public List<A> removeAt(int index) throws InterruptedException, ExecutionException, TimeoutException{
		CompletionStage<OpBaseResponse> stage = AskPattern
				.ask(system, replay-> new Protocal.Command<LSeqCommand>(new Command.RemoveAt<A>(index), replay), Duration.ofSeconds(3), system.scheduler());
		
		Protocal.Res<List<A>> result = (Protocal.Res<List<A>>) stage.toCompletableFuture().get(1L, TimeUnit.SECONDS);
		
		return result.getRes();
	}
	
	public void connect(Counter counter) {
		system.tell(new Protocal.Connect(counter.getReplicaId(),new EndPoint(counter.getSystem())));
	}
	
	
}
