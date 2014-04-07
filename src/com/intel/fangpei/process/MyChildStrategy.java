package com.intel.fangpei.process;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Set;

public class MyChildStrategy extends ChildStrategy {
	private int flag = 0;

	/**
	 * 类名+“.txt”得到的文件存放任务运行结果，如果内容是end表示可以继续做下一个任务
	 */
	public  boolean canDoNextWork(){
		if(flag ==0){
			flag = 1;
			return true;
		}else{
			Set set = child.keySet();
			Iterator it = set.iterator();
			if(it.hasNext()){
				String s = (String) it.next();
				String name = "D:/"+s.substring(s.lastIndexOf(".")+1,s.length())+".txt";
				//System.out.println("file name is : " +name);
				char[] charbf = new char [100];
				try {
					FileReader fr = new FileReader(name);
					fr.read(charbf);
					String tmp = new String(charbf).trim();
					if(tmp.equals("end")){
						System.out.println(name+" will return true");
						return true;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return false;
		}
		
	}
	
}
