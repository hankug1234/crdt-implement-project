package com.crdt.implement.opBaseCrdt.document;

import com.crdt.implement.opBaseCrdt.document.keyType.HeadK;
import com.crdt.implement.vectorClock.Ord;
import com.crdt.implement.vectorClock.VectorClock;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public class Id {

	private String replicaId;
	private VectorClock vectorClock;
	
	public int compareTo(Id other) {
		int cmp = this.vectorClock.compareTo(other.getVectorClock());
		return cmp;
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Id) {
			Id id = (Id) o;
			if(id.compareTo(this) == 0) {
				return true;
			}
			return false;
		}
		return false;
	}
}
