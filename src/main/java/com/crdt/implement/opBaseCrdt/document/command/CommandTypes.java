package com.crdt.implement.opBaseCrdt.document.command;

import com.crdt.implement.opBaseCrdt.document.expression.Expr;
import com.crdt.implement.opBaseCrdt.document.expression.ExprTypes;
import com.crdt.implement.opBaseCrdt.document.signal.LocationInstructor;
import com.crdt.implement.opBaseCrdt.document.values.Val;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class CommandTypes {
	
	@AllArgsConstructor
	@Getter
	public static class Let implements Command{
		private ExprTypes.Var x;
		private Expr expr;
	}
	
	@AllArgsConstructor
	@Getter
	public static class Assign implements Command{
		private Expr expr;
		private Val value;
	}
	
	@AllArgsConstructor
	@Getter
	public static class Insert implements Command{
		private Expr expr;
		private Val value;
		private int index;
	}
	
	@AllArgsConstructor
	@Getter
	public static class Delete implements Command{
		private Expr expr;
	}
	
	@AllArgsConstructor
	@Getter
	public static class Move implements Command{
		private Expr src;
		private Expr tar;
	}
	
	@AllArgsConstructor
	@Getter
	public static class Sequence implements Command{
		private Command command1;
		private Command command2;
	}
}
