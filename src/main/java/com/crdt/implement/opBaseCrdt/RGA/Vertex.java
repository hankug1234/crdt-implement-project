package com.crdt.implement.opBaseCrdt.RGA;

import java.util.Optional;

import lombok.Getter;

@Getter
public class Vertex<A> {

	private RgaVPtr vptr;
	private Optional<A> value;
	
	public Vertex(RgaVPtr vptr, A value) {
		this.vptr = vptr; this.value = Optional.of(value);
	}
	
	public Vertex(RgaVPtr vptr) {
		this.vptr = vptr;
		this.value = Optional.of(null);
	}
	
	public Vertex<A> clone(){
		if(value.isPresent()) {
			return new Vertex(this.vptr,Optional.of(this.value.get()));
		}else {
			return new Vertex(this.vptr);
		}
	}
	
	public void setTumbstone() {
		this.value = Optional.of(null);
	}
	
	public boolean isTumbstone() {
		return this.value.isPresent();
	}
}
