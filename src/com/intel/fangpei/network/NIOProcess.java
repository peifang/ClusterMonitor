package com.intel.fangpei.network;

import java.nio.BufferUnderflowException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.network.PacketLine.segment;
import com.intel.fangpei.terminalmanager.AdminManager;
import com.intel.fangpei.terminalmanager.ClientManager;

public class NIOProcess implements Runnable {
	private MonitorLog ml = null;
	SelectionKeyManager keymanager = null;
	ClientManager cm = null;
	AdminManager am = null;
	Selector selector = null;
	NIOServerHandler nioserverhandler = null;
	public NIOProcess(Selector selector, SelectionKeyManager keymanager) {
		this.keymanager = keymanager;
		this.selector = selector;
		try {
			ml = new MonitorLog();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		nioserverhandler = new NIOServerHandler(ml,keymanager);
	}

	@Override
	public void run() {
		new Thread(nioserverhandler).start();
		while (true) {
			segment se = nioserverhandler.getNewSegement();
			if (se == null) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			System.out.println("NIOProcess:get out a new segment");
			SelectionKey key = se.key;
			packet p = se.p;
			if (key.equals(keymanager.getAdmin())) {
				System.out.println("admin key");
				am = new AdminManager(keymanager.getAdmin(), keymanager,nioserverhandler);
					if (am.Handle(key,p)) {
						ml.log("have read and handled the admin's request.");
					}
			} else {
				System.out.println("client key");
				cm = new ClientManager(keymanager,nioserverhandler);
					try {
						if (cm.Handle(key,p)) {
							ml.log("have read and handled the client's request.");
						}
					} catch (BufferUnderflowException e) {
						ml.warn("Loss part of one or more packets,throw these!");
					}
			}
		}

	}

}
