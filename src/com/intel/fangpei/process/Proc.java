package com.intel.fangpei.process;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.intel.fangpei.util.SystemUtil;

public class Proc implements Runnable{
	ProcOutPutHandler pioerror = null;
	ProcOutPutHandler pioout = null;
	ProcInPutHandler  pioinput = null;
	ProcessBuilder p = null;
	volatile Process process = null;
	Boolean kill = false;
	String processname = null;
	File redirect = null;
public Proc(String... command){
	p = new ProcessBuilder(command);
	processname = command[0];
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
		System.out.println("cann't start multi times");
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
		System.out.println("no command named:"+processname);
		kill = true;
		return;
	}
	System.out.println("Start pio");
	//write the packet head to Server;
	//pioout.writeToServer(SystemUtil.signature());
	pioerror.start();
	pioout.start();
}
public void setOutput(File f){
	redirect = f;
}
@Override
public void run() {
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
	if(kill&&process!=null){
		System.out.println("the process will exit...");
		process.destroy();
		try {
			exitVal = process.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	if(process!=null){
	try{
		exitVal = process.exitValue();
		System.out.println("the process exit with the val:"+exitVal); 
		break;
	}catch(IllegalThreadStateException e){
		
	}
}
		
}
}
public void killprocess(){
	kill =true;
}
}
