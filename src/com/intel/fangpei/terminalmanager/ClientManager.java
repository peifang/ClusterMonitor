package com.intel.fangpei.terminalmanager;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import org.apache.hadoop.hbase.util.Bytes;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.SystemInfoCollector.SysInfo;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.network.SelectionKeyManager;
import com.intel.fangpei.util.ServerUtil;

public class ClientManager {
	MonitorLog ml = null;
	SelectionKey key = null;
	SelectionKeyManager keymanager = null;
	ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024 * 4);
	int version = 0;
	int argsize = 0;
	byte clientType = 0;
	byte command = 0;
	byte[] args = null;

	public ClientManager(SelectionKey key, SelectionKeyManager keymanager) {
		this.keymanager = keymanager;
		this.key = key;
		buffer.clear();
	}

	public boolean sendCommand(packet p) {
		ServerUtil.SendToClient((SocketChannel) key.channel(), p);
		/*
		 * whether the key is for a node? maybe it is the admin's quit command.
		 */
		if (!keymanager.findnode(key)) {
			System.out.println("return false");
			return false;
		}
		return true;
	}

	public boolean Handle() throws BufferUnderflowException {
		buffer = ((packet)key.attachment()).getBuffer();
			unpacket();
			if (clientType == BasicMessage.ADMIN) {
				if (command == BasicMessage.OP_QUIT) {
					keymanager.setAdmin(null);
					key.cancel();
					return false;
				}
				if (command == BasicMessage.OP_LOGIN) {
					packet p = new packet(BasicMessage.SERVER,BasicMessage.OP_MESSAGE,Bytes.toBytes("[message]admin"));
					key.attach(p);
					if (keymanager.addWriteInterest(key)) {
						keymanager.setAdmin(key);
						return true;
					} else {
						return false;
					}
				}
			} else if (clientType == BasicMessage.NODE) {
				if (command == BasicMessage.OP_QUIT) {
					keymanager.deletenode(key);
					return false;
				}
				if (command == BasicMessage.OP_LOGIN) {
					packet p = new packet(BasicMessage.SERVER,BasicMessage.OP_MESSAGE,Bytes.toBytes(" You have registered as a new Node"));
					key.attach(p);
					if (keymanager.addWriteInterest(key)) {
						keymanager.addnode(key);
						return true;
					} else {
						return false;
					}
				}
				if(command ==BasicMessage.OP_SYSINFO){
					HashMap<String,String> hm = SysInfo.deserialize(args);
					System.out.println(hm.get("CPU_Vendor"));
					System.out.println(hm.get("Disk_info"));
					System.out.print(hm.get("RegUser"));
					return true;
				}
			}
			if (!operate()) {
				return false;
			}
		return true;
	}

	private boolean operate() {
		System.out.println("normal process");
		return true;
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
