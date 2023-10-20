package com.crdt.implement.opBaseCrdt.document.signal;

import com.crdt.implement.opBaseCrdt.BwRGA.BwRgaData;
import com.crdt.implement.opBaseCrdt.document.keyType.IndexK;
import com.crdt.implement.opBaseCrdt.document.node.ordering.BlockMetaData;
import com.crdt.implement.opBaseCrdt.document.node.ordering.OrderId;
import com.crdt.implement.opBaseCrdt.document.values.Val;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class SignalTypes {
	
	@AllArgsConstructor
	@Getter
	public static class AssignS implements Signal{
		private Val value;
		
		public void setValue(Val value) {
			this.value = value;
		}
	}
	
	@AllArgsConstructor
	@Getter
	public static class InsertS implements Signal{
		private Val value;
		private BlockMetaData meta;
		private int index;
		private IndexK key;
		
		
		public void setMeta(BlockMetaData meta) {
			this.meta = meta;
		}
		
		public void setValue(Val value) {
			this.value = value;
		}
	}
	
	public static class DeleteS implements Signal{
		
	}
	
	@AllArgsConstructor
	@Getter
	public static class EditS implements Signal{
		Object data;
		
		public void setData(Object data) {
			this.data = data;
		}
	}
	
	@AllArgsConstructor
	@Getter
	public static class MoveS implements Signal{
		private String replicaId;
		private long seqNr;
		private OrderId from;
		private OrderId to;
		private int priority;
		private int location;
	}
}
