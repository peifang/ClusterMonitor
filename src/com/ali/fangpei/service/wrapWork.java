package com.ali.fangpei.service;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.ServiceMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.task.TaskRunner.TaskEnv;

public class wrapWork {
	private TaskEnv env = null;
	private byte command = 0;
	String taskname = null;
	String[] args = null;
	public wrapWork(String taskname,boolean isService){
		this.taskname = taskname;
		if(isService){
			command  =  ServiceMessage.SERVICE;
		}else{
			command  =  ServiceMessage.THREAD;
		}
		
	}
	public void setEnv(TaskEnv env){
		this.env = env;
	}
	public void setArgs(String... args){
		this.args = args;
	}
	/*
	 * one task name and args
	 */
public packet toTransfer() {
	StringBuilder sb = new StringBuilder();
	sb.append(taskname);
	sb.append(" ");
	if(args!=null){
	for(int i = 0;i < args.length;i++){
	sb.append(args[i]);
	sb.append(" ");
	}
	}
	return new packet(BasicMessage.NODE,command,sb.toString().getBytes());
}
}
