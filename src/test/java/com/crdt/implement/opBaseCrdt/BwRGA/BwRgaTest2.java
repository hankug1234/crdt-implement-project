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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BwRgaTest2 {

	static Executor sharedExcutor;
	static String replicaId1;
	static String replicaId2;
	
	static InMemoryCrdtDB<BwRgaState<String>,BwRgaData<String>> db1 ;
	static InMemoryCrdtDB<BwRgaState<String>,BwRgaData<String>> db2 ;
	
	static BwRga<String> a ;
	static BwRga<String> b ;
	
	@BeforeAll
	static void initialize() throws InterruptedException, ExecutionException, TimeoutException {
		
		Executor sharedExcutor = Executors.newFixedThreadPool(100);
		
		replicaId1 = "a";
		db1 = new InMemoryCrdtDB<>(sharedExcutor);
		
		replicaId2 = "b";
		db2 = new InMemoryCrdtDB<>(sharedExcutor);
		
		a = new BwRga<>(replicaId1,db1,Duration.ofMillis(50));
		b = new BwRga<>(replicaId2,db2,Duration.ofMillis(50));
		
		a.connect(b);
		b.connect(a);
		
		b.insert(0, List.of("hello".split("")));
		
		
	}
	
	@Test
	@Order(1)
	void test() throws InterruptedException, ExecutionException, TimeoutException {
		
		Thread.sleep(100);
		
		a.insert(5, List.of(" hankug".split("")));
		b.insert(5, List.of(" master".split("")));
		
		Thread.sleep(100);
		
		log.info(a.query().stream().flatMap(b->b.getContent().getContent().stream()).collect(Collectors.joining()));
		log.info(b.query().stream().flatMap(b->b.getContent().getContent().stream()).collect(Collectors.joining()));
		
		b.insert(3, List.of(" host ".split("")));
		
		Thread.sleep(100);
		log.info(a.query().stream().flatMap(b->b.getContent().getContent().stream()).collect(Collectors.joining()));
		log.info(b.query().stream().flatMap(b->b.getContent().getContent().stream()).collect(Collectors.joining()));
		
		a.insert(3, List.of(" host ".split("")));
		
		Thread.sleep(100);
		log.info(a.query().stream().flatMap(b->b.getContent().getContent().stream()).collect(Collectors.joining()));
		log.info(b.query().stream().flatMap(b->b.getContent().getContent().stream()).collect(Collectors.joining()));
		
		assertTrue(true);
	}

}
