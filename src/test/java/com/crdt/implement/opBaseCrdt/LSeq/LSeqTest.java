package com.crdt.implement.opBaseCrdt.LSeq;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.crdt.implement.persistence.InMemoryCrdtDB;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LSeqTest {
	
	static Executor sharedExcutor;
	static String replicaId1;
	static String replicaId2;
	static String replicaId3; 
	
	static InMemoryCrdtDB<List<Vertex<String>>,LSeqData<String>> db1 ;
	static InMemoryCrdtDB<List<Vertex<String>>,LSeqData<String>> db2 ;
	static InMemoryCrdtDB<List<Vertex<String>>,LSeqData<String>> db3 ; 
	
	static LSeq<String> a ;
	static LSeq<String> b ;
	static LSeq<String> c ;
	
	@BeforeAll
	static void initialize() throws InterruptedException, ExecutionException, TimeoutException {
		Executor sharedExcutor = Executors.newFixedThreadPool(100);
		
		replicaId1 = "a";
		db1 = new InMemoryCrdtDB<>(sharedExcutor);
		
		replicaId2 = "b";
		db2 = new InMemoryCrdtDB<>(sharedExcutor);
		
		replicaId3 = "c";
		db3 = new InMemoryCrdtDB<>(sharedExcutor);
		
		
		a = new LSeq<>(replicaId1,db1,Duration.ofMillis(500));
		b = new LSeq<>(replicaId2,db2,Duration.ofMillis(500));
		c = new LSeq<>(replicaId2,db2,Duration.ofMillis(500));
		
		a.connect(b);
		b.connect(a);
		
		c.connect(b);
		b.connect(c);
		
		b.insert(0, "h");
		b.insert(1, "e");
		b.insert(2, "l");
		b.insert(3, "l");
		b.insert(4, "o");
		
	}
	
	
	@Test
	@Order(1)
	void testQuery() throws InterruptedException, ExecutionException, TimeoutException {
		Thread.sleep(1000);
		assertEquals(c.query().stream().collect(Collectors.joining()),"hello");
		assertEquals(a.query().stream().collect(Collectors.joining()),"hello");
		
	}

	@Test
	@Order(2)
	void testInsert() throws InterruptedException, ExecutionException, TimeoutException{
		a.insert(2, "l");
		c.insert(5, "!");
		
		Thread.sleep(1000);
		assertEquals(a.query().stream().collect(Collectors.joining()),"helllo!");
		assertEquals(a.query().stream().collect(Collectors.joining()),"helllo!");
	}

	@Test
	@Order(3)
	void testRemoveAt() throws InterruptedException, ExecutionException, TimeoutException {
		a.removeAt(2);
		c.removeAt(6);
		
		Thread.sleep(1000);
		assertEquals(a.query().stream().collect(Collectors.joining()),"hello");
		assertEquals(a.query().stream().collect(Collectors.joining()),"hello");
	}

}
