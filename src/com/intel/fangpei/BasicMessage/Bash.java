package com.intel.fangpei.BasicMessage;

import java.util.ArrayList;
import java.util.List;

public class Bash<type extends Object> {
	private List<type> bash = new ArrayList<type>();
	public List<type> toList(){
		return bash;
	}
	public void add(type row){
		bash.add(row);
	}
	public type get(){
		return bash.remove(bash.size()-1);
	}
	public type[] toArray(){
		return (type[]) bash.toArray();
	}
	public int size(){
		return bash.size();
	}
}
