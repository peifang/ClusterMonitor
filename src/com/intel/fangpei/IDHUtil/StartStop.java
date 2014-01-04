package com.intel.fangpei.IDHUtil;
import com.intel.fangpei.process.ProcessFactory;
import com.intel.fangpei.process.ProcessManager;

public class StartStop {
	public static void check(int a){
		if(a==1){
		int procid = ProcessFactory.buildNewProcess("sh","stophbase");
		ProcessManager.get(procid).setWorkDir("/usr/bin");
		ProcessManager.start(procid);
		}
		if(a==2){
			int procid = ProcessFactory.buildNewProcess("sh","stopmapreduce");
			ProcessManager.get(procid).setWorkDir("/usr/bin");
			ProcessManager.start(procid);	
		}
		if(a==3){
			int procid = ProcessFactory.buildNewProcess("sh","stophdfs");
			ProcessManager.get(procid).setWorkDir("/usr/bin");
			ProcessManager.start(procid);	
		}
		if(a==4){
			int procid = ProcessFactory.buildNewProcess("sh","stopzookeeper");
			ProcessManager.get(procid).setWorkDir("/usr/bin");
			ProcessManager.start(procid);	
		}
		if(a==5){
			int procid = ProcessFactory.buildNewProcess("sh","stopall");
			ProcessManager.get(procid).setWorkDir("/usr/bin");
			ProcessManager.start(procid);	
		}
		if(a==6){
			int procid = ProcessFactory.buildNewProcess("sh","startidh");
			ProcessManager.get(procid).setWorkDir("/usr/bin");
			ProcessManager.start(procid);	
		}
		if(a==11){
			int procid = ProcessFactory.buildNewProcess("sh","statusall");
			ProcessManager.get(procid).setWorkDir("/usr/bin");
			ProcessManager.start(procid);	
		}
	}
}
