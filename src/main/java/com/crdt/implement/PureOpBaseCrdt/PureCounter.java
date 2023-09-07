package com.crdt.implement.PureOpBaseCrdt;

import java.time.Duration;

import com.crdt.implement.taggedStableCusalBroadcast.PureOpBaseProtocal;
import com.crdt.implement.taggedStableCusalBroadcast.PureReplicator;

import akka.actor.typed.ActorSystem;

public class PureCounter {
	
	public PureReplicator<Long,Long,Long> replicator;
	public PureCrdtOperation<Long,Long,Long> crdt;
	public ActorSystem<PureOpBaseProtocal> actor;
	
	
	public PureCounter(String replicaId,Duration timeoutInterval) {
		this.crdt = new PureOpBaseCounterOperation();
		this.actor = ActorSystem.create(PureReplicator.create(this.crdt, replicaId, new PureReplicationState(replicaId,crdt.Default()),timeoutInterval), replicaId);
	}
	
}
