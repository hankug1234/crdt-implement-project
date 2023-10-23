package com.crdt.implement.opBaseCrdt.document;

import com.crdt.implement.opBaseCrdt.document.cursor.Cursor;
import com.crdt.implement.opBaseCrdt.document.signal.Signal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Operation {
	private Id id;
	private Cursor cur;
	private Signal signal;

	public void setId(Id id) {
		this.id = id;
	}
	
	
}
