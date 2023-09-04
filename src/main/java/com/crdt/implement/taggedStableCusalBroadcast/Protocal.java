package com.crdt.implement.taggedStableCusalBroadcast;

import java.util.Set;

import com.crdt.implement.PureOpBaseCrdt.PureReplicationState;
import com.crdt.implement.vectorClock.VectorClock;

import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Protocal {
	
	@Getter
	@AllArgsConstructor
	public static class Query implements PureOpBaseProtocal{
		private ActorRef<PureOpBaseResponse> replyTo;
	}
	
	@Getter
	@AllArgsConstructor
	public static class Connect implements PureOpBaseProtocal{
		private String replicaId;
		private EndPoint endPoint;
	}
	
	@Getter
	@AllArgsConstructor
	public static class Replicate implements PureOpBaseProtocal{
		private String repliaId;
		private VectorClock vectorClock;
	}
	
	@Getter
	@AllArgsConstructor
	public static class Reset<S,O> implements PureOpBaseProtocal{
		private String from;
		private PureReplicationState<S,O> snapshot;
	}
	
	@Getter
	@AllArgsConstructor
	public static class Replicated<O> implements PureOpBaseProtocal{
		private String from;
		private Set<PureOpBaseEvent<O>> operations;
	}
	
	@Getter
	@AllArgsConstructor
	public static class ReplicateTimeout implements PureOpBaseProtocal{
		private String replicaId;
	}
	
	@Getter
	@AllArgsConstructor
	public static class Submit<O> implements PureOpBaseProtocal{
		private O operation;
		private ActorRef<PureOpBaseResponse> replyTo;
	}
	
	@Getter
	@AllArgsConstructor
	public static class Evict implements PureOpBaseProtocal{
		private String replicaId;
	}
	
	@Getter
	@AllArgsConstructor
	public static class Evicted implements PureOpBaseProtocal{
		private String replicaId;
		private VectorClock vectorClock;
	}
	
	@Getter
	@AllArgsConstructor
	public static class Res<Q> implements PureOpBaseResponse{
		private Q res;
	}
	
	@Getter
	@AllArgsConstructor
	public static class Stop implements PureOpBaseProtocal{
		
	}

}
