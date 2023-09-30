package com.crdt.implement.opBaseCrdt.document.cursor;

import com.crdt.implement.opBaseCrdt.document.keyType.Key;
import com.crdt.implement.opBaseCrdt.document.typetag.BranchTag;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ViewTypes {

	@AllArgsConstructor
	@Getter
	public static class Leaf implements View{
		private Key finalKey;
	}
	
	@AllArgsConstructor
	@Getter
	public static class Branch implements View{
		private BranchTag head;
		private Cursor tail;
	}
}
