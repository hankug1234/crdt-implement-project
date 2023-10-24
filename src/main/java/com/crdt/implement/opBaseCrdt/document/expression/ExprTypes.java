package com.crdt.implement.opBaseCrdt.document.expression;

import java.util.LinkedList;
import java.util.List;

import com.crdt.implement.opBaseCrdt.document.keyType.IndexK;
import com.crdt.implement.opBaseCrdt.document.keyType.StringK;
import com.crdt.implement.opBaseCrdt.document.typetag.BranchTag;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes;
import com.crdt.implement.opBaseCrdt.document.typetag.TypeTag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class ExprTypes {
	
	public static class Root{
		List<Expr> paths = new LinkedList<>();
		
		public Root dict(String key) {
			paths.add(new Get(new TagTypes.MapT(new StringK(key))));
			return this;
		}
		
		public Root list(String key) {
			paths.add(new Get(new TagTypes.ListT(new StringK(key))));
			return this;
		}
		
		public Root index(int i) {
			paths.add(new Index(i));
			return this;
		}
		
		public Expr build() {
			Expr result = new Doc();
			Expr next = result;
			for(Expr expr : paths) {
				next.setExpr(expr);
				next = expr;
			}
			return result;
		}
	}
	
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
