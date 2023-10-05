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
import com.crdt.implement.opBaseCrdt.document.expression.ExprTypes.Index;
import com.crdt.implement.opBaseCrdt.document.expression.ExprTypes.Var;
import com.crdt.implement.opBaseCrdt.document.keyType.IndexK;
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
						op = makeOp(evalExpr(crdt,insert.getExpr()),new SignalTypes.InsertS(insert.getValue(),insert.getIndex()));
						
					}else if(cmd instanceof CommandTypes.Move) {
						
						CommandTypes.Move move = (CommandTypes.Move) cmd;
						op = makeOp(evalExpr(crdt,move.getSrc()),new SignalTypes.MoveS(evalExpr(crdt,move.getTar())));
						
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
			node.applyOp(op);
		}
		
		return crdt;
	}
	
	public Operation makeOp(Cursor cur, Signal signal) {
		return new Operation(null,cur,signal);
	}
	
	private Cursor go(Document crdt, Expr expr, List<Function<Cursor,Cursor>> fs) {
		
		if(expr instanceof Doc) {
			Doc doc = (Doc) expr;
			go(crdt,doc.getExpr(),fs);
			return applyAllLeft(fs,Cursor.doc());
		}else if (expr instanceof Var) {
			Var var = (Var) expr;
			
			go(crdt,var.getExpr(),fs);
			
			if(crdt.getVariables().containsKey(var)) {
				return applyAllLeft(fs,crdt.getVariables().get(var));
			}else {
				return applyAllLeft(fs,Cursor.doc());
			}
			
		}else if(expr instanceof Get) {
			Get get = (Get) expr; Expr nextExpr = get.getExpr();
			Function<Cursor,Cursor> f = (Cursor c) -> {
				c.append((Key k) -> new MapT(k) , new StringK(get.getKey()));
				return c;
			};
			fs.add(f);
			return go(crdt,nextExpr,fs);
		}else if(expr instanceof Index) {
			Index index = (Index) expr; Expr nextExpr = index.getExpr();
			Function<Cursor,Cursor> f = (Cursor c) -> {
				c.append((Key k) -> new ListT(k) , new IndexK(index.getIndex(),index.getReplicaId()));
				return c;
			};
			fs.add(f);
			return go(crdt,nextExpr,fs);
		}else {
			return null;
		}
		
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
