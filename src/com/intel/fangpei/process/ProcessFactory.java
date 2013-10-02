package com.intel.fangpei.process;

public class ProcessFactory {
private static ProcessManager pm = new ProcessManager();
private static Proc oneproc = null;
private static int processNum = 0;
private static void refreshID(){
	processNum++;
}
public static int buildNewProcess(String... command){
	if(command == null){
		return -1;
	}
	oneproc = new Proc(command);
	refreshID();
	pm.add(processNum, oneproc);
	return processNum;
	
}
public static Proc removeProc(int procid){
	return pm.remove(processNum);
}
public static Proc getProc(int procid){
	return pm.get(processNum);
}
public static void killProc(int procid){
	try{
	pm.get(procid).killprocess();
	}catch(Exception e){
		System.out.println("process not found!");
	}
}
}
