package com.crdt.implement.opBaseCrdt.document;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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

import com.crdt.implement.opBaseCrdt.document.command.Command;
import com.crdt.implement.opBaseCrdt.document.command.CommandTypes;
import com.crdt.implement.opBaseCrdt.document.expression.ExprTypes;
import com.crdt.implement.opBaseCrdt.document.keyType.StringK;
import com.crdt.implement.opBaseCrdt.document.typetag.*;
import com.crdt.implement.opBaseCrdt.document.values.EmptyList;
import com.crdt.implement.opBaseCrdt.document.values.Str;
import com.crdt.implement.persistence.InMemoryCrdtDB;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JsonDocumentTest {

	public static List<Command> cmds = new LinkedList<>();
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
		
	}
	
	@Test
	@Order(2)
	void testQuery() throws InterruptedException, ExecutionException, TimeoutException {
		
		List<Command> cmds2 = new LinkedList<>();
		
		doc.connect(doc2);
		doc2.connect(doc);
		
		
		Thread.sleep(1000);
		
		Command command1 = new CommandTypes.Let(new ExprTypes.Var("carts"),new ExprTypes.Doc(new ExprTypes.Get(new TagTypes.ListT(new StringK("carts")))));
		Command command2 = new CommandTypes.Insert(new ExprTypes.Var("carts"),new Str("kakao"),0);
		Command command3 = new CommandTypes.Insert(new ExprTypes.Var("carts"),new Str("orange"),0);
		
		cmds2.add(command1);
		cmds2.add(command2);
		cmds2.add(command3);
		
		doc2.applyCommands(cmds2);
		
		cmds2.clear();
		Command command4 = new CommandTypes.Insert(new ExprTypes.Var("carts"),new Str("dragon"),0);
		Command command5 = new CommandTypes.Insert(new ExprTypes.Var("carts"),new Str("kiwi"),0);
		cmds2.add(command4);
		cmds2.add(command5);
		
		doc.applyCommands(cmds2);
		
		Thread.sleep(1000);
		
		JSONObject result2 = doc2.query(new ExprTypes.Doc());
		log.info(result2.toString());
		
		
		JSONObject result = doc.query(new ExprTypes.Doc());
		log.info(result.toString());
		assertTrue(true);
	}

	@Test
	@Order(1)
	void testApplyCommands() throws InterruptedException, ExecutionException, TimeoutException {
		doc.applyCommands(cmds);
		assertTrue(true);
	}

}
