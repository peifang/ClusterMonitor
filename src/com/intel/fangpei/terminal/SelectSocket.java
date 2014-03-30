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

import org.apache.commons.logging.Log;

import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.network.NIOServerHandler;
import com.intel.fangpei.network.SelectionKeyManager;
import com.intel.fangpei.terminalmanager.AdminManager;
import com.intel.fangpei.util.ConfManager;
import com.intel.fangpei.network.NIOProcess;
import com.intel.fangpei.network.rpc.RpcServer;

/**
 * This is the main entrance for server to start up.
 * <p>If you want to start your own server ,you can use {@link com.intel.fangpei.util.ServerUtil} 
 * @author fangpei
 * 
 */
public class SelectSocket {
	public static int data = 0;
	private static final int PORT_NUMBER = 1234;
	public static MonitorLog ml = null;
	private static int processThreadNum = 0;
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

	public void go(String[] args) {
		int port = ConfManager.getInt("selectsocket.server.port", PORT_NUMBER);
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (Exception e) {
			}
		}
			ml.log("/*Start depend on Process...");
			ml.log("/*Start RPC Server...");
			Thread t = new Thread(){
				public void run(){
				int port = ConfManager.getInt("selectsocket.rpc.port", 1235);
				RpcServer rpc= new RpcServer(port);	
				rpc.StartRPCServer();
				}
			};
			t.setDaemon(true);
			t.start();
			ml.log("/*Start " + processThreadNum + " Key handle Threads...");
			SelectionKeyManager keymanager = new SelectionKeyManager();
			ml.log("/*Key handle Threads had started!");
			ml.log("/*Server Listening at port: " + PORT_NUMBER);
			ml.log("/*Start Server...");
			NIOServerHandler nioserverhandler = new NIOServerHandler(1234,ml,keymanager);
			new Thread(nioserverhandler).start();
			for (int i = 0; i < processThreadNum; i++)
			new Thread(new NIOProcess(keymanager,nioserverhandler)).start();
			ml.log("/*Server has been started!");
	}
}
