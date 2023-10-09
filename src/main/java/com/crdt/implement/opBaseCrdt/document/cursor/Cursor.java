package com.crdt.implement.opBaseCrdt.document.cursor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.crdt.implement.opBaseCrdt.document.keyType.Dock;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;
import com.crdt.implement.opBaseCrdt.document.typetag.BranchTag;
import com.crdt.implement.opBaseCrdt.document.typetag.TagTypes;
import com.crdt.implement.opBaseCrdt.document.typetag.TypeTag;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Cursor {

	private List<BranchTag> keys;
	private TypeTag finalKey;
	
	public static Cursor doc() {
		return new Cursor(new TagTypes.MapT(new Dock()));
	}
	
	public View view() {
		if(keys.isEmpty()) {
			return new ViewTypes.Leaf(finalKey);
		}else {
			Cursor tail = new Cursor(keys.subList(1, keys.size()),finalKey); 
			return new ViewTypes.Branch(keys.get(0), tail);
		}
	}
	
	public Cursor(TypeTag finalKey) {
		this.keys = new ArrayList<>();
		this.finalKey = finalKey;
	}
	
	public void append(TypeTag newFinalKey) {
		if(this.finalKey instanceof BranchTag) {
			keys.add((BranchTag) finalKey);
			finalKey = newFinalKey;
		}else {
			throw new RuntimeException();
		}
		
	}
	
	public String toString() {
		String result = "";
		for(BranchTag tag : this.keys) {
			result += tag.getKey().toString() +" -> ";
		}
		result += this.finalKey.getKey().toString();
		return result;
	}
}
