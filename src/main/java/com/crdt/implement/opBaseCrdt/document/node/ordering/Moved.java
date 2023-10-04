package com.crdt.implement.opBaseCrdt.document.node.ordering;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Moved {
	private OrderId from;
	private int priority;
}
