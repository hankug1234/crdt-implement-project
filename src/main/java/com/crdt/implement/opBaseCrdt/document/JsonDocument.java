package com.crdt.implement.opBaseCrdt.document;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONObject;

import com.crdt.implement.opBaseCrdt.document.command.Command;
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
public class JsonDocument {
	private ActorSystem<OpBaseProtocal> system;
	private String replicaId;
	
	public JsonDocument(String replicaId,OpBaseCrdtDB<Document,List<Operation>> db,Duration timeInterval) {
		this.replicaId = replicaId;
		this.system = ActorSystem.create(Replicator.create(db, replicaId,new OpBaseDocOperation(),timeInterval),replicaId);
	}
	
	public JSONObject query() throws InterruptedException, ExecutionException, TimeoutException{
		
		CompletionStage<OpBaseResponse> stage = AskPattern
				.ask(system, replay-> new Protocal.Query(replay), Duration.ofSeconds(3), system.scheduler());
		
		Protocal.Res<JSONObject> result = (Protocal.Res<JSONObject>) stage.toCompletableFuture().get(1L, TimeUnit.SECONDS);
		
		return result.getRes();
	}
	
	
	public JSONObject applyCommands(List<Command> cmds) throws InterruptedException, ExecutionException, TimeoutException{
		CompletionStage<OpBaseResponse> stage = AskPattern
				.ask(system, replay-> new Protocal.Command<List<Command>>(cmds, replay), Duration.ofSeconds(3), system.scheduler());
		
		Protocal.Res<JSONObject> result = (Protocal.Res<JSONObject>) stage.toCompletableFuture().get(1L, TimeUnit.SECONDS);
		
		return result.getRes();
	}
	
	public void connect(JsonDocument  json) {
		system.tell(new Protocal.Connect(json.getReplicaId(),new EndPoint(json.getSystem())));
	}
	
}
