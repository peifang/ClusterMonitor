package com.intel.fangpei.FileSystem;

import java.util.ArrayList;
import java.util.Arrays;

import com.intel.fangpei.util.ConfManager;

public class Directory {
	private ArrayList<String> dirs = new ArrayList<String>();
	public synchronized void addDir(String dir){
		dirs.add(dir);
	}
	public ArrayList<String>  getDirList(){
		return dirs;
	}
	public synchronized void deletedir(String s){
		dirs.remove(s);
	}
	public void initDir(){
		String dirString = ConfManager.getConf("node.disk.load.dir");
		if(dirString == null){
			return;
		}else{
			String[] dir = dirString.split(",");
			dirs.addAll(Arrays.asList((dir)));
		}
	}
}
