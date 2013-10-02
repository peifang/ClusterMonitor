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
import com.intel.fangpei.network.PacketLine.segment;
import com.intel.fangpei.util.ServerUtil;
import com.intel.fangpei.util.TimeCounter;

public class NIOServerHandler implements INIOHandler,Runnable{
	PacketLine pipeline = null;
	PacketLine waitWritePipeLine = null;
//	private LinkedList<SelectionKey> sendqueue = new LinkedList<SelectionKey>();
//	private LinkedList<SelectionKey> receivequeue = new LinkedList<SelectionKey>();
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
		pipeline = new PacketLine();
		waitWritePipeLine = new PacketLine();
	}
	@Override
	public void processConnect() throws IOException {
		// it have been processed by selectsocket ;

	}

	@Override
	public synchronized void processRead() throws IOException {
		System.out.println("read a packet");
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
			System.out.println("received a packet from"+channel.socket().getInetAddress().getHostName());
			if(argsize > 0)
				p = new packet(clientType, command, args);
			else
				p = new packet(clientType, command);
				//ServerUtil.attach(inprocesskey, p);
				pipeline.addNode(inprocesskey, p);
				System.out.println("add a node to pipeline");
				//manager.addNeedProcessKey(inprocesskey);
				manager.addReadInterest(inprocesskey);
				argsize = 0; 
		}

	//}

	@Override
	public synchronized void processWrite() throws IOException {
	//	while((inprocesskey = manager.popNeedWriteKey()) != null){
			//System.out.println("write a packet");
			while(waitWritePipeLine.hasNext()){
			segment se = waitWritePipeLine.popNode();
			if(se !=null){
			SelectionKey sk = se.key;
			packet p = se.p;
			SocketChannel channel = (SocketChannel) sk.channel();
			ByteBuffer buffer = p.getBuffer();
			if(buffer!=null){
			if(channel.write((ByteBuffer) buffer.flip()) < buffer.capacity()){
				ml.warn("server send little bytes than expected!");
			}
			System.out.println("write a segment to client");
			}
			manager.addReadInterest(inprocesskey);
			}
			}
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
			try {
				processWrite();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			inprocesskey = manager.popKey();
			if (inprocesskey == null) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			try {
				if (inprocesskey.isReadable())
				processRead();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public segment getNewSegement() {
		return pipeline.popNode();
	}
	public void pushWriteSegement(SelectionKey key,packet p){
		waitWritePipeLine.addNode(key, p);
	}
}
