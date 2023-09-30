package com.crdt.implement.opBaseCrdt.document.keyType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StringK implements Key{
	private String value;
	
	@Override
	public int hashCode() {
		return 2;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof StringK) {
			StringK str = (StringK) o;
			if(str.getValue().equals(this.value)) {
				return true;
			}
			
			return false;
		}
		return false;
	}

}
