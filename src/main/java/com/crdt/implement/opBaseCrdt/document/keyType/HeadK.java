package com.crdt.implement.opBaseCrdt.document.keyType;

public class HeadK implements Key{

	@Override
	public int hashCode() {
		return 0;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof HeadK) {
			return true;
		}
		return false;
	}
}
