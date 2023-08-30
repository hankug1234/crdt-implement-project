package com.crdt.implement.opBaseCrdt;

import com.crdt.implement.reliableBroadcast.EndPoint;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ReplicationStatus  {
	private EndPoint endpoint;
	private Object timeoutKey;
}
