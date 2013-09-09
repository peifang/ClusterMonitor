package com.intel.fangpei.network;

import java.nio.BufferUnderflowException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.terminalmanager.AdminManager;
import com.intel.fangpei.terminalmanager.ClientManager;

public class NIOProcess implements Runnable {
	private MonitorLog ml = null;
	SelectionKeyManager keymanager = null;
	ClientManager cm = null;
	AdminManager am = null;
	Selector selector = null;

	public NIOProcess(Selector selector, SelectionKeyManager keymanager) {
		this.keymanager = keymanager;
		this.selector = selector;
	}

	@Override
	public void run() {
		try {
			ml = new MonitorLog();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (true) {
			SelectionKey key = keymanager.popNeedProcessKey();
			if (key == null) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}

			if (key.equals(keymanager.getAdmin())) {
				am = new AdminManager(keymanager.getAdmin(), keymanager);
					if (am.Handle()) {
						ml.log("have read and handled the admin's request.");
					}
			} else {
				cm = new ClientManager(key, keymanager);
					try {
						if (cm.Handle()) {
							ml.log("have read and handled the client's request.");
						}
					} catch (BufferUnderflowException e) {
						ml.warn("Loss part of one or more packets,throw these!");
					}
			}
		}

	}

}
