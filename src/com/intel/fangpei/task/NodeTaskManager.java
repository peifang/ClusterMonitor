package com.intel.fangpei.task;

import java.util.concurrent.locks.ReentrantLock;

import com.intel.fangpei.process.ProcessManager;
import com.intel.fangpei.task.NodeTaskManager.PriorityLine.PriorityNode;
import com.intel.fangpei.util.ConfManager;

public class NodeTaskManager implements Runnable {
	private static int DEFAULT_JVM_LIMIT = 3;
	/**
	 * jvm���Ƹ���
	 */
	private int jvmLimit = 0;
	/**
	 * ��ǰjvm��������ͬ��childstrategy����
	 */
	private int jvmNum = 0;
	/**
	 * ��������jvm��������������ɵ�jvm����
	 */
	private int startedJvmNum = 0;
	/**
	 * �жϵ�ǰnodetaskmanager�Ƿ���������Ϊ���߳�ֻ������һ��
	 */
	private boolean isStarted = false;

	private ReentrantLock lock = new ReentrantLock();

	PriorityLine line;

	public NodeTaskManager() {
		jvmLimit = ConfManager.getInt("jvm.limit", DEFAULT_JVM_LIMIT);
		//jvmLimit = 3;
		line = new PriorityLine();
	}

	/**
	 * add a node to line
	 * 
	 * @param ts
	 * @param priority
	 */
	public synchronized void registerTaskRunner(TaskRunner tr, int priority) {
		System.out.println("register taskrunner,priority : " + priority);
		line.addNode(priority, tr);
	}

	/**
	 * ���ڲ��ԣ���ӡ�����Ľ��
	 */
	public void printSortedMap() {
		line.print();
	}

	/**
	 * ��ȡ��ǰjvm������������ڵ�������jvmLimit�����˯1�룬Ȼ���ٻ�ȡ��ǰjvm������
	 * ���Ű�˳������sortedTaskRunners�е�TR��ֱ����ǰjvm����+��һ��TR��jvm������������jvmLimit����mapԪ�ض��ꡣ
	 */
	@Override
	public void run() {
		isStarted = true;
		while (true) {
			startedJvmNum = line.getStartedNum();
			jvmNum = startedJvmNum - ProcessManager.getFinishedJvmNum();
			while (jvmNum >= jvmLimit) {
				try {
					Thread.sleep(1000);
					System.out.println("Zzz..");
					jvmNum = startedJvmNum - ProcessManager.getFinishedJvmNum();
					System.out.println("process jvm num is : " + jvmNum);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			lock.lock();
			
			try{
				PriorityNode node = line.firstNode();
				while (node != null) {
					if (node.v.isStarted()) {
						node = node.next;
					} else {
						TaskRunner tr = node.v;
						if (tr.getJvmNum() + jvmNum <= jvmLimit) {
							tr.setStarted(true);
							new Thread(tr).start();
							jvmNum += tr.getJvmNum();
							System.out.println("start thread priority is :"
									+ node.k + ", jvmNum is:" + jvmNum+", runtime is "+ProcessManager.getFinishedJvmNum());
						} else {
							break;
						}
					}
				}
			}
			finally{
				lock.unlock();
				
		}
		}
	}

	/**
	 * �жϵ�ǰ�߳��Ƿ��Ѿ�����
	 * 
	 * @return
	 */
	public synchronized boolean isStarted() {
		System.out.println("ntm is started : " + isStarted);
		return isStarted;
	}

	/**
	 * ���õ�ǰ�߳�״̬
	 * 
	 * @param isStarted
	 */
	public void setStarted(boolean isStarted) {
		this.isStarted = isStarted;
	}

	class PriorityLine {
		protected PriorityNode head = null;

		public class PriorityNode {
			Integer k = null;
			TaskRunner v = null;
			PriorityNode next = null;

			PriorityNode(Integer k, TaskRunner v) {
				this.k = k;
				this.v = v;
			}
		}

		private void addNode(PriorityNode node) {
			PriorityNode p = head;
			PriorityNode prev = p;
			if (p == null) {
				head = node;
			}
			// ���node��headԪ�ػ�Ҫ����node��Ϊ�µ�head
			else if (head.k.compareTo(node.k) < 0) {
				node.next = head;
				head = node;
			} else {
				while (p.next != null && p.next.k.compareTo(node.k) >= 0) {
					p = p.next;
				}
				// ������һ��Ԫ�ض���node����node��ӵ�����β��
				if (p.next == null) {
					p.next = node;
				}
				// node����p��p.next֮��
				else {
					PriorityNode tmp = p.next;
					p.next = node;
					node.next = tmp;
				}

			}
		}

		public void addNode(Integer k, TaskRunner v) {
			lock.lock();
			System.out.println("line lock");
			try{
				System.out.println("add node");
				addNode(new PriorityNode(k, v));
			}
			finally{
				System.out.println("line unlock");
				lock.unlock();
			}
		}

		public PriorityNode firstNode() {
			return head;
		}
		//��δʹ��
		public PriorityNode firstUnstartedNode() {
			PriorityNode p = head;
			if (p == null) {
				return null;
			}
			while (p != null) {
				if (p.v.isStarted()) {
					p = p.next;
				} else {
					return p;
				}
			}
			return p;

		}

		public void print() {
			PriorityNode p = head;
			while (p != null) {
				System.out.println("priority is : " + p.k);
				p = p.next;
			}
		}

		public int getStartedNum() {
			int startedNum = 0;
			PriorityNode p = head;
			while (p != null) {
				if (p.v.isStarted()) {
					startedNum+=p.v.getJvmNum();
				}
				p = p.next;
			}
			return startedNum;
		}

	}

}
