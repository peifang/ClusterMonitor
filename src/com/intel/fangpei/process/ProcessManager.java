package com.intel.fangpei.process;

import java.io.File;
import java.util.HashMap;

public class ProcessManager {
private static HashMap<Integer,Proc> procRes = new HashMap<Integer,Proc>();
private static HashMap<Integer,Thread> threadRes = new HashMap<Integer,Thread>();
public synchronized void add(int procid,Proc proc){
	procRes.put(procid, proc);
}
public synchronized boolean delete(int procid){
	if(procRes.remove(procid) == null)
		return false;
	return true;
}
public synchronized Proc remove(int procid){
	return procRes.remove(procid);
}
public synchronized Proc get(int procid) {
	return procRes.get(procid);
}
public static synchronized void start(int procid){
	try{
	Thread t = new Thread(procRes.get(procid));
	t.start();
	threadRes.put(procid, t);
	System.out.println("Started!"+procid);
	}catch (Exception e){
		e.printStackTrace();
		System.out.println(procid+" is not found!");
	}
}
public static synchronized void start(int procid,File f){
		Proc process = procRes.get(procid);
		process.setOutput(f);
	Thread t = new Thread(process);
	t.start();
	threadRes.put(procid, t);
	System.out.println("Started!"+procid);
}
}