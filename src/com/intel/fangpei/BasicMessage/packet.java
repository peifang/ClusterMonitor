package com.intel.fangpei.BasicMessage;

import java.nio.ByteBuffer;

import com.intel.fangpei.logfactory.MonitorLog;
/**
 * basic class for data exchange on data format. 
 * @author fangpei
 *
 */
public class packet {
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getArgsize() {
		return argsize;
	}

	public void setArgsize(int argsize) {
		this.argsize = argsize;
	}

	public byte getClientType() {
		return clientType;
	}

	public void setClientType(byte clientType) {
		this.clientType = clientType;
	}

	public byte getCommand() {
		return command;
	}

	public void setCommand(byte command) {
		this.command = command;
	}

	public byte[] getArgs() {
		return args;
	}

	public void setArgs(byte[] args) {
		this.args = args;
	}

	private int version = 0;
	private int argsize = 0;
	private byte clientType = 0;
	private byte command = 0;
	byte[] args = null;
	public packet(ByteBuffer buffer) {
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

	public packet(byte clientType, byte command) {
		this.clientType = clientType;
		this.version = BasicMessage.VERSION;
		this.argsize = 0;
		this.command = command;

	}

	public packet(byte clientType, byte command, byte[] args) {
		if (args == null) {
			this.clientType = clientType;
			this.version = BasicMessage.VERSION;
			this.argsize = 0;
			this.command = command;
			return;
		}
		this.clientType = clientType;
		this.version = BasicMessage.VERSION;
		this.argsize = args.length;
		this.command = command;
		this.args = args;
	}

	public packet(byte clientType, byte command, String args) {
		if (args == null) {
			this.clientType = clientType;
			this.version = BasicMessage.VERSION;
			this.argsize = 0;
			this.command = command;
			return;
		}
		byte[] arg = args.getBytes();
		this.clientType = clientType;
		this.version = BasicMessage.VERSION;
		this.argsize = arg.length;
		this.command = command;
		this.args = arg;
	}

	public ByteBuffer getBuffer() {
		ByteBuffer buffer = null;
		if (args == null) {
			buffer = ByteBuffer.allocate(10);
			buffer.clear();
		} else {
			buffer = ByteBuffer.allocate(args.length + 10);
		}

		buffer.putInt(version);
		buffer.putInt(argsize);
		buffer.put(clientType);
		buffer.put(command);
		if (args != null) {
			buffer.put(args);
		}
		;
		return buffer;
	}
	public static packet getOnePacket(ByteBuffer buffer){
		if(buffer.remaining() < 10){
			return null;
		}
		int version = buffer.getInt();
		if (version != BasicMessage.VERSION) {
			System.out.println("the remote host's version is not compatible with us ,"
					+ " maybe this will make no sense!");
		}
		
		int argsize = buffer.getInt();
		byte clientType = buffer.get();
		byte command = buffer.get();
		if(buffer.remaining() < argsize ){
			buffer.rewind();
			return null;
		}else{
			byte[] args = new byte[argsize];
			buffer.get(args, 0, argsize);
			return new packet(clientType,command,args);
		}
	}

	public int size() {
		// TODO Auto-generated method stub
		return argsize + 10;
	}
}
