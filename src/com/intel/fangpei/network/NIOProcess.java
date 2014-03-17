package com.intel.fangpei.network;

import java.nio.BufferUnderflowException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.network.PacketLine.segment;
import com.intel.fangpei.terminalmanager.AdminManager;
import com.intel.fangpei.terminalmanager.ClientManager;
import com.intel.fangpei.util.SystemUtil;

public class NIOProcess implements Runnable {
	private MonitorLog ml = null;
	SelectionKeyManager keymanager = null;
	ClientManager cm = null;
	AdminManager am = null;
	NIOServerHandler nioserverhandler = null;
	public NIOProcess(SelectionKeyManager keymanager,NIOServerHandler nioserverhandler) {
		this.keymanager = keymanager;
		this.nioserverhandler = nioserverhandler;
		try {
			ml = new MonitorLog();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public NIOProcess(NIOServerHandler nioserverhandler) {
		this.nioserverhandler = nioserverhandler;
		try {
			ml = new MonitorLog();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	@Override
	public void run() {
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
			System.out.println("[NIOProcess]get out a new packet:"+SystemUtil.byteToString(se.p.getArgs()));
			SelectionKey key = se.key;
			packet p = se.p;
			if (key.equals(keymanager.getAdmin())) {
				System.out.println("[NIOProcess]this a packet from admin");
				am = new AdminManager(ml,keymanager.getAdmin(), keymanager,nioserverhandler);
					if (am.Handle(key,p)) {
						ml.log("have read and handled the admin's request.");
					}
			} else {
				System.out.println("[NIOProcess]this a packet from client");
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
