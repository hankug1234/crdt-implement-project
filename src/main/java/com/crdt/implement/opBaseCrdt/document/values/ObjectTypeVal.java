package com.crdt.implement.opBaseCrdt.document.values;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ObjectTypeVal extends LeafVal{
	private String type;
}
