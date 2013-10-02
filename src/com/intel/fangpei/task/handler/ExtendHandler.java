package com.intel.fangpei.task.handler;

import com.intel.fangpei.terminal.Node;

public abstract class ExtendHandler implements ExtendHandleable {
	Node node = null;
	protected double percent = 0.0;
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
	public void commitTask() {
		// TODO Auto-generated method stub
		
	}

}
