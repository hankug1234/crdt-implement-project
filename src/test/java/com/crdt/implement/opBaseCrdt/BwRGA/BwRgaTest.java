package com.crdt.implement.opBaseCrdt.BwRGA;

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
class BwRgaTest {

	static Executor sharedExcutor;
	static String replicaId1;
	static String replicaId2;
	static String replicaId3; 
	
	static InMemoryCrdtDB<BwRgaState<String>,BwRgaData<String>> db1 ;
	static InMemoryCrdtDB<BwRgaState<String>,BwRgaData<String>> db2 ;
	static InMemoryCrdtDB<BwRgaState<String>,BwRgaData<String>> db3 ; 
	
	static BwRga<String> a ;
	static BwRga<String> b ;
	static BwRga<String> c ;
	
	@BeforeAll
	static void initialize() throws InterruptedException, ExecutionException, TimeoutException {
		
		Executor sharedExcutor = Executors.newFixedThreadPool(100);
		
		replicaId1 = "a";
		db1 = new InMemoryCrdtDB<>(sharedExcutor);
		
		replicaId2 = "b";
		db2 = new InMemoryCrdtDB<>(sharedExcutor);
		
		replicaId3 = "c";
		db3 = new InMemoryCrdtDB<>(sharedExcutor);
		
		
		a = new BwRga<>(replicaId1,db1,Duration.ofMillis(500));
		b = new BwRga<>(replicaId2,db2,Duration.ofMillis(500));
		c = new BwRga<>(replicaId3,db3,Duration.ofMillis(500));
		
		a.connect(b);
		b.connect(a);
		
		c.connect(b);
		b.connect(c);
		
		b.insert(0, List.of("hello".split("")));
		
		
	}
	
	@Test
	@Order(1)
	void testQuery() throws InterruptedException, ExecutionException, TimeoutException {
		
		Thread.sleep(1000);
		assertEquals(a.query().stream().flatMap(b->b.getContent().getContent().stream()).collect(Collectors.joining()),"hello");
		assertEquals(c.query().stream().flatMap(b->b.getContent().getContent().stream()).collect(Collectors.joining()),"hello");
	}

	@Test
	@Order(2)
	void testInsert() throws InterruptedException, ExecutionException, TimeoutException {
		c.insert(5, List.of(" han".split("")));
		a.insert(5, List.of(" world".split("")));
		
		
		Thread.sleep(2000);
		assertEquals(a.query().stream().flatMap(b->b.getContent().getContent().stream()).collect(Collectors.joining()),"hello world han");
		assertEquals(c.query().stream().flatMap(b->b.getContent().getContent().stream()).collect(Collectors.joining()),"hello world han");
		
		c.insert(6, List.of("king of the ".split("")));
		
		Thread.sleep(1000);
		
		assertEquals(a.query().stream().flatMap(b->b.getContent().getContent().stream()).collect(Collectors.joining()),"hello king of the world han");
		assertEquals(c.query().stream().flatMap(b->b.getContent().getContent().stream()).collect(Collectors.joining()),"hello king of the world han");
	}

	@Test
	@Order(3)
	void testRemoveAt() throws InterruptedException, ExecutionException, TimeoutException {
		a.removeAt(6,5);
		
		//c.removeAt(8,3);
		
		//c.insert(8, List.of("rmd".split("")));
		
		Thread.sleep(2000);
		assertEquals(a.query().stream().flatMap(b->b.getContent().getContent().stream()).collect(Collectors.joining()),"hello of the world han");
		assertEquals(c.query().stream().flatMap(b->b.getContent().getContent().stream()).collect(Collectors.joining()),"hello of the world han");
	}

}
