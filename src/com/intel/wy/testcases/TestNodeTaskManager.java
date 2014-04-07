package com.intel.wy.testcases;

import java.io.FileWriter;
import java.io.IOException;

import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.process.MyChildStrategy;
import com.intel.fangpei.task.NodeTaskTracker;
import com.intel.fangpei.task.TaskRunner;
import com.intel.fangpei.task.TaskStrategy;
/**
 * 测试Jvm限制功能和优先级调度
 * 
 * @author WY
 *
 */
public class TestNodeTaskManager {

	/**
	 * tr1和tr2各有两个child strategy,tr3有一个child strategy，tr3的优先级高于tr2,jvm限制为3
	 * 预期先运行tr1和tr3，再运行tr2
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		clean();
		NodeTaskTracker tracker = new NodeTaskTracker(new MonitorLog());
		
		TaskRunner tr1 = createTaskRunner(new String[]{"com.intel.developer.extend.wyextend"});
		tr1.extendNewStrategy(new MyChildStrategy(), new String[]{"com.intel.developer.extend.wyextend2"});
		
		TaskRunner tr2 = createTaskRunner(new String[]{"com.intel.developer.extend.wyextend3"});
		tr2.extendNewStrategy(new MyChildStrategy(), new String[]{"com.intel.developer.extend.wyextend4"});
		
		TaskRunner tr3 = createTaskRunner(new String[]{"com.intel.developer.extend.wyextend5"});
		System.out.println("tr1's child num: "+tr1.getJvmNum());
		System.out.println("tr2's child num: "+tr2.getJvmNum());
		System.out.println("tr3's child num: "+tr3.getJvmNum());
		//TaskRunner tr4 = createTaskRunner(new String[]{"com.intel.developer.extend.wyextend4"});
		tracker.addNewTaskMonitorWithPriority(tr1, 9);
		Thread.sleep(1000);
		tracker.addNewTaskMonitorWithPriority(tr2, 7);
		Thread.sleep(1000);
		tracker.addNewTaskMonitorWithPriority(tr3, 8);
	
//		Thread.sleep(1000);
//		tracker.addNewTaskMonitorWithPriority(tr4, 6);
	}
	public static TaskRunner createTaskRunner(String[] s){
		TaskRunner tr = new TaskRunner();
		TaskStrategy strate = null;
		strate = new TaskStrategy();
		//strate.addStrategy(tr.getDefaultStrategy(),s);
		strate.addStrategy(new MyChildStrategy(),s);
		tr.setTaskStrategy(strate);
		return tr;
		
		
	}
	public static void clean(){
		FileWriter fw;
		try {
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
