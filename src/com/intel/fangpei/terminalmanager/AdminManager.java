package com.intel.fangpei.terminalmanager;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.network.SelectionKeyManager;
import com.intel.fangpei.util.ServerUtil;

public class AdminManager {
	SelectionKey admin = null;
	SelectionKeyManager keymanager = null;
	ByteBuffer buffer = ByteBuffer.allocate(1024);
	int version = 0;
	int argsize = 0;
	byte clientType = 0;
	byte command = 0;
	byte[] args = null;

	public AdminManager(SelectionKey admin, SelectionKeyManager keymanager) {
		this.admin = admin;
		this.keymanager = keymanager;
	}

	public boolean Handle() {
		buffer = ((packet)admin.attachment()).getBuffer();
		// System.out.println("task"+buffer.mark()+" "+buffer.limit()+" "+buffer.remaining());
		unpacket();
		if (args != null)
			System.out.println(((SocketChannel)admin.channel()).socket().getInetAddress()
					.getHostAddress()
					+ ":" + new String(args) + "[end]");
		if (command == BasicMessage.OP_QUIT) {
			packet one = new packet(BasicMessage.SERVER,
					BasicMessage.OP_MESSAGE, "offline");
			admin.attach(one);
			keymanager.addWriteInterest(admin);
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
		AllHandsHandler();
		return true;
	}

	public String AllHandsHandler() {
		packet p = null;
		switch (command) {
		case BasicMessage.OP_EXEC:
		case BasicMessage.OP_MESSAGE:
		case BasicMessage.OP_CLOSE:
		case BasicMessage.OP_SYSINFO:
			p = new packet(BasicMessage.SERVER, command, args);
			System.out.println("exec a new admin command!");
			keymanager.handleAllNodes(p);
			packet reply = new packet(BasicMessage.SERVER, BasicMessage.OK);
			admin.attach(reply);
			keymanager.addWriteInterest(admin);
			break;
		case BasicMessage.OP_HTABLE_CREATE:
			if (keymanager.isNoNodes()) {
				break;
			} else {
				SelectionKey node_for_execute_command = keymanager.getOneNode();
				packet htableinfo = new packet(BasicMessage.SERVER, command,
						args);
				node_for_execute_command.attach(htableinfo);
				keymanager.addWriteInterest(node_for_execute_command);
			}
			break;
		case BasicMessage.OP_lOAD_HBASE:
			p = new packet(BasicMessage.SERVER, command, args);
			System.out.println("exec admin's load hbase command!");
			keymanager.handleAllNodes(p);
			packet reply2 = new packet(BasicMessage.SERVER, BasicMessage.OK);
			admin.attach(reply2);
			keymanager.addWriteInterest(admin);
			
			break;
		case BasicMessage.OP_lOAD_DISK:
			p = new packet(BasicMessage.SERVER, command, args);
			System.out.println("exec admin's load disk command!");
			keymanager.handleAllNodes(p);
			packet reply3 = new packet(BasicMessage.SERVER, BasicMessage.OK);
			admin.attach(reply3);
			keymanager.addWriteInterest(admin);
			break;
		}
		return "";
	}

	private void unpacket() {
		buffer.flip();
		version = buffer.getInt();
		argsize = buffer.getInt();
		clientType = buffer.get();
		command = buffer.get();
		if (argsize != 0) {
			args = new byte[buffer.remaining()];
			buffer.get(args);
		}
	}

}
