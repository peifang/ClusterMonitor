package com.intel.fangpei.terminalmanager;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.hbase.util.Bytes;

import com.intel.fangpei.BasicMessage.AppHandler;
import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.ServiceMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.network.NIOServerHandler;
import com.intel.fangpei.network.SelectionKeyManager;
import com.intel.fangpei.util.ServerUtil;
/**
 * server use this class to communicate with Admin.
 * @author fangpei
 *
 */
public class AdminManager extends SlaveManager{
	class ServerTaskMonitor{
		//key taskid value map
		private HashMap<Integer, HashMap<Integer, Double>> response = new HashMap<Integer,HashMap<Integer, Double>>();
		public boolean newTask(int taskid){
			if(response.containsKey(taskid)){
				return false;
			}
			synchronized(response){
			response.put(taskid,new HashMap<Integer, Double>());
			response.notifyAll();
			}
			return true;
		}
		public boolean removeTask(int taskid){
			synchronized(response){
				if(response.remove(taskid) == null){
					return false;
				}
				response.notifyAll();
				return true;
			}
		}
		public int taskCompleteNum(int taskid){
			int count = 0;
			synchronized(response){
			if(response.containsKey(taskid)){
				HashMap<Integer, Double> tmp = response.get(taskid);
				Iterator<Entry<Integer, Double>> iter = tmp.entrySet().iterator();
				while (iter.hasNext()) {
				Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>) iter.next();
				int key = entry.getKey();
				double value = entry.getValue();
				if(value > 0.9999999){
					count ++;
				}
				}
			}
			response.notifyAll();
			}
			return count;			
		}
		public void complete(int taskid,int childid,double percent){
			synchronized(response){
			if(response.containsKey(taskid)){
				response.get(taskid).put(childid, percent);
			}
			response.notifyAll();
			}
		}
	}
	SelectionKey admin = null;
	MonitorLog ml = null;
	AppHandler handler = new AppHandler(1);
	private ServerTaskMonitor tasks = new ServerTaskMonitor();
	public boolean taskregister(int taskid){
		return tasks.newTask(taskid);
	}
	public void taskcomplete(int taskid,int childid,double percent){
		tasks.complete(taskid, childid, percent);
	}
	public int taskcompleteNum(int taskid){
		return tasks.taskCompleteNum(taskid);
	}
	public void removeTask(int taskid){
		//uncomplete
	}
	public AdminManager(MonitorLog ml,SelectionKey admin, SelectionKeyManager keymanager,NIOServerHandler nioserverhandler) {
		super(keymanager,nioserverhandler);
		this.ml = ml;
		this.admin = admin;
	}
	public boolean Handle(packet p){
		return Handle(admin,p);
	}
	public boolean Handle(SelectionKey admin,packet p) {
		buffer = p.getBuffer();
		// System.out.println("task"+buffer.mark()+" "+buffer.limit()+" "+buffer.remaining());
		unpacket();
		if (args != null)
			System.out.println("[AdminManager]"+((SocketChannel)admin.channel()).socket().getInetAddress()
					.getHostAddress()
					+ ":" + new String(args) + "[end]");
		if (command == BasicMessage.OP_QUIT) {
			ml.log("handle admin's quit request");
			packet one = new packet(BasicMessage.SERVER,
					BasicMessage.OP_MESSAGE, "offline");
			nioserverhandler.pushWriteSegement(admin,one);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			nioserverhandler.removeWriteKey(admin);
			keymanager.setAdmin(null);
			keymanager.addCancelInterest(admin);
			return false;
		}
		if (command == BasicMessage.OP_SH) {
			ml.log("handle admin's sh request");
			HandOneNode();
			return true;
		}
		AllHandsHandler();
		return true;
	}

	private void HandOneNode() {
		String tmp = new String(args);
		tmp = tmp.trim();
		String[] meta = tmp.split(" ");
		SelectionKey sk = keymanager.getOneNode(meta[0]);
		try{
		packet p = new packet(BasicMessage.SERVER, command, Bytes.toBytes(tmp.substring(tmp.indexOf(" "))));
		nioserverhandler.pushWriteSegement(sk,p);
		}catch (StringIndexOutOfBoundsException e){
		packet p = new packet(BasicMessage.SERVER, command);
		nioserverhandler.pushWriteSegement(sk,p);
		}
		nioserverhandler.pushWriteSegement(admin,new packet(BasicMessage.SERVER, BasicMessage.OK));
	}

	public String AllHandsHandler() {
		packet p = null;
		switch (command) {
		case BasicMessage.OP_EXEC:
		case ServiceMessage.SERVICE:
		case ServiceMessage.THREAD:
			p = new packet(BasicMessage.SERVER, command, args);
			ml.log("handle admin's exec|service|thread request");
			keymanager.handleAllNodes(nioserverhandler,p);
			break;
		case BasicMessage.OP_CLOSE:
		case BasicMessage.OP_MESSAGE:
		case BasicMessage.OP_SYSINFO:
			ml.log("handle admin's close|message|sysinfo request");
			p = new packet(BasicMessage.SERVER, command, args);
			keymanager.handleAllNodes(nioserverhandler,p);
			packet reply = new packet(BasicMessage.SERVER, BasicMessage.OK);
			nioserverhandler.pushWriteSegement(admin,reply);
			break;
		}
		return "";
	}

}
