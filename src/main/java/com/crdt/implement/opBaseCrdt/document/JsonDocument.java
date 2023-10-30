package com.crdt.implement.opBaseCrdt.document;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.crdt.implement.opBaseCrdt.document.command.Command;
import com.crdt.implement.opBaseCrdt.document.command.CommandTypes;
import com.crdt.implement.opBaseCrdt.document.expression.Expr;
import com.crdt.implement.opBaseCrdt.document.expression.ExprTypes;
import com.crdt.implement.opBaseCrdt.document.expression.ExprTypes.Root;
import com.crdt.implement.opBaseCrdt.document.keyType.StringK;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes;
import com.crdt.implement.opBaseCrdt.document.values.EmptyList;
import com.crdt.implement.opBaseCrdt.document.values.EmptyMap;
import com.crdt.implement.opBaseCrdt.document.values.Location;
import com.crdt.implement.opBaseCrdt.document.values.Val;
import com.crdt.implement.persistence.OpBaseCrdtDB;
import com.crdt.implement.reliableBroadcast.EndPoint;
import com.crdt.implement.reliableBroadcast.OpBaseProtocal;
import com.crdt.implement.reliableBroadcast.OpBaseResponse;
import com.crdt.implement.reliableBroadcast.Protocal;
import com.crdt.implement.reliableBroadcast.Replicator;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class JsonDocument {
	private ActorSystem<OpBaseProtocal> system;
	private String replicaId;
	
	public JsonDocument(String replicaId,OpBaseCrdtDB<Document,List<Operation>> db,Duration timeInterval) {
		this.replicaId = replicaId;
		this.system = ActorSystem.create(Replicator.create(db, replicaId,new OpBaseDocOperation(replicaId),timeInterval),replicaId);
	}
	
	private List<Command> convert(Root paths,Object obj,int index, boolean bool){
		
		List<Command> list = new LinkedList<>();
		
		if(obj instanceof JSONObject) {
			JSONObject data = (JSONObject) obj;
			
			if(!bool) {
				list.add(new CommandTypes.Assign(paths.build(),new EmptyMap()));
			}else {
				list.add(new CommandTypes.Insert(paths.build(), new EmptyMap(),index));
			}
			
			
			for(String key : data.keySet()) {
				Object value = data.get(key);
				
				if(value instanceof JSONObject) {
					list.addAll(convert(paths.clone().dict(key),value,0,false));
				}else if(value instanceof JSONArray) {
					list.addAll(convert(paths.clone().list(key),value,0,false));
				}else if(value instanceof Val) {
					list.addAll(convert(paths.clone().reg(key),value,0,false));
				}
				
			}
			
		}else if(obj instanceof JSONArray) {
			JSONArray data = (JSONArray) obj;
			
			if(!bool) {
				list.add(new CommandTypes.Assign(paths.build(),new EmptyList()));
			}else {
				list.add(new CommandTypes.Insert(paths.build(), new EmptyList(),index));
			}
			
			
			Expr expr = paths.build();
			for(int i=0; i<data.length(); i++) {
				Object value = data.get(i);
				
				if(value instanceof JSONObject) {
					list.addAll(convert(paths.clone(),value,i,true));
					
				}else if(value instanceof JSONArray) {
					list.addAll(convert(paths.clone(),value,i,true));
					
				}else if(value instanceof Val) {
					Val val = (Val) value;
					list.add(new CommandTypes.Insert(expr, val, i));
				}
			}
			
		}else if(obj instanceof Val) {
			Val data = (Val) obj;
			if(!bool) {
				list.add(new CommandTypes.Assign(paths.build(),data));
			}else {
				list.add(new CommandTypes.Insert(paths.build(),data,index));
			}
		}
		
		return list;
	}
	
	
	public JSONObject get(Root root) throws InterruptedException, ExecutionException, TimeoutException{
		Expr expr = root.build();
		return this.query(expr);
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
	
	public void assign(Root paths, Object obj) throws InterruptedException, ExecutionException, TimeoutException {
		Expr expr = paths.build();
		if(expr == null) {
			throw new RuntimeException();
		}
		
		List<Command> commands = convert(paths,obj,0,false);
		this.applyCommands(commands);
		
	}
	
	public void insert(Root paths, int index, Object obj ) throws InterruptedException, ExecutionException, TimeoutException {
		Expr expr = paths.build();
		if(expr == null) {
			throw new RuntimeException();
		}
		
		List<Command> commands = convert(paths,obj,index,true);
		this.applyCommands(commands);
		
	}
	
	
	public void edit(Root paths, Object behavior) throws InterruptedException, ExecutionException, TimeoutException {
		
		Expr expr = paths.build();
		if(expr == null) {
			throw new RuntimeException();
		}
		
		this.applyCommands(List.of(new CommandTypes.Edit(expr, behavior)));
		
	}
	
	public void delete(Root paths) throws InterruptedException, ExecutionException, TimeoutException {
		
		Expr expr = paths.build();
		
		if(expr == null) {
			throw new RuntimeException();
		}
		
		this.applyCommands(List.of(new CommandTypes.Delete(expr)));
	}
	
	public void move(Root paths,int from, int to, Location location) throws InterruptedException, ExecutionException, TimeoutException {
		Expr expr = paths.build();
		
		if(expr == null) {
			throw new RuntimeException();
		}
		
		this.applyCommands(List.of(new CommandTypes.Move(expr, from, to, location.getValue())));
	}
	
	public void let(String variableName, Root paths) throws InterruptedException, ExecutionException, TimeoutException {
		this.applyCommands(List.of(new CommandTypes.Let(new ExprTypes.Var(variableName),paths.build())));
	}
	
	
	public void connect(JsonDocument  json) {
		system.tell(new Protocal.Connect(json.getReplicaId(),new EndPoint(json.getSystem())));
	}
	
}
