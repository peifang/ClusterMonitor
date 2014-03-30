package com.intel.fangpei.process;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
/**
 * manager proc
 * @author Administrator
 *
 */
public class ProcessManager {
	/**
	 * this serviceBoss is used for service Demo and 
	 * other Thread work env.
	 */
private static Proc serviceWorker = null;
private static HashMap<Integer,Proc> procRes = new HashMap<Integer,Proc>();
private static ArrayList<Integer> alreadyStarted = new ArrayList<Integer>();
private static ArrayList<Integer> alreadyFinished = new ArrayList<Integer>();
private static ArrayList<Integer> alreadyKilled = new ArrayList<Integer>();
public static void startServiceProc(){
	if(serviceWorker == null){
		serviceWorker = new Proc("ServiceDemo");
		serviceWorker.startAndWait();
	}
}
public static synchronized void add(int procid,Proc proc){
	procRes.put(procid, proc);
}
public static synchronized boolean delete(int procid){
	if(procRes.remove(procid) == null)
		return false;
	return true;
}
public synchronized Proc remove(int procid){
	return procRes.remove(procid);
}
public static Proc get(int procid) {
	return procRes.get(procid);
}
public static void kill(int procid){
	try{
	get(procid).killprocess();
	}catch(Exception e){
		System.out.println("process not found!");
	}
}
public static void start(int procid){
	Proc p = procRes.get(procid);
	synchronized(p){
	if(alreadyStarted.contains(procid)||
	   alreadyFinished.contains(procid)||
	   alreadyKilled.contains(procid)){
		p.notify();
		//already started!
		return;
	}
	try{
	if(p!=null){
	alreadyStarted.add(procid);
	System.out.println("Started!"+procid);
	if(p.startAndWait() < 0){
		alreadyKilled.add(procid);
	}else{
	alreadyFinished.add(procid);
	System.out.println("Stoped!"+procid);
	}
	}
	}catch (Exception e){
		e.printStackTrace();
		System.out.println(procid+" is not found!");
	}
	p.notify();
	}
}
public static void start(int procid,File f){
	Proc process = procRes.get(procid);
	process.setOutput(f);
	start(procid);

}
}