package com.intel.wy.testcases;

import com.intel.fangpei.terminal.Node;

/**
 * ���Դ�Ȩ�޵�extendTask�ӿڿ���
 * @author WY
 *
 */
public class TestExtendNewStrategy {
	public static void main(String[]args) throws Exception{
		TestExtendNewStrategy ts = new TestExtendNewStrategy();
		ts.test();
	}
	public void test() throws Exception{
		Node n = new Node();
		n.extendTask("abc",  new String[]{"com.intel.developer.extend.myextend"}, 0);
		n.extendTask(null, new String[]{"com.intel.developer.extend.myextend"}, 0);
	}
	
}
