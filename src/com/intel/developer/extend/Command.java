package com.intel.developer.extend;

import com.intel.fangpei.process.ProcessFactory;
import com.intel.fangpei.process.ProcessManager;
import com.intel.fangpei.task.handler.ExtendHandler;

public class Command  extends ExtendHandler{
	String paras;
public Command(String paras){
	this.paras = paras;
}
public void commitTask(){
	int procid = ProcessFactory.buildNewProcess(paras);
	ProcessManager.start(procid);
	}
}
