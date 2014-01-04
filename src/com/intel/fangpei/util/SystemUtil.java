package com.intel.fangpei.util;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Properties;

public class SystemUtil {
	private static String machineName = null;
public static void initSysParameter(HashMap<String,String> map){
	/*
	 * this func will init the SysInfoHandler's resource .It contains 
	 * CPU,Memory and Disk .Detail.
	 */
	map.put("CPU_Vendor", "");
	map.put("CPU_Celeron", "");
	map.put("CPU_Cache", "");
	map.put("CPU_Count", "");
	map.put("CPU_Mhz", "");
	map.put("CPU_Perc", "");
	map.put("Memory_Total", "");
	map.put("Memory_Used", "");
	map.put("Memory_Free", "");
	map.put("Memory_Swap_Total", "");
	map.put("Memory_Swap_Used", "");
	map.put("Memory_Swap_Free", "");
	map.put("System_Name", "");
	map.put("System_Vendor", "");
	map.put("RegUser", "");
	map.put("Disk_info", "");
	map.put("NetWork_FQDN", "");
	map.put("NetWork_Name","");
	map.put("NetWork_IP", "");
	map.put("NetWork_NetMask","");
	map.put("NetWork_MAC", "");
	map.put("NetWork_RxPackets", "");
	map.put("NetWork_TxPackets", "");
	map.put("NetWork_RxBytes", "");
	map.put("NetWork_TxBytes", "");
	map.put("NetWork_RxError", "");
	map.put("NetWork_TxError", "");
	map.put("NetWork_RxDropped", "");
	map.put("NetWork_TxDropped", "");
	map.put("NIC_Description", "");
}

public static String signature() {
	if(machineName != null){
		return machineName;
	}else{
		try {
			machineName ="----->"+InetAddress.getLocalHost().getHostName()+"("+
					InetAddress.getLocalHost().getHostAddress()+"):";
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return machineName;
}
}
public static String operationType(){
	Properties prop = System.getProperties();
	String os = prop.getProperty("os.name");
	return os;
	
}
public static String buildSysPath(){
	String syslib  = ConfManager.getConf("node.system.lib.path");
	System.out.println("SystemPath:"+syslib);
	String lib ="";
	if(syslib!=null){
	File f = new File(syslib);
	if(f.isDirectory()){
		File[] libs = f.listFiles();
		for(int i =0;i<libs.length-1;i++){
			if(libs[i].isFile())
			lib += libs[i].getAbsolutePath()+":";
		}
		if(libs[libs.length-1].isFile()){
			lib+=libs[libs.length-1].getAbsolutePath();
		}
		else{
			lib = lib.substring(0, lib.length()-1);
		}
	}
	System.out.println("ClusterMonitor System path is:"+lib);
	return lib;
	}
	return "";
}
public static void main(String[] args){
	operationType();
}
}