package com.crdt.implement.vectorClock;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.Map.Entry;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VectorClockTest {
	static VectorClock test1; static VectorClock test2;
	
	@BeforeAll
	static void testinit() {
		test1 = new VectorClock(); test2 = new VectorClock();
	}
	
	@Test
	@Order(1)
	void testInc() {
		test1.inc("a");
		test1.inc("a");
		test1.inc("a");
		test2.inc("b");
		assertSame(test1.getLogicalTime("a"),3L);
		assertSame(test2.getLogicalTime("b"),1L);
	}

	@Test
	@Order(2)
	void testSetLogicalTime() {
		test1.setLogicalTime("a", 5L);
		test2.setLogicalTime("b", 4L);
		assertSame(test1.getLogicalTime("a"),5L);
		assertSame(test2.getLogicalTime("b"),4L);
	}

	@Test
	@Order(3)
	void testGetLogicalTime() {
		test2.setLogicalTime("d", 1L);
		assertSame(test1.getLogicalTime("a"),5L);
		assertSame(test2.getLogicalTime("b"),4L);
		assertSame(test1.getLogicalTime("c"),0L);
		assertSame(test2.getLogicalTime("d"),1L);
	}

	@Test
	@Order(4)
	void testGetLogicalTimes() {
		Set<Entry<String,Long>> logicalTimes = test1.getLogicalTimes();
		assertSame(logicalTimes.size(), 1);
	
	}

	@Test
	@Order(5)
	void testGetReplicaIds() {
		Set<String> replicaIds = test2.getReplicaIds();
		assertSame(replicaIds.size(), 2);
	}

	@Test
	@Order(6)
	void testMerge() {
		test1.merge(test2);
		Set<String> replicaIds = test1.getReplicaIds();
		assertSame(replicaIds.size(), 3);
	}

	@Test
	@Order(7)
	void testCompareTo() {
		
		VectorClock test3 = new VectorClock();
		VectorClock test4 = new VectorClock();
		
		assertSame(test3.compareTo(test4),Ord.Eq.getValue());
		test3.inc("a");
		test4.inc("b");
		
		assertNotSame(test3.compareTo(test4), Ord.Eq.getValue());
		assertSame(test3.compareTo(test4), Ord.Cc.getValue());
		
		test3.inc("b");
		assertSame(test3.compareTo(test4),Ord.Gt.getValue());
		
		test4.inc("a");
		test4.inc("c");
		assertSame(test3.compareTo(test4),Ord.Lt.getValue());
		
		test3.inc("c");
		assertSame(test3.compareTo(test4),Ord.Eq.getValue());
	}

}
