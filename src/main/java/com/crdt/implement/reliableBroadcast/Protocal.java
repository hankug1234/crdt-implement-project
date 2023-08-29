package com.crdt.implement.reliableBroadcast;

import java.util.List;

import com.crdt.implement.opBaseCrdt.ReplicationState;
import com.crdt.implement.vectorClock.VectorClock;

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
	public static class Replicate implements OpBaseProtocal{
		private final long seqNr; 
		private final int maxCount;
		private final VectorClock filter;
		private final EndPoint replayTo;
	}
	
	@Getter
	@AllArgsConstructor
	public class ReplicateTimeout implements OpBaseProtocal{
		private final String replicaId;
	}
	
	@Getter
	@AllArgsConstructor
	public class Replicated<E> implements OpBaseProtocal{
		private final String from;
		private final long toSeqNr;
		private final List<E> events;
	}
	
	@Data
	public class Query implements OpBaseProtocal{
	}
	
	@Getter
	@AllArgsConstructor
	public class Command<C> implements OpBaseProtocal{
		private final C command;
	}
	
	@Getter
	@AllArgsConstructor
	public class Loaded<S> implements OpBaseProtocal{ 
		private final ReplicationState<S> replicationState;
	}
	
	@Data
	public class Snapshot implements OpBaseProtocal{
	}
}
