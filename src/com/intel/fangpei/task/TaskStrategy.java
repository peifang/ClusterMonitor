package com.intel.fangpei.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.intel.fangpei.process.ChildStrategy;
import com.intel.fangpei.util.ReflectFactory;

public class TaskStrategy {
private int jvmNum = 0;
private ArrayList<ChildStrategy> Strategys = null;
public TaskStrategy(){
	Strategys = new ArrayList<ChildStrategy>();
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
}
public void addStrategy(ChildStrategy strategy,String[] classname){
	Map<String,String[]> map = new HashMap<String,String[]>();
	int classnum = classname.length;
	for(int i=0;i <classnum;i++){
	map.put(classname[i],null);
	}
	addStrategy(strategy,map);
}
public void addStrategy(ChildStrategy strategy,Map<String,String[]> loadToArgs){
	Strategys.add(strategy);
	strategy.addLoads(loadToArgs);
	jvmNum ++;
}
public synchronized ArrayList<ChildStrategy> ChildStrategys(){
return Strategys;
}
}
