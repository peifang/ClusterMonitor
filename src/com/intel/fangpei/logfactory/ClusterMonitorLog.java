package com.intel.fangpei.logfactory;

public abstract class ClusterMonitorLog {
	String className = "";
	String filePath = "";

	public abstract void log(String message) throws Exception;
}
