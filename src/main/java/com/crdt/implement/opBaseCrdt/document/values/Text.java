package com.crdt.implement.opBaseCrdt.document.values;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.crdt.implement.opBaseCrdt.BwRGA.Block;
import com.crdt.implement.opBaseCrdt.BwRGA.BwRgaCommand;
import com.crdt.implement.opBaseCrdt.BwRGA.BwRgaData;
import com.crdt.implement.opBaseCrdt.BwRGA.BwRgaState;
import com.crdt.implement.opBaseCrdt.BwRGA.Command;
import com.crdt.implement.opBaseCrdt.BwRGA.Command.Insert;
import com.crdt.implement.opBaseCrdt.BwRGA.Command.RemoveAt;
import com.crdt.implement.opBaseCrdt.BwRGA.OpBaseBwRgaOperation;
import com.crdt.implement.opBaseCrdt.document.node.BranchNode;
import com.crdt.implement.opBaseCrdt.document.signal.Signal;
import com.crdt.implement.opBaseCrdt.document.signal.SignalTypes;
import com.crdt.implement.reliableBroadcast.OpBaseEvent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Getter
public class Text extends ObjectVal{
	
	private BwRgaState<String> value;
	private OpBaseBwRgaOperation<String> execute;
	
	public Text(String replicaId) {
		log.info(replicaId+" : text created");
		this.execute = new OpBaseBwRgaOperation<>(replicaId);
		this.value = execute.Default();
	}
	
	public static Insert<String> insert(int index,String value){
		List<String> list = List.of(value.split(""));
		return new Command.Insert<>(index, list);
	}
	
	public static RemoveAt<String> remove(int index, int count){
		return new Command.RemoveAt<>(index,count);
	}
	
	public Val clone() {
		return new Text(execute.copy(this.value),execute);
	}
	
	public List<Block<String>> query(){
		return execute.Query(value);
	}
	
	@Override
	public Optional<Object> prepare(Object cmd) {
		if(cmd instanceof BwRgaCommand) {
			BwRgaCommand<String> bwCmd = (BwRgaCommand<String>) cmd;
			BwRgaData<String> data = this.execute.Prepare(value,bwCmd);
			return Optional.of(data);
		}
		
		return Optional.empty();
	}
	
	@Override
	public void effect(Object data){
		if(data instanceof BwRgaData) {
			BwRgaData<String> bwData = (BwRgaData<String>) data;
			OpBaseEvent<BwRgaData<String>> bwOp = new OpBaseEvent<>(bwData);
			this.value = this.execute.Effect(value, bwOp);
		}
	}
	
	@Override
	public String toString() {
		return execute.Query(value).stream().flatMap(b->b.getContent().getContent().stream()).collect(Collectors.joining());
	}

}
