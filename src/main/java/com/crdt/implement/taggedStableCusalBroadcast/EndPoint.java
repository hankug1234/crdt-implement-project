package com.crdt.implement.taggedStableCusalBroadcast;

import com.crdt.implement.vectorClock.VectorClock;

import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
public class EndPoint {
	ActorRef<PureOpBaseProtocal> endPoint;
}
