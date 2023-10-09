package com.crdt.implement.opBaseCrdt.document.values;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Bool extends LeafVal{

	private boolean value;
	
	@Override 
	public String toString() {
		return String.valueOf(value);
	}
}
