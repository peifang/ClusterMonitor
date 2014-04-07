package com.intel.developer.extend;
import java.io.FileWriter;
import java.io.IOException;

import com.intel.fangpei.task.handler.Extender;

public class wyextend5 extends Extender {
	public wyextend5(){
		System.out.println("nothing to do!");
	}
	//֧�ִ�������ǲ���ֻ����String���ͣ�
	public wyextend5(String s){
		System.out.println(s);
	}
	public wyextend5(String s,String s2){
		System.out.println(s+"~"+s2);
	}
	public void commitTask(){
		for(int i=1;i<=5;i++){
			System.out.println("5:"+i);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("work over 5");
		FileWriter fw;
		try {
			fw = new FileWriter("D:/wyextend5.txt");
			fw.write("end");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
