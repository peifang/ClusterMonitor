package com.intel.fangpei.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

public class ConfManager {
	private static Configuration conf = new Configuration();
	public ConfManager(){
		
	}
	public static void addResource(String path){
	conf.addResource(new Path("/usr/conf/Monitor_conf.xml"));
	conf.addResource(new Path("c:\\Monitor_conf.xml"));
	}
	public static String getConf(String name){
		 String a = conf.get(name);
		 return a;
	}
	public static int getInt(String name,int num){
	    Integer b = null ;
		try{
			b = Integer.parseInt(conf.get(name));
		}catch(Exception e){
			
		}
		if(b == null)return num;
		else{
			return b;
		}
	}
}
