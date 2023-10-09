package com.crdt.implement.opBaseCrdt.document.values;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Number extends LeafVal{
	
	private double value;
	
	@Override 
	public String toString() {
		return String.valueOf(value);
	}

}
