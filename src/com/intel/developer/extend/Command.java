package com.intel.developer.extend;

import com.intel.fangpei.process.ProcessFactory;
import com.intel.fangpei.process.ProcessManager;
import com.intel.fangpei.task.handler.Extender;

public class Command  extends Extender{
	String paras;
public Command(String paras){
	this.paras = paras;
}
public void commitTask(){
	int procid = ProcessFactory.buildNewProcess(paras);
	System.out.println("command process start!");
	ProcessManager.start(procid);
	System.out.println("command process end!");
	}
}
