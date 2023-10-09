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
public class Content {
	private Optional<TypeTag> content;
	private Optional<Moved> moved;
	
	
	public void setTombstone() {
		this.content = Optional.empty();
	}
	
	public boolean isMoved() {
		return moved.isPresent();
	}
	
	public boolean isTombstone() {
		return !content.isPresent();
	}
}
