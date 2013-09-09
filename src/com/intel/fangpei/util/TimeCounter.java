package com.intel.fangpei.util;

public class TimeCounter {
	long timeout = 0;
	long timenow = 0;
	long mix = 0;
	public TimeCounter(long timeout){
		this.timeout = timeout;
		timenow = System.currentTimeMillis();
	}
	public void timeRefresh(){
		timenow = System.currentTimeMillis();
	}
	public void setTimeNow(long time){
		this.timenow = time; 
	}
	public boolean isTimeout(){
		mix = System.currentTimeMillis() - timenow;
		return mix < timeout ? false:true;
	}
}
