package com.crdt.implement.opBaseCrdt.document.expression;

public interface Expr {

	public void setExpr(Expr expr);
	public Expr clone();
}
