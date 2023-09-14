package com.crdt.implement.opBaseCrdt.RGA;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class RgaState<A> {
	
	private List<Vertex<A>> state;
	private RgaVPtr sequencer;
	
	public RgaState(List<Vertex<A>> state,RgaVPtr sequencer) {
		this.state = state; 
		this.sequencer = sequencer;
	}
	
	public RgaState(String replicaId) {
		this.state = new ArrayList<>();
		this.sequencer = new RgaVPtr(replicaId,0);
		this.state.add(new Vertex<>(new RgaVPtr("s",0)));
	}

}
