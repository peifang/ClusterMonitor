package com.intel.fangpei.process;

import org.apache.commons.lang.ArrayUtils;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.network.NIONodeHandler;
import com.intel.fangpei.task.ExtendTask;
import com.intel.fangpei.task.TaskRunner;
import com.intel.fangpei.task.TaskRunner.SplitId;
import com.intel.fangpei.util.ConfManager;

public class Child{
	static MonitorLog ml = null;
	  static TaskRunner tr = null;
	  static SplitId childid = null;
	  static String jvmId="";
	  public static void main(String[] args) throws Throwable {
		  ml = new MonitorLog();
		  ml.log("a new child is in construct...");
		  ConfManager.addResource(null);
	    String serverip = args[0];
	    int port = Integer.parseInt(args[1]);
	    NIONodeHandler node = new NIONodeHandler(serverip,port);
	    Thread demon = new Thread(node);
	    demon.setDaemon(true);
	    demon.start();
	    jvmId = args[2];
		packet one = new packet(BasicMessage.NODE, BasicMessage.OP_LOGIN,jvmId.getBytes());
		node.addSendPacket(one);
	    //final String logLocation = args[3];
	    //int jvmIdInt = Integer.parseInt(jvmId);
//	    String cwd = System.getenv().get(TaskRunner.TASK_WORK_DIR);
//	    if (cwd == null) {
//	      throw new IOException("Environment variable " + 
//	                             TaskRunner.TASK_WORK_DIR + " is not set");
//	    }
	    
	    int numTasksToExecute = -1; //-1 signifies "no limit"
	    int numTasksExecuted = 0;
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
	    ml.log("child started!");
	    //wait time for task prepare
	    long millis = 10;
	    packet inprocessTaskPacket = null;
	      while (true) {
	        if ((inprocessTaskPacket = node.getReceivePacket()) == null) {
	          Thread.sleep(millis);
	          continue;
	        } else {
	        	System.out.println("child "+jvmId+" recevied:"+new String(inprocessTaskPacket.getBuffer().array()));
	        	byte[] taskArgsbytes = inprocessTaskPacket.getArgs();
	        	if(taskArgsbytes == null){
	        		System.out.println("fack!!!-----------error");
	        	}
	        	String taskArgsString = new String(taskArgsbytes);
	        	String[] taskArgs = taskArgsString.trim().split(" ");
	        	String taskname = taskArgs[0];
	        	ml.log("receive a new task:"+taskname);
	        	/*
	        	 * end the process signal.
	        	 */
	        	if(taskname.equals("break")){
	        		ml.log("all work of the JVM:"+jvmId+"have complete.");
	        		one = new packet(BasicMessage.NODE, BasicMessage.OP_QUIT,jvmId.getBytes());
	        		node.addSendPacket(one);
	        		node.flush();
	        		Thread.sleep(1000);
	        		while(true){
	        			while(node.getReceivePacket()!=null);
	        			System.exit(0);
	        			}
	        	}
	        	ExtendTask task = null;
	        	if(taskArgs.length > 1){
	        	String[] otherArgs = (String[]) ArrayUtils.subarray(taskArgs, 1, taskArgs.length);
	        	task = new ExtendTask(ml,taskname,otherArgs);
	        	System.out.println("extend task with args");
	        	}else{
	        	task = new ExtendTask(ml,taskname);
	        	}
	        	Thread workthread = new Thread(task);
	        	workthread.setName("child jvm work thread");
	        	workthread.start();
	               }
	      			   }
	    
	  }
}
