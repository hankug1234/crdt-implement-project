package com.crdt.implement.opBaseCrdt.document.keyType;

import com.crdt.implement.opBaseCrdt.document.expression.ExprTypes.Doc;

public class Dock implements Key{

	@Override
	public String toString() {
		return "dock";
	}
	
	@Override
	public int hashCode() {
		return -1;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Doc) {
			return true;
		}
		return false;
	}
}
