package com.intel.fangpei.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.terminal.Admin;
import com.intel.fangpei.util.TimeCounter;

public class NIOHandler implements IConnection, INIOHandler, Runnable {
	protected MonitorLog ml = null;
	protected String serverip = null;
	protected int port = 0;
	protected SocketAddress address = null;
	protected SocketChannel channel = null;
	protected ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024 * 4);
	private TimeCounter tc = null;
	private LinkedList<packet> sendqueue = new LinkedList<packet>();
	private LinkedList<packet> receivequeue = new LinkedList<packet>();
	private int version = 0, argsize = 0;
	byte[] args = null;
	private byte clientType = (byte) 0, command = (byte) 0;
	public NIOHandler(String ip, int port) {
		this.serverip = ip;
		this.port = port;
		try {
			ml = new MonitorLog();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tc = new TimeCounter(5000);
		buffer.clear();
		buffer.flip();
	}

	@Override
	public void processConnect() throws IOException {
		address = new InetSocketAddress(serverip, port);
		channel = SocketChannel.open(address);
		channel.configureBlocking(false);
	}

	@Override
	public synchronized void processRead() throws IOException {
		if(receive() == 0){
			return;
		}
		if(Admin.debug)
		System.out.println("read a packet!"+new String(args));
		packet p = null;
		/*
		 * read a packet once; buffer.putInt(version); buffer.putInt(argsize);
		 * buffer.put(clientType); buffer.put(command);
		 */if(argsize > 0)
			p = new packet(clientType, command, args);
		 else
			p = new packet(clientType, command);
			receivequeue.push(p);
			argsize = 0; 
	}

	@Override
	public synchronized void processWrite() throws IOException {
		if (sendqueue.isEmpty())
			return;
		packet p = sendqueue.pop();
		ByteBuffer buffer = p.getBuffer();
		send(buffer);
	}

	@Override
	public void processError(Exception e) {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void addSendPacket(packet out) {
		sendqueue.add(out);
	}

	public synchronized packet getReceivePacket() {
		if (receivequeue.isEmpty())
			return null;
		return receivequeue.pop();
	}

	@Override
	public void clearSendQueue() {
		sendqueue.clear();

	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		// TODO Auto-generated method stub
		return (InetSocketAddress) address;
	}

	@Override
	public SelectableChannel channel() {
		// TODO Auto-generated method stub
		return channel;
	}

	@Override
	public INIOHandler getNIOHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return receivequeue.isEmpty();
	}

	@Override
	public int receive() throws IOException {
		if(buffer.remaining() < 10){
				buffer.compact();
				channel.read(buffer);
				buffer.flip();
				if(buffer.remaining() < 10){
					return 0;
				}
		}
		/*
		 * bug here;
		 * when i close the server first, the admin will in error;
		 */
		//System.out.print(buffer);
		version = buffer.getInt();
		argsize = buffer.getInt();
		clientType = buffer.get();
		command = buffer.get();
		//System.out.println(argsize+"..."+version);
		if (version != BasicMessage.VERSION) {
			ml.warn("the remote host's version is not compatible with us ,"+version
					+ ", maybe this will make no sense!");
		}
		if(argsize == 0){
			return 10;
		}
		if(buffer.remaining() < argsize ){
			int toread = argsize - buffer.remaining();
			if((buffer.remaining() > 0)){
				buffer.compact();
			}else{
				buffer.clear();
			}
			tc.timeRefresh();
			while(toread > 0 ){
				if(tc.isTimeout()){
					ml.error("have one  packet  error...throw it");
					//clear buffer for next new packet;
					buffer.clear();
					buffer.flip();
					return 0;
				}
				toread -= channel.read(buffer);
			}
			buffer.flip();
			try {
				if (argsize > 0) {
					if(buffer.remaining() < argsize ){
						ml.error("uncomplete packet received");
						buffer.clear();
						buffer.flip();
						return 0;
					}
				}
			} catch (BufferUnderflowException e) {
				ml.error(e.getMessage());
			}
		}
		args = new byte[argsize];
		buffer.get(args, 0, argsize);
		return argsize + 10;
	}

	@Override
	public void send(ByteBuffer buffer) {
		if(Admin.debug)
		System.out.println("send a packet:"+buffer.toString());
		buffer.flip();
		while (buffer.hasRemaining()) {
			try {
				channel.write(buffer);
			} catch (IOException e) {
				ml.error(e.getMessage());
			}
		}
	}

	@Override
	public boolean isConnected() {
		return channel.isConnected();
	}

	@Override
	public void run() {

	}

}
