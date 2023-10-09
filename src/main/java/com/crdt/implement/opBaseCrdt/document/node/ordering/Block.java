package com.crdt.implement.opBaseCrdt.document.node.ordering;

import java.util.Optional;

import com.crdt.implement.opBaseCrdt.document.Id;
import com.crdt.implement.opBaseCrdt.document.keyType.IndexK;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;
import com.crdt.implement.opBaseCrdt.document.node.Node;
import com.crdt.implement.opBaseCrdt.document.typetag.TypeTag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Block {
	private OrderId id;
	private Optional<OrderId> left;
	private Optional<OrderId> right;
	private Optional<OrderId> movedTo;
	private Content value;
	
	public void setTombstone() {
		this.value.setTombstone();
	}
	
	public boolean isTombstone() {
		return value.isTombstone();
	}
	
	public TypeTag value() {
		return value.getContent().get();
	}
}
