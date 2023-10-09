package com.crdt.implement.opBaseCrdt.document.node.ordering;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.crdt.implement.opBaseCrdt.document.keyType.IndexK;
import com.crdt.implement.opBaseCrdt.document.typetag.TypeTag;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes.ListT;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes.MapT;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderListTest {

	public static OrderList test1 = new OrderList();
	public static OrderList test2 = new OrderList();
	public static OrderList test3 = new OrderList();
	public static OrderList test4 = new OrderList();
	
	private static TypeTag generator(int index,String replicaId ,int type) {
		if(type == 0) {
			return new TagTypes.RegT(new IndexK(UUID.randomUUID().toString(),replicaId));
		}else if(type == 1) {
			return new ListT(new IndexK(UUID.randomUUID().toString(),replicaId));
		}else {
			return new MapT(new IndexK(UUID.randomUUID().toString(),replicaId));
		}
	}
	
	@BeforeAll
	public static void testInit() {
		test1.insert(0, generator(0,"c",0));
		test1.insert(0, generator(0,"b",1));
		test1.insert(0, generator(0,"a",2));
		
		test2.insert(0,generator(0,"c",2));
		test2.insert(1,generator(0,"b",2));
		test2.insert(2,generator(0,"a",2));
		
		
		test3.insert(0,generator(0,"c",2));
		test3.insert(1,generator(0,"b",2));
		test3.insert(2,generator(0,"a",2));
		test3.insert(3,generator(0,"a",2));
		test3.insert(4,generator(0,"c",2));
		test3.insert(2,generator(0,"c",2));
		
		
	}
	
	
	@Test
	void testClone() {
		fail("Not yet implemented");
	}

	@Test
	void testGetOrderList() {
		fail("Not yet implemented");
	}

	@Test
	void testGetList() {
		fail("Not yet implemented");
	}

	@Test
	@Order(3)
	void testDelete() {

		test3.delete(5);
		test3.delete(4);
		
		List<String> replicaIds3 = test3.getOrderList().stream().map(t->(IndexK) t.getKey()).map(k->k.getReplicaId()).toList();
		replicaIds3.stream().forEach(e->log.info(e));
		assertEquals(replicaIds3.get(0),"c");
		assertEquals(replicaIds3.get(1),"c");
		assertEquals(replicaIds3.get(2),"b");
		assertEquals(replicaIds3.get(3),"a");
	}

	@Test
	@Order(1)
	void testInsert() {
		List<String> replicaIds1 = test1.getOrderList().stream().map(t->(IndexK) t.getKey()).map(k->k.getReplicaId()).toList();
		assertEquals(replicaIds1.get(0),"a");
		assertEquals(replicaIds1.get(1),"b");
		assertEquals(replicaIds1.get(2),"c");
		
		List<String> replicaIds2 = test2.getOrderList().stream().map(t->(IndexK) t.getKey()).map(k->k.getReplicaId()).toList();
		assertEquals(replicaIds2.get(0),"c");
		assertEquals(replicaIds2.get(1),"b");
		assertEquals(replicaIds2.get(2),"a");
		
		List<String> replicaIds3 = test3.getOrderList().stream().map(t->(IndexK) t.getKey()).map(k->k.getReplicaId()).toList();
		assertEquals(replicaIds3.get(0),"c");
		assertEquals(replicaIds3.get(1),"b");
		assertEquals(replicaIds3.get(2),"c");
		assertEquals(replicaIds3.get(3),"a");
		assertEquals(replicaIds3.get(4),"a");
		assertEquals(replicaIds3.get(5),"c");
	}

	@Test
	void testMoveByKey() {
		fail("Not yet implemented");
	}

	@Test
	@Order(2)
	void testMoveByIndex() {
		test3.moveByIndex(1, 3);
		List<String> replicaIds3 = test3.getOrderList().stream().map(t->(IndexK) t.getKey()).map(k->k.getReplicaId()).toList();
		//replicaIds3.stream().forEach(e->log.info(e));
		assertEquals(replicaIds3.get(0),"c");
		assertEquals(replicaIds3.get(1),"c");
		assertEquals(replicaIds3.get(2),"b");
		assertEquals(replicaIds3.get(3),"a");
		assertEquals(replicaIds3.get(4),"a");
		assertEquals(replicaIds3.get(5),"c");
		
		test3.moveByIndex(3, 6);
		replicaIds3 = test3.getOrderList().stream().map(t->(IndexK) t.getKey()).map(k->k.getReplicaId()).toList();
		assertEquals(replicaIds3.get(0),"c");
		assertEquals(replicaIds3.get(1),"c");
		assertEquals(replicaIds3.get(2),"b");
		assertEquals(replicaIds3.get(3),"a");
		assertEquals(replicaIds3.get(4),"c");
		assertEquals(replicaIds3.get(5),"a");
	}

	@Test
	void testGetLastSeqNr() {
		fail("Not yet implemented");
	}

}
