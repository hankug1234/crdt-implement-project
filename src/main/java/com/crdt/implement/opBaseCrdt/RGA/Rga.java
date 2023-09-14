package com.crdt.implement.opBaseCrdt.RGA;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
public class Rga<A> {
	private ActorSystem<OpBaseProtocal> system;
	private String replicaId;
	
	public Rga(String replicaId,OpBaseCrdtDB<RgaState<A>,RgaData<A>> db,Duration timeInterval) {
		this.replicaId = replicaId;
		this.system = ActorSystem.create(Replicator.create(db, replicaId,new OpBaseRgaOperation<A>(replicaId),timeInterval),replicaId);
	}
	
	public List<A> query() throws InterruptedException, ExecutionException, TimeoutException{
		
		CompletionStage<OpBaseResponse> stage = AskPattern
				.ask(system, replay-> new Protocal.Query(replay), Duration.ofSeconds(3), system.scheduler());
		
		Protocal.Res<List<A>> result = (Protocal.Res<List<A>>) stage.toCompletableFuture().get(1L, TimeUnit.SECONDS);
		
		return result.getRes();
	}
	
	
	public List<A> insert(int index, A value) throws InterruptedException, ExecutionException, TimeoutException{
		CompletionStage<OpBaseResponse> stage = AskPattern
				.ask(system, replay-> new Protocal.Command<RgaCommand>(new Command.Insert<A>(index, value), replay), Duration.ofSeconds(3), system.scheduler());
		
		Protocal.Res<List<A>> result = (Protocal.Res<List<A>>) stage.toCompletableFuture().get(1L, TimeUnit.SECONDS);
		
		return result.getRes();
	}
	
	public List<A> removeAt(int index) throws InterruptedException, ExecutionException, TimeoutException{
		CompletionStage<OpBaseResponse> stage = AskPattern
				.ask(system, replay-> new Protocal.Command<RgaCommand>(new Command.RemoveAt<A>(index), replay), Duration.ofSeconds(3), system.scheduler());
		
		Protocal.Res<List<A>> result = (Protocal.Res<List<A>>) stage.toCompletableFuture().get(1L, TimeUnit.SECONDS);
		
		return result.getRes();
	}
	
	public void connect(Rga<A>  rga) {
		system.tell(new Protocal.Connect(rga.getReplicaId(),new EndPoint(rga.getSystem())));
	}
}
