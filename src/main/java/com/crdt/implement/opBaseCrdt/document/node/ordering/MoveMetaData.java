package com.crdt.implement.opBaseCrdt.document.node.ordering;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class MoveMetaData {
	private String replicaId;
	private long seqNr;
	private OrderId src;
	private OrderId dst;
	private int priority;
}
