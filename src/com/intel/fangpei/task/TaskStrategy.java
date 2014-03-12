package com.intel.fangpei.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.intel.fangpei.process.ChildStrategy;
import com.intel.fangpei.task.TaskRunner.SplitId;
import com.intel.fangpei.task.TaskRunner.SplitRunner;
import com.intel.fangpei.util.ReflectFactory;

public class TaskStrategy {
private int jvmNum = 0;
private boolean isRunning = false;
private boolean hasRefresh = false;
private HashMap<ChildStrategy,Boolean> Strategys = null;
public TaskStrategy(){
	Strategys = new HashMap<ChildStrategy,Boolean>();
}
public int getJvmNum() {
	return jvmNum;
}
public void addStrategy(String strategy,String[] classname){
	ChildStrategy mystrategy = null;
	ReflectFactory factory = ReflectFactory.getInstance();
	try{
	if((strategy !=null)&&
			factory.isSubClass(strategy, ChildStrategy.class)){
	mystrategy = (ChildStrategy) factory.getClass(strategy).newInstance();
	
	}
	}catch(ClassNotFoundException e){
		System.out.println("class"+strategy+"is not found ,please check the path");
	}catch(IllegalAccessException e2){
		System.out.println("class"+strategy+"is not accessable!");
	}catch(InstantiationException e3){
		System.out.println("class"+strategy+"is not the instance of Strategy!");
	}
	addStrategy(mystrategy,classname);
	if(isRunning){
		hasRefresh = true;
	}
}
public void addStrategy(ChildStrategy strategy,String[] classname){
	Map<String,String[]> map = new HashMap<String,String[]>();
	int classnum = classname.length;
	for(int i=0;i <classnum;i++){
	map.put(classname[i],null);
	}
	addStrategy(strategy,map);
	if(isRunning){
		hasRefresh = true;
	}
}
public void addStrategy(ChildStrategy strategy,Map<String,String[]> loadToArgs){
	Strategys.put(strategy,false);
	strategy.addLoads(loadToArgs);
	jvmNum ++;
	if(isRunning){
		hasRefresh = true;
	}
}
public synchronized HashMap<ChildStrategy,Boolean> ChildStrategys(){
return Strategys;
}
/**
 * 是否有新的strategy加入。前提是该strategy已经处于运行态
 * @return boolean 是否已经有新的未启动strategy
 */
public boolean hasNewStrategy() {
	if(isRunning&&hasRefresh){
		hasRefresh =false;
		return true;
	}
	return false;
}
/**
 * 标记当前taskstrategy已经处于运行态
 */
public void flagRunning(){
	isRunning = true;
}
}
