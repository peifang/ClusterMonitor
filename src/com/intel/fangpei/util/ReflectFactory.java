package com.intel.fangpei.util;
import java.io.*;
import java.lang.reflect.Constructor;

import com.intel.fangpei.logfactory.MonitorLog;

public class ReflectFactory {
	MonitorLog ml = null;
	private static ReflectFactory reflect = null;
	private ClassLoader ccl =null;
private ReflectFactory(){
	ccl = Thread.currentThread().getContextClassLoader();
	try {
		this.ml = new MonitorLog();
	} catch (Exception e) {
		MonitorLog.loglog("cann't instance log :");
		MonitorLog.loglog(e.getMessage());
	} 
}
public static synchronized ReflectFactory getInstance(){
	
	if(reflect != null){
		return reflect;
	}
		reflect = new ReflectFactory();
		return reflect;
}
public Class getClass(String classname){
	 try {
		return ccl.loadClass(classname);
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		ml.error(e.getMessage());
		return null;
	}	
}
public Object getMyInstance(String classname) throws IllegalAccessException, InstantiationException{
	System.out.println(classname);
	Class c = getClass(classname);
	if(c!=null){
		System.out.println("===asdas===");
		return c.newInstance();
	}
	return null;
}
public Object getMyInstance(String classname,String[] args)throws Exception{
	Class c = getClass(classname);
	if(c!=null){
		int paranum = args.length;
		Class<?>[] pType = new Class[paranum];
		for(int i=0;i <paranum;i++){
	         pType[i] = Class.forName("java.lang.String");
	    } 
		Constructor<?> cons = c.getConstructor(pType);
		 return cons.newInstance(args);
	}
	return null;
}
@SuppressWarnings("unchecked")
public boolean isSubClass(Class classname,Class c){
	return c.isAssignableFrom(classname);
}
@SuppressWarnings({ "unchecked", "rawtypes" })
public boolean isSubClass(String classname,Class c){
	Class c2 = getClass(classname);
	if(c!=null){
		return c.isAssignableFrom(c2);	
	}
	return false;

}
}
