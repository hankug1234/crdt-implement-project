package com.crdt.implement.PureOpBaseCrdt;

import com.crdt.implement.taggedStableCusalBroadcast.EndPoint;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PureReplicationStatus {
	private EndPoint endPoint;
	private Object timeout;
}
