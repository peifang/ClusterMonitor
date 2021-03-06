package com.intel.fangpei.task.handler;

import com.intel.fangpei.task.DataSourcePool;
import com.intel.fangpei.terminal.Node;

public abstract class ExtendW<type extends Object> implements ExtendHandleable {
	Node node = null;
	DataSourcePool<type> dataPool = null;
	protected double percent = 0.0;
	private String writeDir = null;
	public ExtendW(){
	}
	public ExtendW(String writeDir){
		this.writeDir =writeDir;
	}
	@Override
	public double taskCompletePercent() {
		return percent;
	}
	public void setFather(Node node){
		this.node = node;
	}
	@Override
	public String reportStatus() {
		// TODO Auto-generated method stub
		return "[extend ExtendHandler] auto";
	}

	@Override
	public void run() {
		commitTask();
		percent = 1.0;
	}

	@Override
	public abstract void commitTask();

}
