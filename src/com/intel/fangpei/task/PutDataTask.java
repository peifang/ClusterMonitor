package com.intel.fangpei.task;

import com.intel.fangpei.logfactory.MonitorLog;

public abstract class PutDataTask<type extends Object> implements Task {
	long needs = 0;
	GeneDataTask<type> sourceTask = null;
	protected MonitorLog ml = null;
	public PutDataTask(GeneDataTask<type> sourceTask,long needs) {
		this.sourceTask = sourceTask;
		this.needs = needs;
	}

}
