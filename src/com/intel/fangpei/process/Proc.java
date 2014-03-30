package com.intel.fangpei.process;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.util.SystemUtil;
/**
 * a Proccess instance.
 */
public class Proc{
	ProcOutPutHandler pioerror = null;
	ProcOutPutHandler pioout = null;
	ProcInPutHandler  pioinput = null;
	ProcessBuilder p = null;
	volatile Process process = null;
	Boolean kill = false;
	String processname = null;
	File redirect = null;
	private static MonitorLog ml = null; 
public Proc(String... command){
	if(ml == null){
		getLogInstance();
	}
	p = new ProcessBuilder(command);
	processname = command[0];
}
private synchronized void getLogInstance() {
	if(ml == null){
		try {
			ml = new MonitorLog("/Proc.log");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
public void setWorkDir(String path){
	if(path == null){
		return;
	}
	//change the JVM started work dir
	p.directory(new File(path));
	
}
public synchronized void addClassPath(String classpath){
	if(classpath == null){
		return;
	}
	Map<String,String> m = p.environment();
	String oldpath = m.get("CLASS_PATH");
	m.put("CLASS_PATH", oldpath+";"+classpath);
}
public String getClassPath(){
	Map<String,String> m = p.environment();
	return m.get("CLASS_PATH");
}
public String getProcessName(){
	return processname;
}
public Map<String,String> getAllEnv(){
	return p.environment();
}
private void StartProcess(){
	if(process != null){
		ml.error("cann't start multi times");
		return;
	}
	try {
		process = p.start();
		if(redirect == null){
		pioerror = new ProcOutPutHandler(process.getErrorStream(), "ERR");
		pioout = new ProcOutPutHandler(process.getInputStream(), "OUT");
		}else{
		pioerror = new ProcOutPutHandler(process.getErrorStream(), "ERR",new FileOutputStream(redirect));
		pioout = new ProcOutPutHandler(process.getInputStream(), "OUT",new FileOutputStream(redirect));
		}
	} catch (IOException e) {
		if(process == null){
			ml.error("process is null because the proc cann't be started");
		}
		ml.error("no command named:"+processname);
		kill = true;
		return;
	}
	ml.logWithThreadName("Start pio");
	//write the packet head to Server;
	//pioout.writeToServer(SystemUtil.signature());
	pioerror.start();
	pioout.start();
}
public void setOutput(File f){
	redirect = f;
}
public int startAndWait() {
	StartProcess();
while(true){
	int exitVal = 0;
	try {
		Thread.sleep(1000);
	} catch (InterruptedException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
//	System.out.println("----------------------");
//	System.out.println("total:"+Runtime.getRuntime().totalMemory()/1024.0/1024.0+"M");
//	System.out.println("free:"+Runtime.getRuntime().freeMemory()/1024.0/1024.0+"M");
//	System.out.println("Max:"+Runtime.getRuntime().maxMemory()/1024.0/1024.0+"M");
//	System.out.println("----------------------");
	if(kill){
		ml.logWithThreadName("the process will be killed...");
		process.destroy();
		try {
			exitVal = process.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	if(process!=null){
	try{
		exitVal = process.exitValue();
		ml.logWithThreadName("the process exit with the val:"+exitVal);
		return exitVal;
	}catch(IllegalThreadStateException e){
		
	}
}	
}
}
public void killprocess(){
	kill =true;
}
public void setAllEnv(Map<String, String> env) {
	p.environment().putAll(env);
	
}
}
