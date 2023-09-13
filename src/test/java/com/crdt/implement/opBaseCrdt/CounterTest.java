package com.crdt.implement.opBaseCrdt;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

import com.crdt.implement.opBaseCrdt.counter.Counter;
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
		
		String replicaId3 = "c";
		InMemoryCrdtDB<Long,Long> db3 = new InMemoryCrdtDB<>(sharedExcutor);
		
		String replicaId4 = "d";
		InMemoryCrdtDB<Long,Long> db4 = new InMemoryCrdtDB<>(sharedExcutor);
		
		Counter a = new Counter(replicaId1,db1,Duration.ofMillis(500));
		Counter b = new Counter(replicaId2,db2,Duration.ofMillis(500));
		Counter c = new Counter(replicaId3,db3,Duration.ofMillis(500));
		Counter d = new Counter(replicaId4,db4,Duration.ofMillis(500));
		
		a.connect(b);
		b.connect(a);
		
		b.connect(c);
		c.connect(b);
		
		b.connect(d);
		d.connect(b);
		
		try {
			a.inc(5L);
			assertSame(a.qeury(),5L);
			c.inc(2L);
			d.inc(2L);
			
			Thread.sleep(3000);
			assertSame(b.qeury(),9L);
			assertSame(a.qeury(),9L);
			assertSame(c.qeury(),9L);
		}catch(Exception e) {
			assertTrue(false);
		}
		
		
	}

}
