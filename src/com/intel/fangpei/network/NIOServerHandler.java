package com.intel.fangpei.network;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.util.TimeCounter;

public class NIOServerHandler implements INIOHandler,Runnable{
//	private LinkedList<SelectionKey> sendqueue = new LinkedList<SelectionKey>();
	private LinkedList<SelectionKey> receivequeue = new LinkedList<SelectionKey>();
	private SelectionKeyManager manager = null;
	private SelectionKey inprocesskey = null;
	private ByteBuffer buffer = null;
	private int version = 0;
	private byte clientType = 0;
	private byte command = 0;
	private int argsize = 0;
	private byte[] args = null;
	private packet p = null;
	private TimeCounter tc = null;
	private MonitorLog ml = null;
	public NIOServerHandler(MonitorLog ml,SelectionKeyManager manager){
		this.manager  = manager;
		this.ml = ml;
		buffer = ByteBuffer.allocate(1024*1024*4);
		buffer.clear();
		tc = new TimeCounter(10000);
	}
	@Override
	public void processConnect() throws IOException {
		// it have been processed by selectsocket ;

	}

	@Override
	public void processRead() throws IOException {
		//while((inprocesskey = manager.popNeedReadKey()) != null){
			SocketChannel channel = (SocketChannel) inprocesskey.channel();
			try{
			if(receive(channel) <= 0){
				System.out.println("read node from "
						+ channel.socket().getInetAddress().getHostAddress()
						+ " fail,exclude the node!");
				manager.addCancelInterest(inprocesskey);
				return;
			}
			}catch(IOException e){
				System.out.println("read node from "
						+ channel.socket().getInetAddress().getHostAddress()
						+ " fail,exclude the node!");
				manager.addCancelInterest(inprocesskey);
				return;
			}
			if(argsize > 0)
				p = new packet(clientType, command, args);
			else
				p = new packet(clientType, command);
				inprocesskey.attach(p);
				manager.addNeedProcessKey(inprocesskey);
				manager.addReadInterest(inprocesskey);
				argsize = 0; 
		}

	//}

	@Override
	public void processWrite() throws IOException {
	//	while((inprocesskey = manager.popNeedWriteKey()) != null){
			SocketChannel channel = (SocketChannel) inprocesskey.channel();
			p = (packet) inprocesskey.attachment();
			if(channel.write((ByteBuffer)(p.getBuffer().flip())) < p.size()){
				ml.warn("server send little bytes than expected!");
			}
			manager.addReadInterest(inprocesskey);
		}

	//}

	@Override
	public void processError(Exception e) {
		// TODO Auto-generated method stub

	}
	public int receive(SocketChannel channel) throws IOException {
		buffer.clear();
		buffer.limit(10);
		channel.read(buffer);
		if(buffer.position() != buffer.limit()){
			buffer.clear();
			return 0;
		}
		buffer.flip();
		version = buffer.getInt();
		if (version != BasicMessage.VERSION) {
			ml.warn("the remote host's version is not compatible with us ,"
					+ " maybe this will make no sense!");
		}
		argsize = buffer.getInt();
		clientType = buffer.get();
		command = buffer.get();
		//over the packet size;
		if(argsize > buffer.capacity() - 10){
			ml.error("packet args size is over the packet size limit" +
					",and the limit" +
					"is "+(buffer.capacity()-10)+"," +
							"but the packet has "+argsize);
			return -1;
		}
		buffer.clear();
		if(argsize == 0){
			return 10;
		}else{
			buffer.limit(argsize);
			tc.timeRefresh();
			while(buffer.remaining() > 0 ){
				if(tc.isTimeout()){
					ml.error("have one  packet read error...please check the link?");
					buffer.clear();
					return 0;
				}
				channel.read(buffer);
			}
			buffer.flip();
			args = new byte[argsize];
			buffer.get(args, 0, argsize);
			buffer.clear();
		}
		return argsize + 10;
	}
	@Override
	public void run() {
		
		while (true) {
			inprocesskey = manager.popKey();
			if (inprocesskey == null) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			try {
				if (inprocesskey.isReadable())
				processRead();
				else if(inprocesskey.isWritable())
				processWrite();
				Thread.sleep(50);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
