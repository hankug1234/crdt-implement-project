package com.crdt.implement.opBaseCrdt.BwRGA;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Block<A> {
	private BwRgaVPtrOff vptrOff;
	private Content<A> content;
	
	public int length() {
		return this.content.length();
	}
	
	public void setTombstone() {
		this.content.setTombstone();
	}
	
	public boolean isTombstone() {
		return this.content.isTombstone();
	}
	
	public Block<A> clone(){
		return new Block<>(vptrOff,content.clone());
	}
	
	public boolean containOffset(int offset) {
		return (this.vptrOff.getOffset() + this.content.length()) >= offset && offset >= this.vptrOff.getOffset();
	}
	
	public Block<A>[] split(int position){
		
		if(position >= this.length()) {
			return new Block[] {this,null};
		}
		
		Content<A>[] contents = this.content.slice(position);
		BwRgaVPtrOff right = new BwRgaVPtrOff(this.vptrOff.getVptr(),this.vptrOff.getOffset()+position);
		return new Block[] {new Block<A>(vptrOff,contents[0]), new Block<A>(right,contents[1])};
	}
}
