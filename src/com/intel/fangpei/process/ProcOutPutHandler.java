package com.intel.fangpei.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.network.NIONodeHandler;
import com.intel.fangpei.util.SystemUtil;
import com.intel.fangpei.util.TimeCounter;

public class ProcOutPutHandler extends Thread{
	InputStream is; 
	String type; 
	OutputStream os; 
	packet p = null;
	boolean istimeout = false;
	ProcOutPutHandler(InputStream is, String type) 
	{ 
	this(is, type, null); 
	} 

	ProcOutPutHandler(InputStream is, String type, OutputStream redirect) 
	{ 
	this.is = is; 
	this.type = type; 
	this.os = redirect; 
	} 

	public void run() 
	{ 
		int cachelines = 10000;
		int lines = cachelines;
		String cache = null;
	try 
	{ 
	PrintWriter pw = null; 
	if (os != null) 
	pw = new PrintWriter(os); 

	InputStreamReader isr = new InputStreamReader(is); 
	BufferedReader br = new BufferedReader(isr); 
	String line=null; 
	TimeCounter tc = new TimeCounter(5000);
	while ( (line = br.readLine()) != null) 
	{ 
	if (pw != null) 
	pw.println(line); 
	System.out.println(type + ">" + line); 
	istimeout = tc.isTimeout();
	if(lines >0){
		cache=(cache==null?line:cache+line)+"\n";
		lines --;
		}else{
			writeToServer(cache);
			tc.timeRefresh();
			lines = cachelines;
			cache = null;
		}
	}
	if(cache!=null){
		writeToServer(cache);
	}
	if (pw!= null) 
	pw.flush(); 
	} catch (IOException ioe) 
	{ 
	ioe.printStackTrace(); 
	} 
	}
	public void writeToServer(String cache){
	p = new packet(BasicMessage.NODE,BasicMessage.OP_MESSAGE,SystemUtil.signature()+"\n"+cache);
	NIONodeHandler.processRequest(p);
}
	public boolean MayNeedInput() {
		// TODO Auto-generated method stub
		return istimeout;
	} 
	} 
