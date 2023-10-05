package com.crdt.implement.opBaseCrdt.document.signal;

import com.crdt.implement.opBaseCrdt.document.cursor.Cursor;
import com.crdt.implement.opBaseCrdt.document.values.Val;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class SignalTypes {
	
	@AllArgsConstructor
	@Getter
	public static class AssignS implements Signal{
		private Val value;
	}
	
	@AllArgsConstructor
	@Getter
	public static class InsertS implements Signal{
		private Val value;
		private int index;
	}
	
	public static class DeleteS implements Signal{
		
	}
	
	@AllArgsConstructor
	@Getter
	public static class MoveS implements Signal{
		private Cursor target;
	}
}
