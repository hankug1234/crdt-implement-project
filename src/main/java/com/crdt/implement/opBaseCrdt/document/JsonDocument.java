package com.crdt.implement.opBaseCrdt.document;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONObject;

import com.crdt.implement.opBaseCrdt.document.command.Command;
import com.crdt.implement.opBaseCrdt.document.command.CommandTypes;
import com.crdt.implement.opBaseCrdt.document.expression.Expr;
import com.crdt.implement.opBaseCrdt.document.expression.ExprTypes;
import com.crdt.implement.opBaseCrdt.document.expression.ExprTypes.Root;
import com.crdt.implement.opBaseCrdt.document.keyType.StringK;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes;
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
		this.system = ActorSystem.create(Replicator.create(db, replicaId,new OpBaseDocOperation(replicaId),timeInterval),replicaId);
	}
	
	public JSONObject query(Expr expr) throws InterruptedException, ExecutionException, TimeoutException{
		
		CompletionStage<OpBaseResponse> stage = AskPattern
				.ask(system, replay-> new Protocal.Command<List<Command>>(List.of(new CommandTypes.Var(expr)),replay), Duration.ofSeconds(3), system.scheduler());
		
		Protocal.Res<JSONObject> result = (Protocal.Res<JSONObject>) stage.toCompletableFuture().get(1L, TimeUnit.SECONDS);
		
		return result.getRes();
	}
	
	
	public void applyCommands(List<Command> cmds) throws InterruptedException, ExecutionException, TimeoutException{
		
		List<Command> cloneCmds = new ArrayList<>();
		cloneCmds.addAll(cmds);
		
		CompletionStage<OpBaseResponse> stage = AskPattern
				.ask(system, replay-> new Protocal.Command<List<Command>>(cloneCmds, replay), Duration.ofSeconds(3), system.scheduler());
	}
	
	public void assign() {
		
	}
	
	public void insert() {
		
	}
	
	public void edit() {
		
	}
	
	public void delete() {
		
	}
	
	public void move() {
		
	}
	
	public void let(String variableName, Root paths) throws InterruptedException, ExecutionException, TimeoutException {
		this.applyCommands(List.of(new CommandTypes.Let(new ExprTypes.Var(variableName),paths.build())));
	}
	
	
	public void connect(JsonDocument  json) {
		system.tell(new Protocal.Connect(json.getReplicaId(),new EndPoint(json.getSystem())));
	}
	
}
