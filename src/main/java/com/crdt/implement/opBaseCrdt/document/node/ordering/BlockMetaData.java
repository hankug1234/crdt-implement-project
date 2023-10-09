package com.crdt.implement.opBaseCrdt.document.node.ordering;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class BlockMetaData {

	private Optional<OrderId> left;
	private Optional<OrderId> right;
	private long seqNr;
}
