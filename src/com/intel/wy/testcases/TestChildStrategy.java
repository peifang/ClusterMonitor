package com.intel.wy.testcases;

import java.io.FileWriter;
import java.io.IOException;

import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.process.MyChildStrategy;
import com.intel.fangpei.process.MyChildStrategy2;
import com.intel.fangpei.task.NodeTaskTracker;
import com.intel.fangpei.task.TaskRunner;
import com.intel.fangpei.task.TaskStrategy;
/**
 * 测试childstrategy策略可用
 * 
 * @author WY
 *
 */
public class TestChildStrategy {

	/**
	 * 预期运行完myextend1 才运行 myextend2
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		clean();
		NodeTaskTracker tracker = new NodeTaskTracker(new MonitorLog());
		
		TaskRunner tr1 = createTaskRunner(new String[]{"com.intel.developer.extend.myextend1","com.intel.developer.extend.myextend2"});
		 
		tracker.addNewTaskMonitorWithPriority(tr1, 9);
		
//		Thread.sleep(1000);
//		tracker.addNewTaskMonitorWithPriority(tr4, 6);
	}
	public static TaskRunner createTaskRunner(String[] s){
		TaskRunner tr = new TaskRunner();
		TaskStrategy strate = null;
		strate = new TaskStrategy();
		//strate.addStrategy(tr.getDefaultStrategy(),s);
		strate.addStrategy(new MyChildStrategy2(),s);
		tr.setTaskStrategy(strate);
		return tr;
		
		
	}
	public static void clean(){
		FileWriter fw;
		try {
			fw = new FileWriter("D:/myextend1.txt");
			fw.write(" ");
			fw.close();
			fw = new FileWriter("D:/wyextend.txt");
			fw.write(" ");
			fw.close();
			FileWriter fw2 = new FileWriter("D:/wyextend2.txt");
			fw2.write(" ");
			fw2.close();
			FileWriter fw3 = new FileWriter("D:/wyextend3.txt");
			fw3.write(" ");
			fw3.close();
			FileWriter fw4 = new FileWriter("D:/wyextend4.txt");
			fw4.write(" ");
			fw4.close();
			FileWriter fw5 = new FileWriter("D:/wyextend5.txt");
			fw5.write(" ");
			fw5.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
