package com.crdt.implement.PureOpBaseCrdt;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
@Slf4j
class PureCounterTest {
	
	@Test
	void test() {
		PureCounter c = new PureCounter("c", Duration.ofSeconds(1));
		PureCounter d = new PureCounter("d", Duration.ofSeconds(1));
		
		try {
			
			 
			d.connect(c);
			Thread.sleep(3000);
			c.connect(d);
			//c.inc(10L); 
			//Thread.sleep(5000);
			//assertSame(c.query(),10L);
			//assertSame(b.query(),10L);
		}catch(Exception e) {
			
			log.info(e.toString());
			
			assertTrue(false);
		}
		
	}

}
