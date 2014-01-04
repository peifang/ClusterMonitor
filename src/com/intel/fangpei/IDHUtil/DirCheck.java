package com.intel.fangpei.IDHUtil;

import java.io.File;
import java.io.IOException;

import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.process.ProcessFactory;
import com.intel.fangpei.process.ProcessManager;

public class DirCheck {
public static void check(MonitorLog log){
	MonitorLog ml = log;
	int procid = ProcessFactory.buildNewProcess("sh","BasicCheck");
	ProcessManager.get(procid).setWorkDir("/usr/bin");
	File logfile = new File("/var/log/checklog");
	if(!logfile.exists())
		try {
			logfile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	ProcessManager.start(procid,logfile);
//	File drbd = new File(IDHSummary.root+IDHSummary.drbd);
//	File image = new File(IDHSummary.root+IDHSummary.hadoop_image_local);
//	File mapred = new File(IDHSummary.root+IDHSummary.mapred);
//	if(drbd.exists()&&image.exists()&&mapred.exists()){
//		ml.log("[dir check] OK");
//	}else{	
//	ml.error("[dir check] Failed,drbd or image or mapred unexist!");
//	}
}

}
