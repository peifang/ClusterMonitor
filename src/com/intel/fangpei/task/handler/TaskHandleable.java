package com.intel.fangpei.task.handler;

public interface TaskHandleable extends Runnable{
	public void commitTask();
	public double taskCompletePercent();
}
