package com.crdt.implement.opBaseCrdt.document;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class JsonTypeTest {

	@Test
	void test() {
		Json json = new JsonType.Value(new DataTypes.IntegerType(11));
		assertEquals(json.toString(),"11");
	}

}
