package com.intel.developer.extend;
import com.intel.fangpei.IDHUtil.PreCheck;
import com.intel.fangpei.IDHUtil.StartStop;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.task.handler.Extender;
/**
 *you can use this to support IDH operations;
 */
public class idh extends Extender{
	MonitorLog ml = null;
	String version = null;
	String command1 = null;
	String command2 = null;
	public idh(String s1,String s2){
		this.command1 = s1;
		this.command2 = s2;
	}
	public idh(String s1){
		this.command1 = s1;
	}
	public void commitTask(){
	if(command1.equalsIgnoreCase("stop")){
		if(command2.equalsIgnoreCase("hbase")){
		StartStop.check(1);	
		}
		if(command2.equalsIgnoreCase("mapreduce")){
			StartStop.check(2);	
		}
		if(command2.equalsIgnoreCase("hdfs")){
			StartStop.check(3);	
		}
		if(command2.equalsIgnoreCase("zookeeper")){
			StartStop.check(4);	
		}
		if(command2.equalsIgnoreCase("all")){
			StartStop.check(5);	
		}
	}else if(command1.equalsIgnoreCase("start")){
		if(command2.equalsIgnoreCase("all")){
			StartStop.check(6);	
		}
	}else if(command1.equalsIgnoreCase("status")){
		StartStop.check(11);
	}else if(command1.equalsIgnoreCase("netspeed")){
		PreCheck.check();
	}
	}

}
