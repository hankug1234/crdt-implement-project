package com.crdt.implement.reliableBroadcast;

import java.util.List;

import com.crdt.implement.opBaseCrdt.ReplicationState;
import com.crdt.implement.vectorClock.VectorClock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class OpBaseProtocal <S,C,E>{
	private OpBaseProtocalType type;
	private Object content;
	
	public OpBaseProtocal(OpBaseProtocalType type) {
		this.type = type;
	}
	
	@Getter
	@AllArgsConstructor
	public class Connect{
		private String replicaId;
		private  EndPoint<S,C,E> endpoint;
	}
	
	@Getter
	@AllArgsConstructor
	public class Replicate{
		private long seqNr; 
		private int maxCount;
		private VectorClock filter;
		private EndPoint<S,C,E>  replayTo;
	}
	
	@Getter
	@AllArgsConstructor
	public class ReplicateTimeout{
		private String replicaId;
	}
	
	@Getter
	@AllArgsConstructor
	public class Replicated{
		private String from;
		private long toSeqNr;
		private List<E> events;
	}
	
	@Data
	public class Query{
	}
	
	@Getter
	@AllArgsConstructor
	public class Command{
		private C command;
	}
	
	@Getter
	@AllArgsConstructor
	public class Loaded{
		private ReplicationState<S> replicationState;
	}
	
	@Data
	public class Snapshot{
	}
	
	public void setConnect(String replicaId,EndPoint<S,C,E>  endPoint) throws WrongContentException{
		if(this.type != OpBaseProtocalType.Connect) throw new WrongContentException("wrong type");
		if(replicaId == null || endPoint == null) throw new WrongContentException("null parameters");
		this.content = (Object) new Connect(replicaId,endPoint);
	}
	
	public void setReplicate(long seqNr,int maxCount,VectorClock filter, EndPoint<S,C,E>  replayTo) throws WrongContentException{
		if(this.type != OpBaseProtocalType.Replicate) throw new WrongContentException("wrong type");
		if(seqNr < 0 || maxCount < 0 || filter == null || replayTo == null) throw new WrongContentException("null parameters");
		this.content = (Object) new Replicate(seqNr,maxCount,filter,replayTo);
	}
	
	public void setReplicateTimeout(String replicaId) throws WrongContentException{
		if(this.type != OpBaseProtocalType.ReplicateTimeout) throw new WrongContentException("wrong type");
		if(replicaId == null) throw new WrongContentException("null parameters");
		this.content = (Object) new ReplicateTimeout(replicaId);
	}
	
	public void setReplicated(String from,long toSeqNr,List<E> events) throws WrongContentException{
		if(this.type != OpBaseProtocalType.Replicated) throw new WrongContentException("wrong type");
		if(from == null || toSeqNr < 0) throw new WrongContentException("null parameters");
		this.content = (Object) new Replicated(from,toSeqNr,events);
	}
	
	public void setReplicateTimeout(C command) throws WrongContentException{
		if(this.type != OpBaseProtocalType.Command) throw new WrongContentException("wrong type");
		if(command == null) throw new WrongContentException("null parameters");
		this.content = (Object) new Command(command);
	}
	
	public void setLoaded(ReplicationState state) throws WrongContentException{
		if(this.type != OpBaseProtocalType.Loaded) throw new WrongContentException("wrong type");
		if(state == null) throw new WrongContentException("null parameters");
		this.content = (Object) new Loaded(state);
	}
	
	public void setQuery() throws WrongContentException{
		if(this.type != OpBaseProtocalType.Query) throw new WrongContentException("wrong type");
		this.content = (Object) new Query();
	}
	
	public void setSnapshot() throws WrongContentException{
		if(this.type != OpBaseProtocalType.Snapshot) throw new WrongContentException("wrong type");
		this.content = (Object) new Snapshot();
	}
}
