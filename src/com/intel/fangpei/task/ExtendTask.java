package com.intel.fangpei.task;

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
	ReflectFactory rf = ReflectFactory.getInstance();
	try{
		if(args == null){
		eh = (ExtendHandler) rf.getMyInstance(classname);
		}else{
		eh = (ExtendHandler) rf.getMyInstance(classname, args);
		}
	}catch(Exception e){
		//ml.log("Exception: fail to load class "+e.getMessage()+" please " +
		//		"check the Path ");
		try {
			eh = (ExtendHandler) rf.getMyInstance("com.intel.developer.extend.Command",classname.replace("com.intel.developer.extend.", ""));
		} catch (Exception e1) {
			ml.error("no command named:"+classname.replace("com.intel.developer.extend.", ""));
		}
	}
	if(eh == null)System.out.println("[error]cannot get instance");
	Thread t = new Thread(eh);
	t.start();
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
