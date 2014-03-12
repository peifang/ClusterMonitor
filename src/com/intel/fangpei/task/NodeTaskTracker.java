package com.intel.fangpei.task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.ali.fangpei.service.wrapWork;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.task.TaskRunner.SplitRunner;
import com.intel.fangpei.util.ServerUtil;
import com.intel.fangpei.util.TimeCounter;

public class NodeTaskTracker {
private TaskNotifyServer server = null;
private int taskid = 10000;
private boolean isRunning = false;
/***
 * bug:monitorlog 写入文件冲突问题
 */
private MonitorLog ml = null;
private ArrayList<TaskRunner> runners = new ArrayList<TaskRunner>();
public NodeTaskTracker(MonitorLog ml){
	this.ml = ml;
	server = new TaskNotifyServer();
	new Thread(server).start();
	//wait for tasknotifyserver to start...
	try {
		Thread.sleep(1000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
public int nextTaskID(){
	taskid++;
	return taskid;
}
public void report(String s){
	ml.log(s);
}
public void addNewTaskMonitor(TaskRunner tr){
	runners.add(tr);
	tr.setBoss(this);
	new Thread(tr).start();
}
public void send(int jvmId,SplitRunner cr){
	server.send(jvmId, cr);
}
public void send(int jvmId,wrapWork ww){
	server.send(jvmId, ww);
}
public boolean isRegisted(int jvmid){
	return server.isRegistedJvm(jvmid);
}
	public class TaskNotifyServer extends Thread{
		HashMap<Integer,SocketChannel> registedJvm = new HashMap<Integer,SocketChannel>();
		public synchronized void send(int jvmId,SplitRunner cr){
			ByteBuffer buffer = cr.toTransfer().getBuffer();
			//System.out.println(buffer);
			SocketChannel channel = registedJvm.get(jvmId);
			if(channel == null||(!channel.isConnected())){
				ml.error("we cann't find the JVM which you want to send to"
						+ ",maybe the JVM has been destroyed ");
				return;
			}
			buffer.flip();
			while (buffer.hasRemaining()) {
				try {
					System.out.println("[TaskTracker]buffer is:"+new String(buffer.array()));
					int c = channel.write(buffer);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (IOException e) {
					ml.error("send command to the JVM failed,"
							+ "please check the detail!");
					ml.error(e.getMessage());
				}catch(NullPointerException e){
					System.out.println("channel is none now," +
							"it's jvmid is:"+jvmId
							+",remove this registed key");
					registedJvm.remove(jvmId);
					ml.error("channel is none," +
							"but still some data haven't sent:"
							+new String(buffer.array()));
					ml.error(e.getMessage());
				}
			}
		}
		public boolean isRegistedJvm(int jvmid) {
			return registedJvm.containsKey(jvmid);		
		}
		public synchronized void send(int jvmId,wrapWork ww){
			ByteBuffer buffer = ww.toTransfer().getBuffer();
			//System.out.println(buffer);
			SocketChannel channel = registedJvm.get(jvmId);
			if(channel == null||(!channel.isConnected())){
				ml.error("we cann't find the Service JVM ,"
						+ ",maybe the JVM has been destroyed,"
						+ "please do check the ServiceDemo!");
			}
			buffer.flip();
			while (buffer.hasRemaining()) {
				try {
					channel.write(buffer);
				} catch (IOException e) {
					ml.error("send command to Service JVM failed,"
							+ "please check the detail!");
					ml.error(e.getMessage());
					break;
					
				}
			}
		}
		public void run(){
			Selector selector = null;
			try{
				selector = Selector.open();
			ServerSocketChannel serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);
			ServerSocket serverSocket = serverChannel.socket();
			serverSocket.bind(new InetSocketAddress(4399));
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			}catch(Exception e){
				ml.logWithThreadName("port:"+"4399 is been used by other programs...exit...");
				return;
			}
			
			isRunning = true;
			ml.logWithThreadName("TaskTracker is running...");
			while(isRunning){
				int n = 0;
				try {
					n = selector.select(100);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				if (n == 0) {
					continue;
				}
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SocketChannel channel = null;
					SelectionKey key = it.next();
					if(!key.isValid()){
						it.remove();
						continue;
					}
					if (key.isAcceptable()) {
						ServerSocketChannel server = (ServerSocketChannel) key
								.channel();
						try {
							channel = server.accept();
							ml.log("tracker detect a new jvm connection");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (channel == null) {
							continue;
						}
						try {
							channel.configureBlocking(false);
							channel.register(selector, SelectionKey.OP_READ);
							ByteBuffer buffer = ByteBuffer.allocate(7+Integer.SIZE/8);
							buffer.clear();
							if(ServerUtil.ReceiveWithTimeout(channel, buffer) <= 0){
								ml.error("when accept the jvm connect,we cann't get" +
										"the procid it sends,throw the jvm");
								it.remove();
								continue;
							}
							buffer.flip();
							try{
							int jvmid = Integer.parseInt(new String(packet.getOnePacket(buffer).getArgs()));
							//System.out.println("this jvm id :"+jvmid);	
							registedJvm.put(jvmid, channel);
							}catch(NullPointerException e){
								ml.error("when the JVM registe on NotifyServer,we got uncomplete Buffer!");
								it.remove();
								continue;
							}
						} catch (IOException e) {
						}
						it.remove();
						continue;
					}
					it.remove();
					key.interestOps(key.interestOps() & (~key.readyOps()));
			}
		}
	}

}
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return isRunning;
	}
	public void killTracker(){
		isRunning = false;
	}
}
