package com.intel.fangpei.terminalmanager;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.hbase.util.Bytes;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.network.NIOServerHandler;
import com.intel.fangpei.network.SelectionKeyManager;
import com.intel.fangpei.util.ServerUtil;

public class AdminManager extends SlaveManager{
	SelectionKey admin = null;
	public AdminManager(SelectionKey admin, SelectionKeyManager keymanager,NIOServerHandler nioserverhandler) {
		super(keymanager,nioserverhandler);
		this.admin = admin;
	}
	public boolean Handle(packet p){
		return Handle(admin,p);
	}
	public boolean Handle(SelectionKey admin,packet p) {
		System.out.println("handle admin request");
		buffer = p.getBuffer();
		System.out.println("admin packet is:"+buffer.toString());
		// System.out.println("task"+buffer.mark()+" "+buffer.limit()+" "+buffer.remaining());
		unpacket();
		if (args != null)
			System.out.println(((SocketChannel)admin.channel()).socket().getInetAddress()
					.getHostAddress()
					+ ":" + new String(args) + "[end]");
		if (command == BasicMessage.OP_QUIT) {
			packet one = new packet(BasicMessage.SERVER,
					BasicMessage.OP_MESSAGE, "offline");
			nioserverhandler.pushWriteSegement(admin,one);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			keymanager.setAdmin(null);
			keymanager.addCancelInterest(admin);
			return false;
		}
		if (command == BasicMessage.OP_SH) {
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
		System.out.println("exec a new admin exec node command!");
		nioserverhandler.pushWriteSegement(admin,new packet(BasicMessage.SERVER, BasicMessage.OK));
	}

	public String AllHandsHandler() {
		packet p = null;
		switch (command) {
		case BasicMessage.OP_EXEC:
			p = new packet(BasicMessage.SERVER, command, args);
			System.out.println("exec a new admin exec node command!");
			keymanager.handleAllNodes(nioserverhandler,p);
			//admin.attach(new packet(BasicMessage.SERVER, BasicMessage.OK));
			//keymanager.addWriteInterest(admin);
			break;
		case BasicMessage.OP_CLOSE:
		case BasicMessage.OP_MESSAGE:
		case BasicMessage.OP_SYSINFO:
			p = new packet(BasicMessage.SERVER, command, args);
			System.out.println("exec a new admin command!");
			keymanager.handleAllNodes(nioserverhandler,p);
			packet reply = new packet(BasicMessage.SERVER, BasicMessage.OK);
			nioserverhandler.pushWriteSegement(admin,reply);
			break;
		case BasicMessage.OP_HTABLE_CREATE:
			if (keymanager.isNoNodes()) {
				break;
			} else {
				SelectionKey node_for_execute_command = keymanager.getOneNode();
				packet htableinfo = new packet(BasicMessage.SERVER, command,
						args);
				nioserverhandler.pushWriteSegement(node_for_execute_command,htableinfo);
			}
			break;
		case BasicMessage.OP_lOAD_HBASE:
			p = new packet(BasicMessage.SERVER, command, args);
			System.out.println("exec admin's load hbase command!");
			keymanager.handleAllNodes(nioserverhandler,p);
			packet reply2 = new packet(BasicMessage.SERVER, BasicMessage.OK);
			nioserverhandler.pushWriteSegement(admin,reply2);
			
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
