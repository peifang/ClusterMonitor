package com.intel.fangpei.logfactory;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.intel.fangpei.util.ConfManager;

public class MonitorLog extends ClusterMonitorLog {
	FileWriter fw = null;
	boolean newfile = false;
	SimpleDateFormat DateFormat = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss:SSS");
	Date date = new Date();

	public MonitorLog() throws Exception {
		StackTraceElement stack[] = (new Throwable()).getStackTrace();
		this.className = stack[1].getClassName();
		File f = new File(ConfManager.getConf("log.path")+"monitorlog");
		if (!f.exists()) {
			f.createNewFile();
			newfile = true;
		}
		fw = new FileWriter(f, true);
		if (newfile) {
			fw.write(headString() + "\n");
		}
		/*
		 * we can get all the stack trace of the caller;
		 */
		/*
		 * for (;i < stack.length; i++){ StackTraceElement ste=stack[i];
		 * System.out
		 * .println(ste.getClassName()+"."+ste.getMethodName()+"(...);");;
		 * System.out.println(i+"--"+ste.getMethodName());
		 * System.out.println(i+"--"+ste.getFileName());
		 * System.out.println(i+"--"+ste.getLineNumber()); }
		 */
	}

	public MonitorLog(String filePath) throws Exception {
		StackTraceElement stack[] = (new Throwable()).getStackTrace();
		this.className = stack[1].getClassName();
		File f = new File(filePath);
		if (!f.exists()) {
			f.createNewFile();
			newfile = true;
		}
		fw = new FileWriter(f, true);
		if (newfile) {
			fw.write(headString() + "\n");
		}
	}
public static void loglog(String message){
	System.out.println("loglog: "+message);
}
	@Override
	public void log(String message) {
		synchronized(fw){
		try {
			fw.write(headString() + "[message]" + message + "\n");
			fw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fw.notify();
		}

	}
	public void logWithThreadName(String message) {
		synchronized(fw){
		try {
			fw.write(headString() + "[message][" +Thread.currentThread().getName() +"]"+message + "\n");
			fw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fw.notify();
		}

	}
	public void warn(String message) {

		try {
			fw.write(headString() + "[warn]" + message + "\n");
			fw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void error(String message) {

		try {
			fw.write(headString() + "[error]" + message + "\n");
			fw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String headString() {
		date.setTime(System.currentTimeMillis());
		return "[" + DateFormat.format(date) + "]" + "[" + className + "]";
	}

	public void flush() {
		try {
			fw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
