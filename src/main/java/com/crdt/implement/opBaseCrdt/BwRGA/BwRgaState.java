package com.crdt.implement.opBaseCrdt.BwRGA;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BwRgaState<A> {
	private BwRgaVPtr sequencer;
	private List<Block<A>> blocks;
	
	public BwRgaState(String replicaId) {
		this.sequencer = new BwRgaVPtr(0,replicaId);
		this.blocks = new ArrayList<>();
		this.blocks.add(new Block<>(new BwRgaVPtrOff(new BwRgaVPtr(0,"start"),0),new Content<A>(0)));
	}
	
	public BwRgaState<A> clone(){
		List<Block<A>> newBlocks = new ArrayList<>();
		for(Block<A> block : blocks) {
			newBlocks.add(block.clone());
		}
		return new BwRgaState<>(this.sequencer,newBlocks);
	}

}
