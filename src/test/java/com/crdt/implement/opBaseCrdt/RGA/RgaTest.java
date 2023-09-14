package com.crdt.implement.opBaseCrdt.RGA;

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

import com.crdt.implement.opBaseCrdt.OpBaseCrdtOperation;
import com.crdt.implement.persistence.InMemoryCrdtDB;
import com.crdt.implement.reliableBroadcast.Replicator;

import lombok.extern.slf4j.Slf4j;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RgaTest {

	static Executor sharedExcutor;
	static String replicaId1;
	static String replicaId2;
	static String replicaId3; 
	
	static InMemoryCrdtDB<RgaState<String>,RgaData<String>> db1 ;
	static InMemoryCrdtDB<RgaState<String>,RgaData<String>> db2 ;
	static InMemoryCrdtDB<RgaState<String>,RgaData<String>> db3 ; 
	
	static Rga<String> a ;
	static Rga<String> b ;
	static Rga<String> c ;
	
	@BeforeAll
	static void initialize() throws InterruptedException, ExecutionException, TimeoutException {
		
		Executor sharedExcutor = Executors.newFixedThreadPool(100);
		
		replicaId1 = "a";
		db1 = new InMemoryCrdtDB<>(sharedExcutor);
		
		replicaId2 = "b";
		db2 = new InMemoryCrdtDB<>(sharedExcutor);
		
		replicaId3 = "c";
		db3 = new InMemoryCrdtDB<>(sharedExcutor);
		
		
		a = new Rga<>(replicaId1,db1,Duration.ofMillis(500));
		b = new Rga<>(replicaId2,db2,Duration.ofMillis(500));
		c = new Rga<>(replicaId3,db3,Duration.ofMillis(500));
		
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
		assertEquals(a.query().stream().collect(Collectors.joining()),"hello");
		assertEquals(c.query().stream().collect(Collectors.joining()),"hello");
	}

	@Test
	@Order(2)
	void testInsert() throws InterruptedException, ExecutionException, TimeoutException {
		a.insert(5, " ");
		a.insert(6, "w");
		a.insert(7, "o");
		a.insert(8, "r");
		a.insert(9, "l");
		a.insert(10, "d");
		
		c.insert(5, " ");
		c.insert(6, "h");
		c.insert(7, "a");
		c.insert(8, "n");
		
		Thread.sleep(2000);
		assertEquals(a.query().stream().collect(Collectors.joining()),"hello world han");
		assertEquals(c.query().stream().collect(Collectors.joining()),"hello world han");
		
	}

	@Test
	@Order(3)
	void testRemoveAt() throws InterruptedException, ExecutionException, TimeoutException {
		a.removeAt(6);
		a.removeAt(6);
		a.removeAt(6);
		a.removeAt(6);
		a.removeAt(6);
		
		c.removeAt(8);
		c.removeAt(8);
		c.removeAt(8);
		
		c.insert(8, "r");
		c.insert(9, "m");
		c.insert(10, "d");
		
		Thread.sleep(2000);
		assertEquals(a.query().stream().collect(Collectors.joining()),"hello rmd han");
		assertEquals(c.query().stream().collect(Collectors.joining()),"hello rmd han");
	}

}
