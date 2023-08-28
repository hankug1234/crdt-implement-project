package com.crdt.implement.reliableBroadcast;

import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
public class EndPoint<S,C,E> {
	private ActorRef<OpBaseProtocal<S,C,E>> endpoint;
}
