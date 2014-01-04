package com.intel.fangpei.task;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.network.rpc.RpcClient;
import com.intel.fangpei.process.ChildStrategy;
import com.intel.fangpei.process.ProcessFactory;
import com.intel.fangpei.process.ProcessManager;
import com.intel.fangpei.task.TaskRunner.ChildId;
import com.intel.fangpei.task.TaskRunner.ChildRunner;
import com.intel.fangpei.task.handler.Extender;
import com.intel.fangpei.util.ClientUtil;
import com.intel.fangpei.util.ConfManager;
import com.intel.fangpei.util.SystemUtil;

public class TaskRunner implements Runnable{
public static String TASK_WORK_DIR = null;
private int taskid = -1;
private ChildRunner child = null;
private TaskTracker boss = null;
private Map<ChildId,Integer> ChildIdTojvmId = null;
private Map<Integer,ChildId> jvmIdToChildId = null;
private Map<JvmRunner,String> jvmToPid = null;
private Map<Integer,JvmTask> idToJvmTask = null;
private Map<Integer,ChildStrategy> jvmIdToStrategy = null;
private int runningChildsCount = 0;
private int maxRunningChilds = 0;
private int completeChilds = 0;
private ChildStrategy defaultChildStrate = null;
private TaskStrategy starte = null;
public TaskRunner(){
	ChildIdTojvmId = new HashMap<ChildId,Integer>();
	jvmIdToChildId = new HashMap<Integer,ChildId>();
	jvmToPid       = new HashMap<JvmRunner,String>();
	idToJvmTask    = new HashMap<Integer,JvmTask>();
	jvmIdToStrategy =new HashMap<Integer,ChildStrategy>();
}
public void setTaskStrategy(TaskStrategy starte){
	this.starte = starte;
}
public void setBoss(TaskTracker tracker){
	this.boss = tracker;
	this.taskid = tracker.nextTaskID();
}
public void report(String s){
	boss.report(s);
}
public ChildStrategy getDefault(){
	return new ChildStrategy();
}
/*
 * 3.add JvmTask to the TaskRunner
 */
public synchronized JvmTask expandNewJvm(TaskEnv env){
	JvmRunner jvm = new JvmRunner("127.0.0.1",4399);
	//jvm.setDaemon(true);
	jvm.start();
	while(!jvm.isStarted()){
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	jvmToPid.put(jvm,""+jvm.getpid());
	jvmIdToStrategy.put(jvm.getpid(), defaultChildStrate);
	System.out.println("JvmTask jvmToPid:"+jvm.getpid());
	JvmTask jvmtask = null;
	if(env != null){
		jvmtask = new JvmTask(jvm.getpid(),env);
	}else{
		jvmtask = new JvmTask(jvm.getpid());
	}
	jvmtask.SetTaskRunner(this);
	idToJvmTask.put(jvm.getpid(), jvmtask);
	return jvmtask;
}
public synchronized JvmTask expandNewJvm(){	
	return expandNewJvm(null);
}
public class ChildRunner{
	private TaskEnv env = null;
	String taskname = null;
	String[] args = null;
	public ChildRunner(String taskname){
		this.taskname = taskname;
		
	}
	public void setEnv(TaskEnv env){
		this.env = env;
	}
	public void setArgs(String... args){
		this.args = args;
	}
	/*
	 * one task name and args
	 */
public packet toTransfer() {
	StringBuilder sb = new StringBuilder();
	sb.append(taskname);
	sb.append(" ");
	if(args!=null){
	for(int i = 0;i < args.length;i++){
	sb.append(args[i]);
	sb.append(" ");
	}
	}
	return new packet(BasicMessage.NODE,BasicMessage.OP_MESSAGE,sb.toString().getBytes());
}
}
/*
 * start child process and wait;
 */
public class JvmRunner extends Thread{
	private int processid = -1;
	private boolean started =false;
	public JvmRunner(String ip,int port){
		
	}
	public boolean isStarted(){
		return started;
	}
	public int getpid(){
		return processid;
	}
	public void run(){
		if(SystemUtil.operationType().startsWith("Win")){
			processid = ProcessFactory.buildNewProcessWithProcessid("java","-cp","../cluster.jar","-Djava.ext.dirs=../tools/lib","com.intel.fangpei.process.Child","127.0.0.1","4399");	
		}else{
		String userlib = ConfManager.getConf("node.task.lib.path");
		String lib = SystemUtil.buildSysPath();
		if(userlib == null){
		processid = ProcessFactory.buildNewProcessWithProcessid("java","-cp",lib,"com.intel.fangpei.process.Child","127.0.0.1","4399");	
		}else{
			System.out.println("ClusterMonitor User extended path is:"+userlib);
		processid = ProcessFactory.buildNewProcessWithProcessid("java","-cp",lib,"-Djava.ext.dirs="+userlib,"com.intel.fangpei.process.Child","127.0.0.1","4399");
		}
		//ProcessManager.get(processid).setAllEnv(System.getenv());
		//ProcessManager.get(processid).setWorkDir("/root/fangpei");
		}
		started = true;
		ProcessManager.start(processid);
	}	
}
/*
 * child conf metrix;
 */
public class ChildId{
		public int id = 0;
		public ChildId(){
			this.id = new Random().nextInt(1000);
		}
		public int maxMemoryToUse = 0;
}
public class TaskEnv{
		public String env = "";
}
public static void setupWorkDir(ChildId childid, File file) {
	
}
@Override
public void run() {
	ChildStrategy handler = null;
	if(starte == null){
		System.out.println("*no strategy to this TaskRunner*");
		return;
	}
	boss.report("start taskRunner!");
	ArrayList<ChildStrategy> childs = starte.ChildStrategys();
	Iterator<ChildStrategy> childScanner = childs.iterator();
	while(childScanner.hasNext()){
		ChildStrategy child = childScanner.next();
		JvmTask jvmtask = expandNewJvm();
		Map<String, String[]> loads = child.getLoads();
		Iterator<String> loadScanner = loads.keySet().iterator();
		while(loadScanner.hasNext()){
			String load = loadScanner.next();
			ChildRunner tmpchild = new ChildRunner(load);
//			if(loads.get(load)!=null){
//				System.out.println("(*)"+loads.get(load)[0]+":"+loads.get(load)[1]);
//			}
			tmpchild.setArgs(loads.get(load));
			jvmtask.assignNewChild(new ChildId(),tmpchild);
		}
		//get last work of the JVM
		jvmtask.assignNewChild(new ChildId(), new ChildRunner(child.getLastWork()));
		child.startStrategyRunner(boss, jvmtask);
	}
	boss.report("complete TaskRunner init!");
	while(true){
		Iterator<Integer> i = idToJvmTask.keySet().iterator();
		while(i.hasNext()){
			JvmTask jt = idToJvmTask.get(i.next());
			if(jt.canStartNextThread()){
				ChildRunner cr = (ChildRunner) jt.getChild().v;
				boss.send(jt.jvmId,cr);
				boss.report("[TaskRunner]assign new task, thename is:"+cr.taskname);
			}
		}
		try {
			//System.out.print(".");
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
}
