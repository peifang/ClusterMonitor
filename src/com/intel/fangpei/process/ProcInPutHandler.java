package com.intel.fangpei.process;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class ProcInPutHandler extends Thread{
	OutputStream os;

	ProcInPutHandler(OutputStream os) 
	{ 
		this.os = os; 
	} 
	public void write(String s){
		try {
			os.write(s.getBytes());
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void terminal(){
		try {
			os.flush();
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
