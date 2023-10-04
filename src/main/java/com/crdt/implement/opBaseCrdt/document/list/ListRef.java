package com.crdt.implement.opBaseCrdt.document.list;

public interface ListRef {

	public ListRef clone();
	public int hashCode(); 
	public boolean equals(Object o);
}
