package com.intel.fangpei.network;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.logfactory.MonitorLog;

public class SelectionKeyManager {
	private MonitorLog ml = null;
	// the cluster's nodes list
	private Collection<SelectionKey> nodes = new ArrayList<SelectionKey>();
	// the keys that is needed to process
	private LinkedList<SelectionKey> keys = new LinkedList<SelectionKey>();
	private SelectionKey lastone = null;
	private LinkedList<SelectionKey> keys_read = new LinkedList<SelectionKey>();
	private LinkedList<SelectionKey> keys_write = new LinkedList<SelectionKey>();
	private LinkedList<SelectionKey> keys_cancel = new LinkedList<SelectionKey>();
	private LinkedList<SelectionKey> receivequeue = new LinkedList<SelectionKey>();
	private SelectionKey Admin = null;

	public SelectionKey getAdmin() {
		return Admin;
	}

	public void setAdmin(SelectionKey admin) {
		/*
		 * this function will cancel the orignal Admin key if exist ,and replace
		 * it with admin no matter whether admin is null
		 */
		synchronized (keys) {
			if (Admin == null) {
				Admin = admin;
				keys.notify();
				return;
			}
			if (Admin.equals(admin)) {
				keys.notify();
				return;
			}
			Admin.cancel();
			Admin = admin;
			keys.notify();
		}
	}

	public SelectionKeyManager() {
		try {
			ml = new MonitorLog();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void pushKey(SelectionKey sc) {
		synchronized (keys) {
			if ((lastone != null) && sc.equals(lastone)) {
				keys.notify();
				return;
			}
			keys.add(sc);
			lastone = sc;
			keys.notify();
			return;
		}
	}

	public void pushKeys(Collection<SelectionKey> nodes) {
		Iterator<SelectionKey> i = nodes.iterator();
		SelectionKey key = null;
		synchronized (keys) {
			while (i.hasNext()) {
				key = i.next();
				keys.add(key);
			}
			lastone = key;
			keys.notify();
		}
	}

	public SelectionKey popKey() {
		SelectionKey key = null;
		synchronized (keys) {
			if (keys.isEmpty()) {
				return null;
			}
			key = keys.remove();
			if (keys.isEmpty()) {
				lastone = null;
			}
			keys.notify();
		}
		return key;

	}

	public boolean handleAllNodes(packet p) {
		Iterator<SelectionKey> i = nodes.iterator();
		SelectionKey key = null;
		while (i.hasNext()) {
			key = i.next();
			key.attach(p);
			addWriteInterest(key);
		}
		ml.log("handle all nodes with the command " + p.getCommand());
		return true;
	}

	public void addnode(SelectionKey key) {
		synchronized (nodes) {
			nodes.add(key);
			ml.log("add one node form "
					+ ((SocketChannel) key.channel()).socket().getInetAddress()
							.getHostAddress());
			nodes.notify();
		}

	}

	public void deletenode(SelectionKey key) {
		synchronized (nodes) {
			if (keys_read.contains(key)) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			keys_read.remove(key);
			keys_write.remove(key);
			keys_cancel.remove(key);
			nodes.remove(key);
			try {
				key.channel().close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			key.cancel();
			ml.log("delete one node form "
					+ ((SocketChannel) key.channel()).socket().getInetAddress()
							.getHostAddress());
			nodes.notify();
		}
	}

	public boolean findnode(SelectionKey key) {
		return nodes.contains(key);
	}

	public boolean isNoNodes() {
		return nodes.isEmpty();
	}

	public SelectionKey getOneNode() {

		return nodes.iterator().next();

	}

	public synchronized boolean addReadInterest(SelectionKey key) {
		synchronized (keys_read) {
			keys_read.add(key);
			keys_read.notify();
			return true;
		}
	}

	public SelectionKey popNeedReadKey() {
		synchronized (keys_read) {
		if (keys_read.isEmpty()) {
			return null;
		}
		SelectionKey key = keys_read.remove();
		keys_read.notify();
		return key;
		}
	}

	public synchronized boolean addWriteInterest(SelectionKey key) {
		if(key.isValid()){
		synchronized (keys_write) {
			keys_write.add(key);
			keys_write.notify();
			return true;
		}
		}else{
			return false;
		}
	}

	public SelectionKey popNeedWriteKey() {
		synchronized (keys_write) {
		if (keys_write.isEmpty()) {
			return null;
		}
			SelectionKey key = keys_write.remove();
			keys_write.notify();
			return key;
		}
	}

	public synchronized boolean addCancelInterest(SelectionKey key) {
		synchronized (keys_cancel) {
			keys_cancel.add(key);
			keys_cancel.notify();
			return true;
		}
	}

	public SelectionKey popNeedCancelKey() {
		synchronized (keys_cancel) {
		if (keys_cancel.isEmpty()) {
			return null;
		}
			SelectionKey key = keys_cancel.remove();
			keys_cancel.notify();
			return key;
		}
	}
	public synchronized boolean addNeedProcessKey(SelectionKey key){
		synchronized (receivequeue) {
			receivequeue.add(key);
			receivequeue.notify();
			return true;
		}
		}
	public SelectionKey popNeedProcessKey(){
		synchronized (receivequeue) {
			if(receivequeue.isEmpty()){
				return null;
			}
			SelectionKey key = receivequeue.remove();
			receivequeue.notify();
			return key;
		}
	}

}
