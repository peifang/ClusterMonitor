package com.intel.fangpei.task;

import com.intel.fangpei.task.TaskRunner.ChildId;
import com.intel.fangpei.task.TaskRunner.ChildRunner;
import com.intel.fangpei.task.TaskRunner.TaskEnv;
import com.intel.fangpei.util.Line;
import com.intel.fangpei.util.Line.segment;

public class JvmTask {
int jvmId = 0;
TaskEnv env = null;
private boolean startNextThread = false;
Line<ChildId,ChildRunner> works = new Line<ChildId,ChildRunner>();
TaskRunner boss = null;
public JvmTask(int jvmId) {
	this.jvmId = jvmId;
}
public JvmTask(int jvmId,TaskEnv env) {
	this.jvmId = jvmId;
	this.env = env;
}
public void setEnv(TaskEnv env){
	this.env = env;
}
public void SetTaskRunner(TaskRunner tr){
	boss = tr;
}
/*
 * if use this function:
 * return true:
 * perform OK for start next work
 * return false:
 * already start a signal for start next work and the work has not been
 * performed OK!please wait...
 */
public synchronized boolean nextWork(){
	if(startNextThread){
		return false;
	}
	boss.report("[jvmtask]prepare to start next child task in the JVM:"+jvmId);
	startNextThread = true;
	return true;
}
public segment getChild() {
	return works.popNode();
}
public void assignNewChild(ChildId id,ChildRunner cr){
	cr.setEnv(env);
	works.addNode(id, cr);
}
public boolean noTaskAssign() {
	if(works!=null&&works.hasNext()){
		return false;
	}
	return true;
}

public boolean canStartNextThread() {
	if(startNextThread){
		startNextThread = false;
		return true;
		}
	return false;
}

}
