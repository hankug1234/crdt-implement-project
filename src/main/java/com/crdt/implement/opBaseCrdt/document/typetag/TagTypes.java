package com.crdt.implement.opBaseCrdt.document.typetag;

import com.crdt.implement.opBaseCrdt.document.keyType.Key;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class TagTypes {

	@AllArgsConstructor
	@Getter
	public static class MapT extends BranchTag{
		private Key key;
	}
	
	@AllArgsConstructor
	@Getter
	public static class ListT extends BranchTag{
		private Key key;
	}
	
	@AllArgsConstructor
	@Getter
	public static class RegT extends LeafTag{
		private Key key;
	}
}
