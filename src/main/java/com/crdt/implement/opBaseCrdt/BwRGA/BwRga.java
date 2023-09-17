package com.crdt.implement.opBaseCrdt.BwRGA;

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
public class BwRga<A> {
	private ActorSystem<OpBaseProtocal> system;
	private String replicaId;
	
	public BwRga(String replicaId,OpBaseCrdtDB<BwRgaState<A>,BwRgaData<A>> db,Duration timeInterval) {
		this.replicaId = replicaId;
		this.system = ActorSystem.create(Replicator.create(db, replicaId,new OpBaseBwRgaOperation<A>(replicaId),timeInterval),replicaId);
	}
	
	public List<Block<A>> query() throws InterruptedException, ExecutionException, TimeoutException{
		
		CompletionStage<OpBaseResponse> stage = AskPattern
				.ask(system, replay-> new Protocal.Query(replay), Duration.ofSeconds(3), system.scheduler());
		
		Protocal.Res<List<Block<A>>> result = (Protocal.Res<List<Block<A>>>) stage.toCompletableFuture().get(1L, TimeUnit.SECONDS);
		
		return result.getRes();
	}
	
	
	public List<Block<A>> insert(int index, List<A> value) throws InterruptedException, ExecutionException, TimeoutException{
		CompletionStage<OpBaseResponse> stage = AskPattern
				.ask(system, replay-> new Protocal.Command<BwRgaCommand>(new Command.Insert<A>(index, value), replay), Duration.ofSeconds(3), system.scheduler());
		
		Protocal.Res<List<Block<A>>> result = (Protocal.Res<List<Block<A>>>) stage.toCompletableFuture().get(1L, TimeUnit.SECONDS);
		
		return result.getRes();
	}
	
	public List<Block<A>> removeAt(int index,int count) throws InterruptedException, ExecutionException, TimeoutException{
		CompletionStage<OpBaseResponse> stage = AskPattern
				.ask(system, replay-> new Protocal.Command<BwRgaCommand>(new Command.RemoveAt<A>(index,count), replay), Duration.ofSeconds(3), system.scheduler());
		
		Protocal.Res<List<Block<A>>> result = (Protocal.Res<List<Block<A>>>) stage.toCompletableFuture().get(1L, TimeUnit.SECONDS);
		
		return result.getRes();
	}
	
	public void connect(BwRga<A>  rga) {
		system.tell(new Protocal.Connect(rga.getReplicaId(),new EndPoint(rga.getSystem())));
	}
}
