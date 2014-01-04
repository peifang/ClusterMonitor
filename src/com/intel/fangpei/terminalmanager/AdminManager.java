package com.intel.fangpei.terminalmanager;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.hbase.util.Bytes;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.ServiceMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.network.NIOServerHandler;
import com.intel.fangpei.network.SelectionKeyManager;
import com.intel.fangpei.util.ServerUtil;

public class AdminManager extends SlaveManager{
	SelectionKey admin = null;
	MonitorLog ml = null;
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
			System.out.println(((SocketChannel)admin.channel()).socket().getInetAddress()
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
		case BasicMessage.OP_lOAD_DISK:
			p = new packet(BasicMessage.SERVER, command, args);
			System.out.println("exec admin's load disk command!");
			keymanager.handleAllNodes(nioserverhandler,p);
			packet reply3 = new packet(BasicMessage.SERVER, BasicMessage.OK);
			nioserverhandler.pushWriteSegement(admin,reply3);
			break;
		}
		return "";
	}

}
