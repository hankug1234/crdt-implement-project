package com.crdt.implement.reliableBroadcast;

import java.util.List;

import com.crdt.implement.opBaseCrdt.ReplicationState;
import com.crdt.implement.vectorClock.VectorClock;

import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class Protocal{
	
	@Getter
	@AllArgsConstructor
	public static class Connect implements OpBaseProtocal{
		private final String replicaId;
		private  final EndPoint endpoint;
	}
	
	@Getter
	@AllArgsConstructor
	public static class Disconnect implements OpBaseProtocal{
		private final String replicaId;
	}
	
	@Getter
	@AllArgsConstructor
	public static class Replicate implements OpBaseProtocal{
		private final long seqNr; 
		private final int maxCount;
		private final VectorClock filter;
		private final EndPoint replayTo;
	}
	
	@Getter
	@AllArgsConstructor
	public static class ReplicateTimeout implements OpBaseProtocal{
		private final String replicaId;
	}
	
	@Getter
	@AllArgsConstructor
	public static class Replicated<E> implements OpBaseProtocal{
		private final String from;
		private final long toSeqNr;
		private final List<OpBaseEvent<E>> events;
	}
	
	@Getter
	@AllArgsConstructor
	public static class Query implements OpBaseProtocal{
		private final ActorRef<OpBaseResponse> replyTo;
	}
	
	@Getter
	@AllArgsConstructor
	public static class Command<C> implements OpBaseProtocal{
		private final C command;
		private final ActorRef<OpBaseResponse> replyTo;
	}
	
	@Getter
	@AllArgsConstructor
	public static class Loaded<S> implements OpBaseProtocal{ 
		private final ReplicationState<S> replicationState;
	}
	
	@Getter
	@AllArgsConstructor
	public static class Snapshot implements OpBaseProtocal{
	}
	
	@Getter
	@AllArgsConstructor
	public static class Res<Q> implements OpBaseResponse{
		private final Q res;
	}
	
	@Getter
	@AllArgsConstructor
	public static class Stop implements OpBaseProtocal{
		
	}
}
