package com.crdt.implement.opBaseCrdt.document;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.json.JSONObject;

import com.crdt.implement.opBaseCrdt.OpBaseCrdtOperation;
import com.crdt.implement.opBaseCrdt.document.command.Command;
import com.crdt.implement.opBaseCrdt.document.command.CommandTypes;
import com.crdt.implement.opBaseCrdt.document.cursor.Cursor;
import com.crdt.implement.opBaseCrdt.document.expression.Expr;
import com.crdt.implement.opBaseCrdt.document.expression.ExprTypes.Doc;
import com.crdt.implement.opBaseCrdt.document.expression.ExprTypes.Get;
import com.crdt.implement.opBaseCrdt.document.expression.ExprTypes.Iter;
import com.crdt.implement.opBaseCrdt.document.expression.ExprTypes.Next;
import com.crdt.implement.opBaseCrdt.document.expression.ExprTypes.Var;
import com.crdt.implement.opBaseCrdt.document.keyType.HeadK;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;
import com.crdt.implement.opBaseCrdt.document.keyType.StringK;
import com.crdt.implement.opBaseCrdt.document.node.BranchNode;
import com.crdt.implement.opBaseCrdt.document.node.LeafNode;
import com.crdt.implement.opBaseCrdt.document.node.ListNode;
import com.crdt.implement.opBaseCrdt.document.node.MapNode;
import com.crdt.implement.opBaseCrdt.document.node.Node;
import com.crdt.implement.opBaseCrdt.document.signal.Signal;
import com.crdt.implement.opBaseCrdt.document.signal.SignalTypes;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes.ListT;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes.MapT;
import com.crdt.implement.reliableBroadcast.OpBaseEvent;
import com.crdt.implement.vectorClock.VectorClock;

public class OpBaseDocOperation implements OpBaseCrdtOperation<Document,JSONObject,List<Command>,List<Operation>>{

	@Override
	public Document Default() {
		return new Document(new MapNode());
	}

	@Override
	public JSONObject Query(Document crdt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Document copy(Document crdt) {
		return null;
	}

	@Override
	public List<Operation> Prepare(Document crdt, List<Command> command) {
		return command.stream().map(
				(Command cmd) -> {
					Operation op;
					if(cmd instanceof CommandTypes.Assign) {
						
						CommandTypes.Assign assign = (CommandTypes.Assign) cmd;
						op = makeOp(evalExpr(crdt,assign.getExpr()),new SignalTypes.AssignS(assign.getValue()));
						
					}else if(cmd instanceof CommandTypes.Delete) {
						
						CommandTypes.Delete delete = (CommandTypes.Delete) cmd;
						op = makeOp(evalExpr(crdt,delete.getExpr()),new SignalTypes.DeleteS());
						
					}else if(cmd instanceof CommandTypes.Insert) {
						
						CommandTypes.Insert insert = (CommandTypes.Insert) cmd;
						op = makeOp(evalExpr(crdt,insert.getExpr()),new SignalTypes.InsertS(insert.getValue()));
						
					}else if(cmd instanceof CommandTypes.Move) {
						
						CommandTypes.Move move = (CommandTypes.Move) cmd;
						op = makeOp(evalExpr(crdt,move.getSrc()),new SignalTypes.MoveS(evalExpr(crdt,move.getTar()),move.getLocation()));
						
					}else if(cmd instanceof CommandTypes.Let) {
						CommandTypes.Let let = (CommandTypes.Let) cmd;
						crdt.getVariables().put(let.getX(),evalExpr(crdt,let.getExpr()));
						return Optional.empty();
					}
					else {
						throw new RuntimeException();
					}
					return Optional.of(op);
				}
				
		).filter(o->o.isPresent()).map(o->(Operation)o.get()).toList();
	}

	@Override
	public Document Effect(Document crdt, OpBaseEvent<List<Operation>> event) {
		VectorClock vectorClock = event.getVectorClock();
		String replicaId = event.getOriginReplicaId();
		List<Operation> data = event.getData();
		
		Node node = crdt.getDocument();
		
		for(Operation op : data) {
			op.setId(new Id(replicaId,vectorClock));
			node.applyOp(op, null);
		}
		
		return crdt;
	}
	
	public Operation makeOp(Cursor cur, Signal signal) {
		return new Operation(null,cur,signal);
	}
	
	private Cursor go(Document crdt, Expr expr, List<Function<Cursor,Cursor>> fs) {
		Function<Cursor,Cursor> f; Expr nextExpr;
		if(expr instanceof Doc) {
			return applyAllLeft(fs,Cursor.doc());
		}else if (expr instanceof Var) {
			Var var = (Var) expr;
			if(crdt.getVariables().containsKey(var)) {
				return applyAllLeft(fs,crdt.getVariables().get(var));
			}else {
				return applyAllLeft(fs,Cursor.doc());
			}
		}else if(expr instanceof Get) {
			Get get = (Get) expr; nextExpr = get.getExpr();
			f = (Cursor c) -> {
				Key key = c.getFinalKey();
				if(key instanceof HeadK) {
					return c;
				}
				c.append((Key k) -> new MapT(k) , new StringK(get.getKey()));
				return c;
			};
			
			
		}else if(expr instanceof Iter) {
			Iter iter = (Iter) expr; nextExpr = iter.getExpr();
			f = (Cursor c) -> {c.append((Key k) -> new ListT(k) , new HeadK()); return c; };
		}else {
			Next next = (Next) expr; nextExpr = next.getExpr();
			f = (Cursor c) -> crdt.getDocument().next(c).get();
		}
		fs.add(f);
		return go(crdt,nextExpr,fs);
	}
	
	private Cursor evalExpr(Document crdt,Expr expr) {
		return go(crdt,expr,new LinkedList<>());
	}
	
	private Cursor applyAllLeft(List<Function<Cursor,Cursor>> fs,Cursor cur) {
		for(Function<Cursor,Cursor> f : fs) {
			cur = f.apply(cur);
		}
		return cur;
	}

}
