package com.intel.fangpei.task.handler.example;

import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.task.Task;
import com.intel.fangpei.task.handler.Extender;

public class PutDataTask<type extends Object> extends Extender {
	long needs = 0;
	GeneDataTask<type> sourceTask = null;
	protected MonitorLog ml = null;
	protected DiskPutDataWorker worker=null;
	public PutDataTask(GeneDataTask<type> sourceTask,long needs) {
		try {
			this.ml=new MonitorLog();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.sourceTask = sourceTask;
		this.needs = needs;
	}
	@Override
	public void commitTask() {
		worker =new DiskPutDataWorker(ml,"c:\\");
		/*
		 * we need new feature for native 
		 * process data communication.
		 */
		//worker.setDataSource(pools, predictputs);
		worker.work();
	}

}
