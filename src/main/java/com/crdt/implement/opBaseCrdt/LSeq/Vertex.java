package com.crdt.implement.opBaseCrdt.LSeq;

import lombok.Getter;

@Getter
public class Vertex <A>{
	private LSeqVPtr vptr;
	private A data; 
	
	public Vertex(LSeqVPtr vptr, A data) {
		this.vptr = vptr; this.data = data;
	}
}
