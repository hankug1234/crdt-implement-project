package com.crdt.implement.PureOpBaseCrdt;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
@Slf4j
class PureCounterTest {
	
	@Test 
	void test2() {
		PureCounter a = new PureCounter("a", Duration.ofMillis(500));
		PureCounter b = new PureCounter("b", Duration.ofMillis(500));
		PureCounter c = new PureCounter("c", Duration.ofMillis(500));
		
		a.connect(b);
		b.connect(a);
		
		c.connect(b);
		b.connect(c);
		
		try {
			a.inc(1L);
			c.inc(1L);
			
			Thread.sleep(2000);
			
			assertSame(a.query(),2L);
			assertSame(c.query(),2L);
			assertSame(b.query(),2L);
			
			b.inc(1L);
			
			Thread.sleep(2000);
			
			assertSame(a.query(),3L);
			assertSame(c.query(),3L);
			assertSame(b.query(),3L);
			
			c.inc(1L);
			b.inc(1L);
			a.inc(1L);
		
			Thread.sleep(5000);
			
			assertSame(a.query(),6L);
			assertSame(c.query(),6L);
			assertSame(b.query(),6L);
			
		}catch(Exception e) {
			assertTrue(false);
		}
		
		
	}
	
	@Test
	void test() {
		PureCounter c = new PureCounter("c", Duration.ofSeconds(1));
		PureCounter d = new PureCounter("d", Duration.ofSeconds(1));
		
		try {
			
			 
			d.connect(c);
			c.connect(d);
			c.inc(10L); 
			Thread.sleep(3000);
			assertSame(c.query(),10L);
			assertSame(d.query(),10L);
			d.inc(11L);
			Thread.sleep(1000);
			assertSame(c.query(),21L);
			
			c.evict(d);
			
			c.inc(10L);
			
			Thread.sleep(1000);
			
			assertSame(c.query(),31L);
			//assertSame(d.query(),21L);
			
		}catch(Exception e) {
			
			log.info(e.toString());
			
			assertTrue(false);
		}
		
	}

}
