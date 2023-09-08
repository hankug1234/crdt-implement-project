package com.crdt.implement.PureOpBaseCrdt;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.crdt.implement.taggedStableCusalBroadcast.EndPoint;
import com.crdt.implement.taggedStableCusalBroadcast.Protocal;
import com.crdt.implement.taggedStableCusalBroadcast.PureOpBaseProtocal;
import com.crdt.implement.taggedStableCusalBroadcast.PureOpBaseResponse;
import com.crdt.implement.taggedStableCusalBroadcast.PureReplicator;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;

public class PureCounter {
	
	public String replicaId;
	public PureCrdtOperation<Long,Long,Long> crdt;
	public ActorSystem<PureOpBaseProtocal> actor;
	
	
	public PureCounter(String replicaId,Duration timeoutInterval) {
		this.replicaId = replicaId;
		this.crdt = new PureOpBaseCounterOperation();
		this.actor = ActorSystem.create(PureReplicator.create(this.crdt, replicaId, new PureReplicationState<>(replicaId,crdt.Default()),timeoutInterval), replicaId);
	}
	
	public String getReplicaId() {
		return this.replicaId;
	}
	
	public ActorSystem<PureOpBaseProtocal> getActorRef(){
		return this.actor;
	}
	
	public long inc(long num) throws InterruptedException, ExecutionException, TimeoutException {
		CompletionStage<PureOpBaseResponse> stage = AskPattern
				.ask(actor, replay-> new Protocal.Submit<Long>(num, replay), Duration.ofSeconds(3), actor.scheduler());
		
		Protocal.Res<Long> result = (Protocal.Res<Long>) stage.toCompletableFuture().get(1L, TimeUnit.SECONDS);
		
		return result.getRes(); 
	}
	
	public long query() throws InterruptedException, ExecutionException, TimeoutException {
		CompletionStage<PureOpBaseResponse> stage = AskPattern
				.ask(actor, replay-> new Protocal.Query(replay), Duration.ofSeconds(3), actor.scheduler());
		
		Protocal.Res<Long> result = (Protocal.Res<Long>) stage.toCompletableFuture().get(1L, TimeUnit.SECONDS);
		
		return result.getRes(); 
	}
	
	public void connect(PureCounter counter) {
		this.actor.tell(new Protocal.Connect(counter.getReplicaId(), new EndPoint(counter.getActorRef())));
	}
	
	public void evict(PureCounter counter) {
		this.actor.tell(new Protocal.Evict(counter.replicaId));
	}
}
