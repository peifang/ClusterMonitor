package com.intel.fangpei.task.handler;

public abstract class ExtendHandler implements ExtendHandleable {
	protected double percent = 0.0;
	@Override
	public double taskCompletePercent() {
		return percent;
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
