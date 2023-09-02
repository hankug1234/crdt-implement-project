package com.crdt.implement.opBaseCrdt;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

import com.crdt.implement.persistence.InMemoryCrdtDB;
import com.crdt.implement.reliableBroadcast.OpBaseProtocal;
import com.crdt.implement.reliableBroadcast.Replicator;

import akka.actor.typed.ActorSystem;

class CounterTest {

	@Test
	void test() {
		
		Executor sharedExcutor = Executors.newFixedThreadPool(100);
		
		String replicaId1 = "a";
		InMemoryCrdtDB<Long,Long> db1 = new InMemoryCrdtDB<>(sharedExcutor);
		
		String replicaId2 = "b";
		InMemoryCrdtDB<Long,Long> db2 = new InMemoryCrdtDB<>(sharedExcutor);
		
		Counter a = new Counter(replicaId1,db1);
		Counter b = new Counter(replicaId2,db2);
		
		a.connect(b);
		b.connect(a);
		try {
			a.inc(5L);
			assertSame(a.qeury(),5L);
			Thread.sleep(5000);
			assertSame(b.qeury(),5L);
		}catch(Exception e) {
			assertTrue(false);
		}
		
		
	}

}
