package com.crdt.implement.opBaseCrdt.document.values;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Str extends LeafVal{

	private String value;
	
	@Override 
	public String toString() {
		return value.toString();
	}
}
