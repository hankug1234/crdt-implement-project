package com.crdt.implement.opBaseCrdt.document.typetag;

import com.crdt.implement.opBaseCrdt.document.keyType.Key;

public interface TypeTag {
	public Key getKey();
	public String toString();
}
