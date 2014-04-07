package com.intel.wy.testcases;

import com.intel.fangpei.task.NodeTaskManager;
import com.intel.fangpei.task.TaskRunner;

/**
 * 测试NodeTaskManager优先级排序有效
 * @author WY
 *
 */
public class TestNtmPriority{
	/**
	 * 测试优先级,期望结果是13,10,10,7,5 数字越大优先级越高，排得越前
	 * @param args
	 */
	public static void main(String[] args) {
		NodeTaskManager ntm = new NodeTaskManager();
		ntm.registerTaskRunner(new TaskRunner(), 10);
		ntm.registerTaskRunner(new TaskRunner(), 7);
		ntm.registerTaskRunner(new TaskRunner(), 13);
		ntm.registerTaskRunner(new TaskRunner(), 5);
		ntm.registerTaskRunner(new TaskRunner(), 10);
		ntm.printSortedMap();
	}
}
