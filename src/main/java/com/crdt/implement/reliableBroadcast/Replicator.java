package com.crdt.implement.reliableBroadcast;

import com.crdt.implement.opBaseCrdt.OpBaseCrdtOperation;
import com.crdt.implement.persistence.OpBaseCrdtDB;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.StashBuffer;


public class Replicator<Crdt,S,C,E> extends AbstractBehavior{
	
	private final StashBuffer<OpBaseProtocal> buffer;
	private OpBaseCrdtDB db;
	private OpBaseCrdtOperation<Crdt,S,C,E> crdt;
	
	public Replicator(ActorContext context,StashBuffer<OpBaseProtocal> buffer,OpBaseCrdtDB db
			,OpBaseCrdtOperation<Crdt,S,C,E> crdt) {
		super(context);
		this.buffer = buffer;
		this.db = db;
		this.crdt = crdt;
	}
	
	public static <Crdt,S,C,E>  Behavior<OpBaseProtocal> create(OpBaseCrdtDB db,OpBaseCrdtOperation<Crdt,S,C,E> crdt){
		return Behaviors.withStash(100, stash->{
			return Behaviors.setup(ctx->{
				return new Replicator<Crdt,S,C,E>(ctx,stash,db,crdt);
			});
		});
	}
	
	private Behavior<OpBaseProtocal> onInit(Object protocal){
		if(protocal instanceof Protocal.Loaded) {
			return buffer.unstashAll(active((OpBaseProtocal)protocal));
		}else {
			buffer.stash((OpBaseProtocal)protocal);
			return Behaviors.same();
		}
	}
	
	private Behavior<OpBaseProtocal> onQuery(Protocal.Query query){
		return null;
	}
	
	private Behavior<OpBaseProtocal> onReplicate(Protocal.Replicate replicate){
		return null;
	}
	
	private Behavior<OpBaseProtocal> onReplicated(Protocal.Replicated<E> replicated){
		return null;
	}
	
	private Behavior<OpBaseProtocal> onReplicateTimeout(Protocal.ReplicateTimeout timeout){
		return null;
	}
	
	private Behavior<OpBaseProtocal> onCommand(Protocal.Command<C> command){
		return null;
	}
	
	private Behavior<OpBaseProtocal> onConnect(Protocal.Connect connect){
		return null;
	}
	
	private Behavior<OpBaseProtocal> onSnapshot(Protocal.Snapshot snapshot){
		return null;
	}
	
	private Behavior<OpBaseProtocal> active(OpBaseProtocal protocal){
		return Behaviors.receive(OpBaseProtocal.class)
				.onMessage(Protocal.Query.class, this::onQuery)
				.onMessage(Protocal.Replicate.class, this::onReplicate)
				.onMessage(Protocal.Replicated.class, this::onReplicated)
				.onMessage(Protocal.ReplicateTimeout.class, this::onReplicateTimeout)
				.onMessage(Protocal.Command.class, this::onCommand)
				.onMessage(Protocal.Connect.class, this::onConnect)
				.onMessage(Protocal.Snapshot.class, this::onSnapshot)
				.build();
	}

	@Override
	public Receive<OpBaseProtocal> createReceive() {
		return newReceiveBuilder().onMessage(OpBaseProtocal.class,this::onInit).build();
	}
	

}
