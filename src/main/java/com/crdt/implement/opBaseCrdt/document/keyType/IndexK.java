package com.crdt.implement.opBaseCrdt.document.keyType;

import com.crdt.implement.opBaseCrdt.document.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class IndexK implements Key{
	private String id;
	private String replicaId;
	
	@Override
	public String toString() {
		return replicaId +" : " + id;
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof IndexK) {
			IndexK i = (IndexK) o;
			if(i.getId().equals(this.id)) {
				return true;
			}
			return false;
		}
		return false;
	}
}
