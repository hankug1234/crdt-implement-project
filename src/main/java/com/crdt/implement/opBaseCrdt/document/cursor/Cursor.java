package com.crdt.implement.opBaseCrdt.document.cursor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.crdt.implement.opBaseCrdt.document.keyType.Dock;
import com.crdt.implement.opBaseCrdt.document.keyType.Key;
import com.crdt.implement.opBaseCrdt.document.typetag.BranchTag;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Cursor {

	private List<BranchTag> keys;
	private Key finalKey;
	
	public static Cursor doc() {
		return new Cursor(new Dock());
	}
	
	public View view() {
		if(keys.isEmpty()) {
			return new ViewTypes.Leaf(finalKey);
		}else {
			Cursor tail = new Cursor(keys.subList(1, keys.size()),finalKey); 
			return new ViewTypes.Branch(keys.get(0), tail);
		}
	}
	
	public Cursor(Key finalKey) {
		this.keys = new ArrayList<>();
		this.finalKey = finalKey;
	}
	
	public void append(Function<Key,BranchTag> tag, Key newFinalKey) {
		keys.add(tag.apply(this.finalKey));
		finalKey = newFinalKey;
	}
}
