package com.crdt.implement.opBaseCrdt.document;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.crdt.implement.opBaseCrdt.BwRGA.Command.Insert;
import com.crdt.implement.opBaseCrdt.BwRGA.Command.RemoveAt;
import com.crdt.implement.opBaseCrdt.document.command.Command;
import com.crdt.implement.opBaseCrdt.document.command.CommandTypes;
import com.crdt.implement.opBaseCrdt.document.expression.ExprTypes;
import com.crdt.implement.opBaseCrdt.document.keyType.StringK;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes;
import com.crdt.implement.opBaseCrdt.document.values.*;
import com.crdt.implement.opBaseCrdt.document.values.Number;
import com.crdt.implement.persistence.InMemoryCrdtDB;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JsonDocumentTest2 {

	public static List<Command> cmds = new LinkedList<>();
	public static List<Command> cmds2 = new LinkedList<>();
	
	static Executor sharedExcutor;
	static String replicaId1;
	static InMemoryCrdtDB<Document,List<Operation>> db1 ;
	static JsonDocument doc;
	
	static String replicaId2;
	static InMemoryCrdtDB<Document,List<Operation>> db2 ;
	static JsonDocument doc2;
	
	@BeforeAll
	public static void init() {
		
		Executor sharedExcutor = Executors.newFixedThreadPool(100);
		replicaId1 = "a";
		db1 = new InMemoryCrdtDB<>(sharedExcutor);
		doc = new JsonDocument(replicaId1,db1,Duration.ofMillis(500));
		
		replicaId2 = "b";
		db2 = new InMemoryCrdtDB<>(sharedExcutor);
		doc2 = new JsonDocument(replicaId2,db2,Duration.ofMillis(500));
		
		Command command1 = new CommandTypes.Assign(new ExprTypes.Doc(new ExprTypes.Get(new TagTypes.RegT(new StringK("name")))),new Str("hankug"));
		Command command2 = new CommandTypes.Assign(new ExprTypes.Doc(new ExprTypes.Get(new TagTypes.ListT(new StringK("carts")))),new EmptyList());
		Command command3 = new CommandTypes.Let(new ExprTypes.Var("carts"),new ExprTypes.Doc(new ExprTypes.Get(new TagTypes.ListT(new StringK("carts")))));
		Command command4 = new CommandTypes.Insert(new ExprTypes.Var("carts"),new Str("apple"),0);
		Command command5 = new CommandTypes.Insert(new ExprTypes.Var("carts"),new Str("cock"),0);
		Command command6 = new CommandTypes.Insert(new ExprTypes.Var("carts"),new Str("candy"),0);
		Command command7 = new CommandTypes.Insert(new ExprTypes.Var("carts"),new Str("snack"),0);
		
		cmds.add(command1);
		cmds.add(command2);
		cmds.add(command3);
		cmds.add(command4);
		cmds.add(command5);
		cmds.add(command6);
		cmds.add(command7);
		
		Command command8 = new CommandTypes.Assign(new ExprTypes.Doc(new ExprTypes.Get(new TagTypes.MapT(new StringK("profile")))), new EmptyMap());
		Command command9 = new CommandTypes.Assign(new ExprTypes.Doc(new ExprTypes.Get(new TagTypes.MapT(new StringK("profile")),
				new ExprTypes.Get(new TagTypes.RegT(new StringK("age"))))), new Number(26));
		Command command10 = new CommandTypes.Assign(new ExprTypes.Doc(new ExprTypes.Get(new TagTypes.MapT(new StringK("profile")),
				new ExprTypes.Get(new TagTypes.RegT(new StringK("job"))))), new Str("Developer"));
		
		cmds2.add(command8);
		cmds2.add(command9);
		cmds2.add(command10);
	}
	
	
	@Test
	@Order(1)
	void test1() throws InterruptedException, ExecutionException, TimeoutException {
		
		doc.connect(doc2);
		doc2.connect(doc);
		
		doc2.applyCommands(cmds);
		doc.applyCommands(cmds2);
		
		Thread.sleep(1000);
		
		JSONObject result2 = doc2.query(new ExprTypes.Doc());
		log.info(result2.toString());
		
		
		JSONObject result = doc.query(new ExprTypes.Doc());
		log.info(result.toString());
		
		assertTrue(true);
	}
	
	@Test
	@Order(2)
	void test2() throws InterruptedException, ExecutionException, TimeoutException {
		List<Command> cmds = new LinkedList<>();
		Command command = new CommandTypes.Delete(new ExprTypes.Doc(new ExprTypes.Get(new TagTypes.MapT(new StringK("profile")))));
		cmds.add(command);
		
		doc2.applyCommands(cmds);
		
		Thread.sleep(1000);
		
		JSONObject result2 = doc2.query(new ExprTypes.Doc());
		log.info(result2.toString());
		
		
		JSONObject result = doc.query(new ExprTypes.Doc());
		log.info(result.toString());
		
		assertTrue(true);
	}
	
	@Test
	@Order(3)
	void test3() throws InterruptedException, ExecutionException, TimeoutException {
		Command command8 = new CommandTypes.Assign(new ExprTypes.Doc(new ExprTypes.Get(new TagTypes.MapT(new StringK("profile")))), new EmptyMap());
		//Command command9 = new CommandTypes.Assign(new ExprTypes.Doc(new ExprTypes.Get(new TagTypes.ListT(new StringK("profile")))), new EmptyList());
		Command command10 = new CommandTypes.Assign(new ExprTypes.Doc(new ExprTypes.Get(new TagTypes.MapT(new StringK("profile")),
				new ExprTypes.Get(new TagTypes.RegT(new StringK("job"))))), new Str("Developer"));
		//Command command11 = new CommandTypes.Insert(new ExprTypes.Doc(new ExprTypes.Get(new TagTypes.ListT(new StringK("profile")))),new Str("hankug") , 0);
		
		Command command12 = new CommandTypes.Assign(new ExprTypes.Doc(new ExprTypes.Get(new TagTypes.MapT(new StringK("profile")),
				new ExprTypes.Get(new TagTypes.RegT(new StringK("explain"))))), new ObjectTypeVal("text"));
		
		Command command13 = new CommandTypes.Edit(new ExprTypes.Doc(new ExprTypes.Get(new TagTypes.MapT(new StringK("profile")),
				new ExprTypes.Get(new TagTypes.RegT(new StringK("explain"))))), new Insert<String>(0, List.of("hello".split(""))));
		
		List<Command> cmds1 = new LinkedList<>();
		List<Command> cmds2 = new LinkedList<>();
		
		cmds1.add(command8);
		cmds1.add(command10);
		
		cmds1.add(command12);
		cmds1.add(command13);
		
		//cmds2.add(command9);
		//cmds2.add(command11);
		
		doc2.applyCommands(cmds1);
		doc.applyCommands(cmds2);
		
		Thread.sleep(1000);
		
		JSONObject result2 = doc2.query(new ExprTypes.Doc());
		log.info(result2.toString());
		
		
		JSONObject result = doc.query(new ExprTypes.Doc());
		log.info(result.toString());
		
		cmds1.clear();
		cmds2.clear();
		
		Command command14 = new CommandTypes.Edit(new ExprTypes.Doc(new ExprTypes.Get(new TagTypes.MapT(new StringK("profile")),
				new ExprTypes.Get(new TagTypes.RegT(new StringK("explain"))))), new Insert<String>(5, List.of(" master".split(""))));
		
		Command command15 = new CommandTypes.Edit(new ExprTypes.Doc(new ExprTypes.Get(new TagTypes.MapT(new StringK("profile")),
				new ExprTypes.Get(new TagTypes.RegT(new StringK("explain"))))), new Insert<String>(5, List.of(" hankug".split(""))));
		
		
		cmds1.add(command14);
		cmds2.add(command15);
		
		doc2.applyCommands(cmds1);
		doc.applyCommands(cmds2);
		
		Thread.sleep(1000);
		
		result2 = doc2.query(new ExprTypes.Doc());
		log.info(result2.toString());
		
		
		result = doc.query(new ExprTypes.Doc());
		log.info(result.toString());
		
		
		cmds1.clear();
		cmds2.clear();
		
		
		assertTrue(true);
		
	}
	
	@Test
	@Order(4)
	void test4() throws InterruptedException, ExecutionException, TimeoutException {
		List<Command> cmds1 = new LinkedList<>();
		List<Command> cmds2 = new LinkedList<>();
		
		JSONObject result2 = doc2.query(new ExprTypes.Doc());
		log.info(result2.toString());
		
		
		JSONObject result = doc.query(new ExprTypes.Doc());
		log.info(result.toString());
		
		
		Command command16 = new CommandTypes.Edit(new ExprTypes.Doc(new ExprTypes.Get(new TagTypes.MapT(new StringK("profile")),
				new ExprTypes.Get(new TagTypes.RegT(new StringK("explain"))))), new Insert<String>(2, List.of(" host".split(""))));
		
		//Command command17 = new CommandTypes.Edit(new ExprTypes.Doc(new ExprTypes.Get(new TagTypes.MapT(new StringK("profile")),
				//new ExprTypes.Get(new TagTypes.RegT(new StringK("explain"))))), new RemoveAt(0,5));
		
		
		cmds1.add(command16);
		//cmds2.add(command17);
		
		doc2.applyCommands(cmds1);
		//doc.applyCommands(cmds2);
		
		Thread.sleep(1000);
		
		result2 = doc2.query(new ExprTypes.Doc());
		log.info(result2.toString());
		
		
		result = doc.query(new ExprTypes.Doc());
		log.info(result.toString());
	}

}
