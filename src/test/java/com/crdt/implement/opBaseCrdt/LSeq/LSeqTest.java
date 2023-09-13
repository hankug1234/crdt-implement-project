package com.crdt.implement.opBaseCrdt.LSeq;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.crdt.implement.opBaseCrdt.counter.Counter;
import com.crdt.implement.persistence.InMemoryCrdtDB;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LSeqTest {

	Map<String,Long> map;
	
	@BeforeAll
	void initialize() {
		map = new HashMap<>();
		map.put("a", 1L);
		map.put("b", 1L);
		log.info("map : "+map.toString());
	}
	
	
	@Test
	void testQuery() {
		log.info("map : "+map.toString());
		
	}

	@Test
	void testInsert() {
		
	}

	@Test
	void testRemoveAt() {
		
	}

}
