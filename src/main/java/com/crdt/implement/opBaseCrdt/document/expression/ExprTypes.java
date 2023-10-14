package com.crdt.implement.opBaseCrdt.document.expression;

import com.crdt.implement.opBaseCrdt.document.keyType.IndexK;
import com.crdt.implement.opBaseCrdt.document.keyType.StringK;
import com.crdt.implement.opBaseCrdt.document.typetag.BranchTag;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes;
import com.crdt.implement.opBaseCrdt.document.typetag.TypeTag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class ExprTypes {
	
	@AllArgsConstructor
	@Getter
	@Setter
	public static class Doc implements Expr{
		private Expr expr;
		
		public Doc() {
			this.expr = null;
		}
	}
	
	@AllArgsConstructor
	@Getter
	@Setter
	public static class Var implements Expr{
		private String name;
		private Expr expr;
		
		public Var(String name) {
			this.name = name; 
			this.expr = null;
		}
		
		
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
		private TypeTag tag;
		private Expr expr;
		
		public Get(TypeTag tag) {
			this.tag = tag;
			this.expr = null;
		}
		
	}
	
	@AllArgsConstructor
	@Getter
	@Setter
	public static class Index implements Expr{	
		private int index;
		private Expr expr;
		
		public Index(int index) {
			this.index = index;
			this.expr = null;
		}
	}
	
}
