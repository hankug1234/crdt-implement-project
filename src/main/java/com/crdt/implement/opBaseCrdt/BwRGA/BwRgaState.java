package com.crdt.implement.opBaseCrdt.BwRGA;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BwRgaState<A> {
	private BwRgaVPtr sequencer;
	private List<Block<A>> blocks;

}
