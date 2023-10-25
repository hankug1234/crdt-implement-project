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
		public List<Expr> paths;
		
		private Root() {
			this.paths = new LinkedList<>();
		}
		
		private Root(List<Expr> exprs) {
			this.paths = exprs;
		}
		
		public Root clone() {
			List<Expr> result = new LinkedList<>();
			result.addAll(this.paths);
			return new Root(result);
		}
		
		public static Root value(String valueName) {
			
			List<Expr> paths = new LinkedList<>();
			paths.add(new ExprTypes.Var(valueName));
			
			return new Root(paths);
		}
		
		public static Root document() {
			
			List<Expr> paths = new LinkedList<>();
			paths.add(new ExprTypes.Doc());
			
			return new Root(paths);
		}
		
		public Root dict(String key) {
			paths.add(new Get(new TagTypes.MapT(new StringK(key))));
			return this;
		}
		
		public Root list(String key) {
			paths.add(new Get(new TagTypes.ListT(new StringK(key))));
			return this;
		}
		
		public Root reg(String key) {
			paths.add(new Get(new TagTypes.RegT(new StringK(key))));
			return this;
		}
		
		public Root index(int i) {
			paths.add(new Index(i));
			return this;
		}
		
		public Expr build() {
			if(paths.size() > 0) {
				Expr result = paths.get(0).clone();
				Expr next = result;
				for(int i=1; i<paths.size();i++) {
					Expr cur = paths.get(i).clone();
					next.setExpr(cur);
					next = cur;
				}
				return result;
			}
			return null;
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
		
		public Expr clone() {
			return new Doc();
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
		
		public Expr clone() {
			return new Var(name);
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
		
		public Expr clone() {
			return new Get(tag);
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
		
		public Expr clone() {
			return new Index(index);
		}
	}
	
}
