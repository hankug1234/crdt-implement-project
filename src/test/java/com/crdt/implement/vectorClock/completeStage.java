package com.crdt.implement.vectorClock;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

public class completeStage {
	@Test
	void completionStageTest1() {
		CompletionStage<Double> test1 = CompletableFuture.supplyAsync(()->{
			try {
				Thread.sleep(1000); return (Double)1.0;
			}catch(Exception e){
				return (Double)0.0;
			}
			
		});
		
		test1.whenComplete((value,ex)->{
			if(ex == null) {
				System.out.println(value);
			}else {
				System.out.println(value);
			}
		});
		
		assertSame(true,true);
	}
	
	@Test
	void completionStageTest2() {
		CompletionStage<Double> test2 = CompletableFuture.supplyAsync(()->{
			try {
				Thread.sleep(1000); return (Double)1.0;
			}catch(Exception e){
				return (Double)0.0;
			}
		});
		
		test2.whenCompleteAsync((value,ex)->{
			if(ex == null) {
				System.out.println(value);
			}else {
				System.out.println(value);
			}
		});
		assertSame(true,true);
	}
}
