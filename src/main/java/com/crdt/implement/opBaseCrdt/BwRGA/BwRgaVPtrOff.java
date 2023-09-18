package com.crdt.implement.opBaseCrdt.BwRGA;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BwRgaVPtrOff {
	private BwRgaVPtr vptr;
	private int offset;//전체 데이터 상에서의 길이 
	
	public boolean equals(BwRgaVPtrOff other) {
		if(this.vptr.equals(other.getVptr()) && this.offset == other.getOffset()) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return vptr.toString()+", offset : "+offset;
	}
}
