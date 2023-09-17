package com.crdt.implement.opBaseCrdt.BwRGA;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Content<A> {
	private int size;
	private Optional<List<A>> content;
	
	public Content(List<A> content) {
		this.content = Optional.of(content);
		this.size = content.size();
	}
	
	public Content(int size) {
		this.content = Optional.empty();
		this.size = size;
	}
	
	public Content<A> clone(){
		return new Content<A>(size,content);
	}
	
	public List<A> getContent(){
		return this.content.get();
	}
	
	public int length() {
		return this.size;
	}
	
	public Content<A>[] slice(int offset) {
		
		if(this.isTombstone()) {
			return new Content[] {new Content<A>(offset),new Content<A>(this.size - offset)};
		}else {
			return new Content[] {new Content<A>(this.content.get().subList(0, offset)), new Content<A>(this.content.get().subList(offset,this.size))};
		}
	}
	
	public boolean isTombstone() {
		return !content.isPresent();
	}
	
	public void setTombstone() {
		this.content = Optional.empty();
	}
	
	
}
