package com.crdt.implement.opBaseCrdt.BwRGA;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class Data {
	
	@AllArgsConstructor
	@Getter
	public static class Inserted<A> implements BwRgaData<A>{
		private BwRgaVPtrOff after;
		private BwRgaVPtr at;
		private List<A> values;
	}
	
	@AllArgsConstructor
	@Getter
	public static class RemovedRange{
		private BwRgaVPtrOff startOff;
		private int range;
	}
	
	@AllArgsConstructor
	@Getter
	public static class Removed<A> implements BwRgaData<A>{
		List<RemovedRange> removeds;
	}
}
