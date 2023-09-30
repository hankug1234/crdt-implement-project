package com.crdt.implement.opBaseCrdt.document.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ExprTypes {

	public static class Doc implements Expr{}
	
	@AllArgsConstructor
	@Getter
	public static class Var implements Expr{
		private String name;
	}
	
	@AllArgsConstructor
	@Getter
	public static class Get implements Expr{
		private Expr expr;
		private String key;
	}
	
	@AllArgsConstructor
	@Getter
	public static class Iter implements Expr{
		
		private Expr expr;
	}
	
	@AllArgsConstructor
	@Getter
	public static class Next implements Expr{
		private Expr expr;
	}
}
