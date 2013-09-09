package com.intel.fangpei.task;

import java.lang.reflect.Constructor;

import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.task.handler.ExtendHandleable;
import com.intel.fangpei.task.handler.ExtendHandler;
import com.intel.fangpei.util.ReflectFactory;

public class ExtendTask implements Task{
MonitorLog ml = null;
ClassLoader ccl =Thread.currentThread().getContextClassLoader();
String classname = null;
String args[] = null;
public ExtendTask(MonitorLog ml,String classname){
	this.ml = ml;
	this.classname = classname;
}
public ExtendTask(MonitorLog ml,String classname,String[] args){
	this.ml = ml;
	this.classname = classname;
	this.args = args;
}
@Override
public void run() {
	ExtendHandleable eh = null;
	try{
		ReflectFactory rf = ReflectFactory.getInstance();
		if(args == null){
		eh = (ExtendHandler) rf.getMyInstance(classname);
		}else{
		eh = (ExtendHandler) rf.getMyInstance(classname, args);
		}
		Thread t = new Thread(eh);
		t.start();
	}catch(Exception e){
		ml.log("Exception: fail to load class "+e.getMessage()+" please " +
				"check the Path ");
		return;
	}
	while(true){
		ml.log("your task have completed: "+eh.taskCompletePercent());
		ml.log("[extend log] "+eh.reportStatus());
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			ml.error(e.getMessage());
			return;
		}
		if(eh.taskCompletePercent() >0.99999999){
			ml.log("[extend log] "+eh.reportStatus());
			ml.log("complete!");
			break;
		}
	}
	
}

}
