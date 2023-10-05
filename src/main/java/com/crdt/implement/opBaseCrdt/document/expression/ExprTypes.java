package com.crdt.implement.opBaseCrdt.document.expression;

import com.crdt.implement.opBaseCrdt.document.keyType.IndexK;
import com.crdt.implement.opBaseCrdt.document.keyType.StringK;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class ExprTypes {
	
	@AllArgsConstructor
	@Getter
	@Setter
	public static class Doc implements Expr{
		private Expr expr;
	}
	
	@AllArgsConstructor
	@Getter
	@Setter
	public static class Var implements Expr{
		private String name;
		private Expr expr;
		
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
	@Setter
	public static class Get implements Expr{
		private Expr expr;
		private String key;
	}
	
	@AllArgsConstructor
	@Getter
	@Setter
	public static class Index implements Expr{	
		private Expr expr;
		private int Index;
		private String replicaId;
	}
	
}
