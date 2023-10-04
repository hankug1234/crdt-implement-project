package com.crdt.implement.opBaseCrdt.document.expression;

import com.crdt.implement.opBaseCrdt.document.keyType.StringK;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ExprTypes {

	public static class Doc implements Expr{}
	
	@AllArgsConstructor
	@Getter
	public static class Var implements Expr{
		private String name;
		
		@Override
		public int hashCode() {
			return 0;
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof Var) {
				Var var = (Var) o;
				if(var.getName().equals(this.name)) {
					return true;
				}
				
				return false;
			}
			return false;
		}
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
