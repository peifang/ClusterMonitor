package com.intel.developer.extend;
import com.intel.fangpei.task.handler.Extender;

public class myextend extends Extender {
	public myextend(){
		System.out.println("nothing to do!");
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
