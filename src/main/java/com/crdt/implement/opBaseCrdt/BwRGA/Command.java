package com.crdt.implement.opBaseCrdt.BwRGA;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class Command {
	@AllArgsConstructor
	@Getter
	public static class Insert<A> implements BwRgaCommand<A>{
		private int index;
		private List<A> values;
	}
	
	@AllArgsConstructor
	@Getter
	public static class RemoveAt<A> implements BwRgaCommand<A>{
		private int index;
		private int count;
	}
	
}
