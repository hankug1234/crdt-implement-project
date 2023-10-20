package com.crdt.implement.opBaseCrdt.document;

import java.util.HashMap;
import java.util.Map;

import com.crdt.implement.opBaseCrdt.document.cursor.Cursor;
import com.crdt.implement.opBaseCrdt.document.expression.ExprTypes.Var;
import com.crdt.implement.opBaseCrdt.document.node.Node;
import com.crdt.implement.opBaseCrdt.document.values.Text;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Document {

	private Map<Var,Cursor> variables;
	private Node document;
	private Cursor var;
	
	public Document(Node document) {
		this.document = document;
		this.variables = new HashMap<>();
		this.var = null;
	}
	
	public Document(Map<Var,Cursor> variables,Node document) {
		this.variables = variables;
		this.document = document;
		this.var = null;
	}
	
	public Document clone() {
		Map<Var,Cursor> newVariables = new HashMap<>(variables);
		return new Document(newVariables,document.clone());
	}
	
	public void setVar(Cursor cur) {
		this.var = cur;
	}
	
}
