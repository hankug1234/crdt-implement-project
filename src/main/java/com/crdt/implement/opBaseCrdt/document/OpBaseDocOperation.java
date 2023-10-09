package com.crdt.implement.opBaseCrdt.document;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
import com.crdt.implement.opBaseCrdt.document.node.MapNode;
import com.crdt.implement.opBaseCrdt.document.node.Node;
import com.crdt.implement.opBaseCrdt.document.node.ordering.Block;
import com.crdt.implement.opBaseCrdt.document.node.ordering.BlockMetaData;
import com.crdt.implement.opBaseCrdt.document.signal.Signal;
import com.crdt.implement.opBaseCrdt.document.signal.SignalTypes;
import com.crdt.implement.reliableBroadcast.OpBaseEvent;
import com.crdt.implement.vectorClock.VectorClock;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpBaseDocOperation implements OpBaseCrdtOperation<Document,JSONObject,List<Command>,List<Operation>>{

	private String replicaId;
	
	public OpBaseDocOperation(String replicaId) {
		this.replicaId = replicaId;
	}
	
	
	@Override
	public Document Default() {
		return new Document(new MapNode());
	}

	@Override
	public JSONObject Query(Document crdt) {
		JSONObject result = new JSONObject();
		if(crdt.getVar() != null) {
			
			Optional<Node> node = crdt.getDocument().query(crdt.getVar());
			if(node.isPresent()) {
				result.put("answer", node.get().toJson());
				return result;
			}
		}
		result.put("answer", false);
		return result;
	}

	@Override
	public Document copy(Document crdt) {
		return crdt.clone();
	}

	@Override
	public List<Operation> Prepare(Document crdt, List<Command> command) {
		return command.stream().map(
				(Command cmd) -> {
					Operation op = null;
					if(cmd instanceof CommandTypes.Assign) {
						
						CommandTypes.Assign assign = (CommandTypes.Assign) cmd;
						op = makeOp(evalExpr(crdt,assign.getExpr()),new SignalTypes.AssignS(assign.getValue()));
						
					}else if(cmd instanceof CommandTypes.Delete) {
						
						CommandTypes.Delete delete = (CommandTypes.Delete) cmd;
						op = makeOp(evalExpr(crdt,delete.getExpr()),new SignalTypes.DeleteS());
						
					}else if(cmd instanceof CommandTypes.Insert) {
						
						CommandTypes.Insert insert = (CommandTypes.Insert) cmd;
						op = makeOp(evalExpr(crdt,insert.getExpr()),insertToInsertS(crdt,insert));
						
					}else if(cmd instanceof CommandTypes.Move) {
						
						CommandTypes.Move move = (CommandTypes.Move) cmd;
						op = makeOp(evalExpr(crdt,move.getSrc()),moveToMoveS(crdt,move));
						
					}else if(cmd instanceof CommandTypes.Let) {
						CommandTypes.Let let = (CommandTypes.Let) cmd;
						crdt.getVariables().put(let.getX(),evalExpr(crdt,let.getExpr()));
						return Optional.empty();
					}else if(cmd instanceof CommandTypes.Var) {
						CommandTypes.Var var = (CommandTypes.Var) cmd;
						Cursor cur = evalExpr(crdt,var.getVar());
						crdt.setVar(cur);
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
			if((op.getSignal() instanceof SignalTypes.InsertS)) {
				SignalTypes.InsertS insert = (SignalTypes.InsertS) op.getSignal();
				if(insert.getMeta() == null) {
					setMetaData(crdt,op.getCur(),insert);
				}
			}
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
				c.append(get.getTag());
				return c;
			};
			fs.add(f);
			return go(crdt,nextExpr,fs);
		}else if(expr instanceof Index) {
			Index index = (Index) expr; Expr nextExpr = index.getExpr();
			Function<Cursor,Cursor> f = (Cursor c) -> {
				c.append(index.getTag());
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
	
	public SignalTypes.InsertS insertToInsertS(Document crdt,CommandTypes.Insert insert){
		UUID uuid = UUID.randomUUID();
		IndexK indexK = new IndexK(uuid.toString(),this.replicaId);
		
		return new SignalTypes.InsertS(insert.getValue(), null,insert.getIndex(), indexK);
	}
	
	public void setMetaData(Document crdt,Cursor cur,SignalTypes.InsertS insertS) {
		Node node = crdt.getDocument();
		Optional<BlockMetaData> meta = node.getInsertMetaData(cur, insertS.getIndex());
		if(!meta.isPresent()) {
			throw new RuntimeException();
		}
		insertS.setMeta(meta.get());
	}
	
	public SignalTypes.MoveS moveToMoveS(Document crdt, CommandTypes.Move move){
		Node node = crdt.getDocument();
		Cursor location = evalExpr(crdt,move.getSrc());
		Optional<Block> src = node.getOrderBlock(location, move.getFrom());
		Optional<Block> dst = node.getOrderBlock(location, move.getTo());
		
		if(!src.isPresent() || !dst.isPresent()) {
			throw new RuntimeException();
		}
		
		return new SignalTypes.MoveS(src.get().getId(), dst.get().getId(), move.getLocation());
	}

}
