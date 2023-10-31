package com.crdt.implement.opBaseCrdt.document;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.RETURNS_SELF;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.crdt.implement.opBaseCrdt.document.expression.ExprTypes;
import com.crdt.implement.opBaseCrdt.document.expression.ExprTypes.Root;
import com.crdt.implement.opBaseCrdt.document.values.Str;
import com.crdt.implement.opBaseCrdt.document.values.Text;
import com.crdt.implement.opBaseCrdt.document.values.Location;
import com.crdt.implement.opBaseCrdt.document.values.Number;
import com.crdt.implement.opBaseCrdt.document.values.ObjectTypeVal;
import com.crdt.implement.persistence.InMemoryCrdtDB;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JsonDocumentTest3 {
	
	static Executor sharedExcutor;
	static String replicaId1;
	static InMemoryCrdtDB<Document,List<Operation>> db1 ;
	static JsonDocument doc1;
	
	static String replicaId2;
	static InMemoryCrdtDB<Document,List<Operation>> db2 ;
	static JsonDocument doc2;
	
	@BeforeAll
	public static void init() {
		Executor sharedExcutor = Executors.newFixedThreadPool(100);
		replicaId1 = "a";
		db1 = new InMemoryCrdtDB<>(sharedExcutor);
		doc1 = new JsonDocument(replicaId1,db1,Duration.ofMillis(50));
		
		replicaId2 = "b";
		db2 = new InMemoryCrdtDB<>(sharedExcutor);
		doc2 = new JsonDocument(replicaId2,db2,Duration.ofMillis(50));
	}
	
	@Test
	@Order(1)
	void test1() throws InterruptedException, ExecutionException, TimeoutException {
		doc1.connect(doc2);
		doc2.connect(doc1);
		
		doc2.assign(Root.document().reg("name"), new Str("hankug"));
		
		JSONArray arr = new JSONArray();
		arr.put(new Str("apple"));
		arr.put(new Str("cock"));
		arr.put(new Str("candy"));
		arr.put(new Str("snack"));
		
		doc2.assign(Root.document().list("carts"), arr);
		
		
		JSONObject obj = new JSONObject();
		obj.put("age", new Number(26));
		obj.put("job", new Str("Developer"));
		
		doc1.assign(Root.document().dict("profile"), obj);
		
		/*
		
		
		
		*/
		
		Thread.sleep(100);
		
		JSONObject result2 = doc2.query(new ExprTypes.Doc());
		log.info(result2.toString());
		
		
		JSONObject result = doc1.query(new ExprTypes.Doc());
		log.info(result.toString());
		
		
		doc2.assign(Root.document().dict("profile").reg("explain"), new ObjectTypeVal("text"));
		doc2.edit(Root.document().dict("profile").reg("explain"), Text.insert(0, "hello"));
		
		Thread.sleep(100);
		
		doc1.edit(Root.document().dict("profile").reg("explain"), Text.insert(5, " master"));
		doc2.edit(Root.document().dict("profile").reg("explain"), Text.insert(5, " hankug"));
		
		Thread.sleep(200);
		
		result2 = doc2.query(new ExprTypes.Doc());
		log.info(result2.toString());
		
		
		result = doc1.query(new ExprTypes.Doc());
		log.info(result.toString());
		
		doc1.move(Root.document().list("carts"), 1, 3, Location.after);
		
		Thread.sleep(100);
		
		doc1.disconnect(doc2);
		doc2.disconnect(doc1);
		
		doc1.insert(Root.document().list("carts"), 0, new Str("donut"));
		
		Thread.sleep(200);
		
		result2 = doc2.query(new ExprTypes.Doc());
		log.info(result2.toString());
		
		
		result = doc1.query(new ExprTypes.Doc());
		log.info(result.toString());
		
		doc1.connect(doc2);
		doc2.connect(doc1);
		
		Thread.sleep(200);
		
		result2 = doc2.query(new ExprTypes.Doc());
		log.info(result2.toString());
		
		
		result = doc1.query(new ExprTypes.Doc());
		log.info(result.toString());
		
		assertTrue(true);
		
	}
}
