package com.crdt.implement.opBaseCrdt.document.node;

import java.util.List;

import com.crdt.implement.opBaseCrdt.document.values.LeafVal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LeafNode implements Node{
	private List<LeafVal> values;
	
}
