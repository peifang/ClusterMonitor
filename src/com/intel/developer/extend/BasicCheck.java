package com.intel.developer.extend;

import com.intel.fangpei.IDHUtil.DirCheck;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.process.ProcessFactory;
import com.intel.fangpei.task.handler.Extender;

public class BasicCheck extends Extender{
	MonitorLog ml = null;
	String version = null;
	public BasicCheck(){
		try {
			ml = new MonitorLog("/var/log/BasicCheck");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public BasicCheck(String s){
		version = s;
		try {
			ml = new MonitorLog("/var/log/BasicCheck");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void commitTask(){
	DirCheck.check(ml);
	}

}
