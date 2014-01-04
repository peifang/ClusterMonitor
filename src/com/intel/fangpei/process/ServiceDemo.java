package com.intel.fangpei.process;

import org.apache.commons.lang.ArrayUtils;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.network.NIONodeHandler;
import com.intel.fangpei.task.ExtendTask;
import com.intel.fangpei.task.TaskRunner;
import com.intel.fangpei.task.TaskRunner.ChildId;
import com.intel.fangpei.util.ConfManager;

public class ServiceDemo {
	static MonitorLog ml = null;
	  static TaskRunner tr = null;
	  static ChildId childid = null;
	  public static void main(String[] args) throws Throwable {
		  ProcessFactory.setStartID(10000);
		  ml = new MonitorLog("/service.log");
		  ml.log("Service Demo is in construct...");
		  ConfManager.addResource(null);
	    String serverip = args[0];
	    int port = Integer.parseInt(args[1]);
	    ml.log("Service Demo: server ip:"+serverip+",port:"+port);

	    NIONodeHandler node = new NIONodeHandler(serverip,port);
	    Thread demon = new Thread(node);
	    demon.setDaemon(true);
	    demon.start();
	    String jvmId = args[2];
		packet one = new packet(BasicMessage.NODE, BasicMessage.OP_LOGIN,jvmId.getBytes());
		node.addSendPacket(one);
	    Runtime.getRuntime().addShutdownHook(new Thread() {
	      public void run() {
	        try {
	          if (childid != null) {
	           //add shutdown hook context;
	          }
	        } catch (Throwable throwable) {
	        }
	      }
	    });
	    Thread t = new Thread() {
	      public void run() {
	        //every so often wake up and syncLogs so that we can track
	        //logs of the currently running task
	        while (true) {
	          try {
	            Thread.sleep(5000);
	            if (childid != null) {
	            //demond task here;
	            }
	          } catch (InterruptedException ie) {
	          }
	        }
	      }
	    };
	    t.setName("Thread for Logs");
	    t.setDaemon(true);
	    t.start();
	    //wait time for task prepare
	    long millis = 1000;
	    packet inprocessTaskPacket = null;
	      while (true) {
	        if ((inprocessTaskPacket = node.getReceivePacket()) == null) {
	          Thread.sleep(millis);
	          continue;
	        } else {
	        	ml.log("Service Demo Receive:"+inprocessTaskPacket.getBuffer());
	        	byte[] taskArgsbytes = inprocessTaskPacket.getArgs();
	        	if(taskArgsbytes == null){
	        		ml.error("Service Demo Receive:fack!!! no data-----------");
	        	}
	        	String taskArgsString = new String(taskArgsbytes);
	        	String[] taskArgs = taskArgsString.trim().split(" ");
	        	ml.log("Service Demo Receive:receive a new task!");
	        	String taskname = taskArgs[0];
	        	ExtendTask task = null;
	        	if(taskArgs.length > 1){
	        	String[] otherArgs = (String[]) ArrayUtils.subarray(taskArgs, 1, taskArgs.length);
	        	task = new ExtendTask(ml,taskname,otherArgs);
	        	ml.log("Service Demo Receive:extend task with args");
	        	}else{
	        	task = new ExtendTask(ml,taskname);
	        	}
	        	Thread workthread = new Thread(task);
	        	workthread.setName("service");
	        	workthread.start();
	               } 
	        }
	    
	  }
}
