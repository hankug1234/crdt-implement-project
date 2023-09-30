package com.crdt.implement.opBaseCrdt.document.keyType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class IndexK implements Key{
	private int index;
	
	@Override
	public int hashCode() {
		return 1;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof IndexK) {
			IndexK i = (IndexK) o;
			if(i.getIndex() == this.index) {
				return true;
			}
			return false;
		}
		return false;
	}
}
