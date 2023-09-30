package com.crdt.implement.opBaseCrdt.document.list;

import com.crdt.implement.opBaseCrdt.document.keyType.HeadK;
import com.crdt.implement.opBaseCrdt.document.keyType.IndexK;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class RefTypes {

	public static ListRef keyToR(Key key) {
		if(key instanceof IndexK) {
			IndexK index = (IndexK) key;
			return new IndexR(index.getIndex());
		}else if(key instanceof HeadK) {
			return new HeadR();
		}else {
			return new TailR();
		}
	}
	
	public static Key RtoKey(ListRef ref) {
		if(ref instanceof IndexR) {
			IndexR index = (IndexR) ref;
			return new IndexK(index.getIndex());
		}else if(ref instanceof HeadR) {
			return new HeadK();
		}else {
			return null;
		}
	}
	
	@AllArgsConstructor
	@Getter
	public static class IndexR implements ListRef{
		private int index;
		
		@Override
		public int hashCode() {
			return 1;
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof IndexR) {
				IndexR i = (IndexR) o;
				if(i.getIndex() == this.index) {
					return true;
				}
				
				return false;
			}
			return false;
		}
	}
	
	public static class HeadR implements ListRef{
		@Override
		public int hashCode() {
			return 0;
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof HeadR) {
				return true;
			}
			return false;
		}
	}
	
	public static class TailR implements ListRef{
		@Override
		public int hashCode() {
			return 1;
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof TailR) {
				return true;
			}
			return false;
		}
	}
}
