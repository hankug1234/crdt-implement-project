package com.crdt.implement.opBaseCrdt.document;

import java.util.HashMap;
import java.util.Map;

import com.crdt.implement.opBaseCrdt.document.cursor.Cursor;
import com.crdt.implement.opBaseCrdt.document.expression.ExprTypes.Var;
import com.crdt.implement.opBaseCrdt.document.node.Node;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Document {

	private Map<Var,Cursor> variables;
	private Node document;
	private String replicaId;
	
	public Document(Node document) {
		this.document = document;
		this.variables = new HashMap<>();
	}
}
