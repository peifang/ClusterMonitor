package com.intel.developer.extend;
import com.intel.fangpei.task.handler.ExtendHandler;

public class myextend extends ExtendHandler {
	public myextend(){
		
	}
	//支持传入参数，但是参数只能是String类型；
	public myextend(String s){
		System.out.println(s);
	}
	public myextend(String s,String s2){
		System.out.println(s+"~"+s2);
	}
	public void commitTask(){
		System.out.println("work over!");
	}
}
