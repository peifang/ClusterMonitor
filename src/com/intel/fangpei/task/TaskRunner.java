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
import com.intel.fangpei.task.TaskRunner.SplitId;
import com.intel.fangpei.task.TaskRunner.SplitRunner;
import com.intel.fangpei.task.handler.Extender;
import com.intel.fangpei.util.ClientUtil;
import com.intel.fangpei.util.ConfManager;
import com.intel.fangpei.util.Line.segment;
import com.intel.fangpei.util.SystemUtil;

public class TaskRunner implements Runnable{
public static String TASK_WORK_DIR = null;
private int taskid = -1;
private SplitRunner child = null;
private NodeTaskTracker boss = null;
//private Map<SplitId,Integer> ChildIdTojvmId = null;
//private Map<Integer,SplitId> jvmIdToChildId = null;
private Map<JvmRunner,String> jvmToid = null;
private Map<Integer,ChildJvm> idToChildJvm = null;
private Map<Integer,ChildStrategy> idToStrategy = null;
private int runningChildsCount = 0;
private int maxRunningChilds = 0;
private int completeChilds = 0;
private ChildStrategy defaultChildStrate = null;
private TaskStrategy taskstrategy = null;
private Object maplock = null;
private boolean started =false;//added
public TaskRunner(){
	maplock = new Object();
	//ChildIdTojvmId = new HashMap<SplitId,Integer>();
	//jvmIdToChildId = new HashMap<Integer,SplitId>();
	jvmToid       = new HashMap<JvmRunner,String>();
	idToChildJvm    = new HashMap<Integer,ChildJvm>();
	idToStrategy =new HashMap<Integer,ChildStrategy>();
}
public void setTaskStrategy(TaskStrategy starte){
	this.taskstrategy = starte;
}
public void setBoss(NodeTaskTracker tracker){
	this.boss = tracker;
	this.taskid = tracker.nextTaskID();
}
public void report(String s){
	boss.report(s);
}
public ChildStrategy getDefaultStrategy(){
	return new ChildStrategy();
}
/*
 * 3.add JvmTask to the TaskRunner
 */
private ChildJvm expandNewJvm(TaskEnv env){
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
	ChildJvm jvmtask = null;
	synchronized(maplock){
	jvmToid.put(jvm,""+jvm.getpid());
	idToStrategy.put(jvm.getpid(), defaultChildStrate);
	System.out.println("JvmTask jvmToPid:"+jvm.getpid());
	if(env != null){
		jvmtask = new ChildJvm(jvm.getpid(),env);
	}else{
		jvmtask = new ChildJvm(jvm.getpid());
	}
	jvmtask.SetTaskRunner(this);
	idToChildJvm.put(jvm.getpid(), jvmtask);
	maplock.notifyAll();
	}
	return jvmtask;
}
private synchronized ChildJvm expandNewJvm(){	
	return expandNewJvm(null);
}
public void removeJvm(ChildJvm childjvm){
	//remove jvmtoid jvm ~~here
	synchronized(maplock){
	idToStrategy.remove(childjvm.jvmId);
	idToChildJvm.remove(childjvm.jvmId);
	maplock.notifyAll();
	}
}
public class SplitRunner{
	private TaskEnv env = null;
	String taskname = null;
	String[] args = null;
	public SplitRunner(String taskname){
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
public class SplitId{
		public int id = 0;
		public SplitId(){
			this.id = new Random().nextInt(1000);
		}
		public int maxMemoryToUse = 0;
}
public class TaskEnv{
		public String env = "";
}
public static void setupWorkDir(SplitId childid, File file) {
	
}
@Override
public void run() {
	if(taskstrategy == null){
		System.out.println("*no strategy to this TaskRunner*");
		return;
	}
	//starte.addStrategy(this.getDefaultStrategy(), new String[]{"com.intel.developer.extend.myextend"});
	boss.report("start taskRunner!");
	registeAllChildStrategy();
	boss.report("complete TaskRunner init!");
	waitForChildUp();
	while(true){//等rpc结束这个线程
		if(taskstrategy.hasNewStrategy()){
			registeAllChildStrategy();
			waitForChildUp();
		}
		synchronized(maplock){
		Object[] ids = idToChildJvm.keySet().toArray();
		int len =ids.length;
		for(int i=0;i<len;i++){
			ChildJvm jt = idToChildJvm.get(ids[i]);
			if(jt.canStartNextThread()){
				segment s= jt.getSplit();
				if(s == null){
					System.out.println("ummm......we got null ....I don't know");
				    continue;
				}
				System.out.println("[TaskRunner]get segment:"+s.v);
				SplitRunner cr = (SplitRunner)s.v;
				boss.send(jt.jvmId,cr);
				boss.report("[TaskRunner]assign new task, thename is:"+cr.taskname);
			}
			if(jt.noSplitAssign()){
				removeJvm(jt);
				//
				//taskstrategy.remove();
			}
		}
		maplock.notifyAll();
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
public void registeAllChildStrategy(){
	HashMap<ChildStrategy,Boolean> childs = taskstrategy.ChildStrategys();
	Object[] childScanner = childs.keySet().toArray();
	ChildStrategy child = null;
	int childnum = childScanner.length;
	for(int i =0;i <childnum;i++){
		child = (ChildStrategy)childScanner[i];
		if(childs.get(child).equals(true)){
			continue;
		}
		ChildJvm jvmtask = expandNewJvm();
		Map<String, String[]> loads = child.getLoads();
		Iterator<String> loadScanner = loads.keySet().iterator();
		while(loadScanner.hasNext()){
			String load = loadScanner.next();
			SplitRunner tmpchild = new SplitRunner(load);
//			if(loads.get(load)!=null){
//				System.out.println("(*)"+loads.get(load)[0]+":"+loads.get(load)[1]);
//			}
			tmpchild.setArgs(loads.get(load));
			jvmtask.assignNewSplit(new SplitId(),tmpchild);
		}
		//get last work of the JVM
		jvmtask.assignNewSplit(new SplitId(), new SplitRunner(child.getLastWork()));
		child.startStrategyRunner(boss, jvmtask);
		childs.put(child, true);
	}
	taskstrategy.flagRunning();
}
public void extendNewStrategy(ChildStrategy strategy,String[] classname){
	taskstrategy.addStrategy(strategy, classname);
}
public void extendNewStrategy(ChildStrategy strategy,Map<String,String[]> loadToArgs){
	taskstrategy.addStrategy(strategy, loadToArgs);
}
private void waitForChildUp() {
	try {
		Thread.sleep(500);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}
//added
public boolean isStarted(){
	return started;
}
//added
public void setStarted(boolean started) {
	this.started = started;
}
//added
public int getJvmNum(){
	return taskstrategy.getJvmNum();
}
}
