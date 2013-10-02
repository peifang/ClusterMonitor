package com.intel.fangpei.terminal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;

import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.network.NIOServerHandler;
import com.intel.fangpei.network.SelectionKeyManager;
import com.intel.fangpei.terminalmanager.AdminManager;
import com.intel.fangpei.util.ConfManager;
import com.intel.fangpei.network.NIOProcess;

/**
 * @author kyle
 * 
 */
public class SelectSocket {
	SelectionKeyManager keymanager = new SelectionKeyManager();
	Selector selector = null;
	AdminManager ad = null;
	public static int data = 0;
	private static final int PORT_NUMBER = 1234;
	public static MonitorLog ml = null;
	public static int processThreadNum = 0;

	public static void main(String[] args) {
		ConfManager.addResource(null);
		try {
			ml = new MonitorLog();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		processThreadNum = 2;
		SelectSocket selectsocket = new SelectSocket();
		selectsocket.go(args);
	}

	private void go(String[] args) {
		int port = ConfManager.getInt("selectsocket.server.port", PORT_NUMBER);
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (Exception e) {
			}
		}

		try {
			ml.log("/*Start depend on Process...");
			ml.log("/*Start to Start Server H2 DataBase:");
			//DataBase db = new DataBase();
//			ProcessBuilder pb = new ProcessBuilder("java","-jar","InfoManager.jar");
//			pb.directory(new File("C:\\Users\\peifang\\Desktop\\sys"));
//			Map<String,String> m = pb.environment();	
//			String classpath = m.get("CLASS_PATH");
//			m.put("CLASS_PATH", classpath+";C:\\Users\\peifang\\Desktop\\sys\\InfoManager.jar;C:\\Users\\peifang\\Desktop\\sys\\h2-1.3.173.jar;C:\\Users\\peifang\\Desktop\\LIB\\hadoop-core-1.0.3-Intel.jar");
//			Iterator enviterator = m.keySet().iterator();
//			while(enviterator.hasNext()){
//				Object key = enviterator.next();
//				System.out.println(key+":"+m.get(key));
//			}
//			Process p = pb.start();
//			InputStreamReader reader = new InputStreamReader(p.getErrorStream());
//			BufferedReader bufferreader = new BufferedReader(reader);
//			String line = null;
//			while((line = bufferreader.readLine())!= null){
//				System.out.println(line);
//			}
			ml.log("/*DataBase Server H2 have been started");
			selector = Selector.open();
			ml.log("/*Start " + processThreadNum + " Key handle Threads...");
			for (int i = 0; i < processThreadNum; i++)
				new Thread(new NIOProcess(selector, keymanager)).start();
			//new Thread(new NIOServerHandler(ml,keymanager)).start();
			ml.log("/*Key handle Threads had started!");
			ml.log("/*Server Listening at port: " + PORT_NUMBER);
			ml.log("/*Start Server...");
			startServer(port, selector);
			ml.log("/*Server has been started!");
			while (true) {
				CheckInterest();
				int n = selector.select(100);
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
					if (key.isValid()&&key.isAcceptable()) {
						ServerSocketChannel server = (ServerSocketChannel) key
								.channel();
						try {
							channel = server.accept();
							ml.log("accept a new connection from "
									+ channel.socket().getInetAddress()
											.getHostAddress());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						registerChannel(selector, channel, SelectionKey.OP_READ);
						it.remove();
						continue;
					}
					it.remove();
					key.interestOps(key.interestOps() & (~key.readyOps()));
					keymanager.pushKey(key);
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void CheckInterest() {
		while (true) {
			SelectionKey key = keymanager.popNeedReadKey();
			if (key != null && key.isValid()) {
				key.interestOps(key.interestOps() & (~key.readyOps()));
				key.interestOps(SelectionKey.OP_READ);
			} else {
				break;
			}
		}
//		while (true) {
//			SelectionKey key = keymanager.popNeedWriteKey();
//			if (key != null && key.isValid()) {
//				key.interestOps(key.interestOps() & (~key.readyOps()));
//				key.interestOps(SelectionKey.OP_WRITE);
//			} else {
//				break;
//			}
//		}
		while (true) {
			SelectionKey key = keymanager.popNeedCancelKey();
			if (key != null) {
				ml.log("delete one node from "
						+ ((SocketChannel) key.channel()).socket()
								.getInetAddress().getHostAddress());
				key.cancel();
			} else {
				break;
			}
		}
	}

	private void startServer(int port, Selector selector) throws IOException,
			ClosedChannelException {
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		ServerSocket serverSocket = serverChannel.socket();
		serverSocket.bind(new InetSocketAddress(port));
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
	}

	private void registerChannel(Selector selector, SocketChannel channel,
			int opRead) {

		if (channel == null) {
			return;
		}
		try {
			channel.configureBlocking(false);
			channel.register(selector, opRead);
		} catch (IOException e) {

		}

	}
	public void close(){
		try {
			selector.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
	}
}
