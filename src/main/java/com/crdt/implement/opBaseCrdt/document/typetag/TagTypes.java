package com.crdt.implement.opBaseCrdt.document.typetag;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class TagTypes {

	@AllArgsConstructor
	@Getter
	public static class MapT extends BranchTag{
		private Key key;
		
		@Override
		public String toString() {
			return "mapT = "+key.toString();
		}
		
		@Override
		public int hashCode() {
			return this.toString().hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof MapT) {
				MapT m = (MapT) o;
				if(m.getKey().equals(this.key)) {
					return true;
				}
				return false;
			}
			return false;
		}
	}
	
	@AllArgsConstructor
	@Getter
	public static class ListT extends BranchTag{
		private Key key;
		
		@Override
		public String toString() {
			return "listT = "+key.toString();
		}
		
		@Override
		public int hashCode() {
			return this.toString().hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof ListT) {
				ListT l = (ListT) o;
				if(l.getKey().equals(l.getKey())) {
					return true;
				}
				return false;
			}
			return false;
		}
	}
	
	@AllArgsConstructor
	@Getter
	public static class RegT extends LeafTag{
		private Key key;
		
		@Override
		public String toString() {
			return "regT = "+key.toString();
		}
		
		@Override
		public int hashCode() {
			return this.toString().hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof RegT) {
				RegT r = (RegT) o;
				if(r.getKey().equals(this.getKey())) {
					return true;
				}
				return false;
			}
			return false;
		}
	}
}
