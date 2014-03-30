package com.intel.fangpei.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.network.PacketLine.segment;
import com.intel.fangpei.util.ConfManager;
import com.intel.fangpei.util.Line;
import com.intel.fangpei.util.ServerUtil;
import com.intel.fangpei.util.SystemUtil;
import com.intel.fangpei.util.TimeCounter;
/**
 * Start as common server ,the args contains port.
 * <p>this class also proccess server connection.It uses SelectionKeyManager to manager it's keys.</p>
 */
public class NIOServerHandler implements INIOHandler,Runnable{
	PacketLine pipeline = null;
	PacketLine waitWritePipeLine = null;
	Selector selector = null;
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
	private int port = 0;
	ServerSocketChannel serverchannel = null;
	public NIOServerHandler(int port,MonitorLog ml,SelectionKeyManager manager){
		this.port = port;
		ConfManager.addResource(null);
		this.manager  = manager;
		this.ml = ml;
		buffer = ByteBuffer.allocate(1024*1024*4);
		buffer.clear();
		tc = new TimeCounter(10000);
		pipeline = new PacketLine();
		waitWritePipeLine = new PacketLine();
	}
	public NIOServerHandler(int port,MonitorLog ml){
		this.port = port;
		ConfManager.addResource(null);
		this.manager = new SelectionKeyManager();
		if(ml!=null){
		this.ml = ml;
		}else{
			try {
				ml = new MonitorLog();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
		//System.out.println("read a packet");
		//while((inprocesskey = manager.popNeedReadKey()) != null){
			SocketChannel channel = (SocketChannel) inprocesskey.channel();
			try{
			if(receive(channel) <= 0){
				System.out.println("read node from "
						+ channel.socket().getInetAddress().getHostAddress()
						+ " fail,exclude the node!");
				manager.addCancelInterest(inprocesskey);
				deletenodeAction(inprocesskey);
				return;
			}
			}catch(IOException e){
				System.out.println("IO Exception:read node from "
						+ channel.socket().getInetAddress().getHostAddress()
						+ " fail,exclude the node!");
				manager.addCancelInterest(inprocesskey);
				deletenodeAction(inprocesskey);
				return;
			}
			//System.out.println("received a packet from"+channel.socket().getInetAddress().getHostName());
			if(argsize > 0)
				p = new packet(clientType, command, args);
			else
				p = new packet(clientType, command);
				//ServerUtil.attach(inprocesskey, p);
				pipeline.addNode(inprocesskey, p);
				//System.out.println("add a node to pipeline");
				//manager.addNeedProcessKey(inprocesskey);
				manager.addReadInterest(inprocesskey);
				argsize = 0; 
		}

	//}

	@Override
	public synchronized void processWrite() throws IOException {
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
			System.out.println("[NIOServerHandler]write a segment to client:"+SystemUtil.byteToString(p.getArgs()));
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
	/**
	 * remove all data want to send to this key
	 * @param key
	 */
	public void removeWriteKey(SelectionKey key){
		waitWritePipeLine.removeNode(key);
	}
	private int receive(SocketChannel channel) throws IOException {
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
	public void init(){
		System.out.println("start NIOServerHandler");
		Thread t = new Thread(){
			public void run(){
				while(true)
					synchronized(waitWritePipeLine){
						try {
							processWrite();
						} catch (IOException e) {
							e.printStackTrace();
						}
						waitWritePipeLine.notifyAll();
					}
			}
		};
		t.setDaemon(true);
		t.start();
	}
	@Override
	public void run() {
		init();
		//1.open selector
		try {
			selector = Selector.open();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//2.start server
		try {
			serverchannel = startServer(port, selector);
		} catch (ClosedChannelException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		int signals = 0;
		//3.check interest and accept channel;
		while (true) {
			CheckInterest();
			try {
				signals = selector.select(100);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (signals == 0) {
				continue;
			}
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			SocketChannel channel = null;
			while (it.hasNext()) {
				SelectionKey key = it.next();
				if(!key.isValid()){
					it.remove();
					continue;
				}
				if (key.isValid()&&key.isAcceptable()) {
					System.out.println("[NIOServerHandler]accept a connection");
					try {
						channel = serverchannel.accept();
						ml.log("accept a new connection from "
								+ channel.socket().getInetAddress()
										.getHostAddress());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					SelectionKey thiskey = registerChannel(selector, channel, SelectionKey.OP_READ);
					it.remove();
					//key.interestOps(key.interestOps() & (~key.readyOps()));
					//pushWriteSegement(thiskey,new packet(BasicMessage.NODE,BasicMessage.OP_MESSAGE,"hello world11!"));
					//pushWriteSegement(thiskey,new packet(BasicMessage.NODE,BasicMessage.OP_MESSAGE,"hello world22!"));
					continue;
				}
				it.remove();
                // The key indexes into the selector so you  
                // can retrieve the socket that's ready for I/O  
                execute(key); 
			}

		}
	}
	public void DataRecvAction(SelectionKey key) {
		
	}
	public void deletenodeAction(SelectionKey key) {
		
		
	}
	private void execute(SelectionKey key) {
		if(key.isReadable()){
			inprocesskey = key;
			try {
				processRead();
			} catch (IOException e) {
				e.printStackTrace();
			}
			DataRecvAction(key);
		}
	}
	private SelectionKey registerChannel(Selector selector, SocketChannel channel,
			int opRead) {

		if (channel == null) {
			return null;
		}
		try {
			channel.configureBlocking(false);
			return channel.register(selector, opRead);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}
	/**
	 * check all the registered need read key and need cancel key;
	 */
	private void CheckInterest() {
		while (true) {
			SelectionKey key = manager.popNeedReadKey();
			if (key != null && key.isValid()) {
				key.interestOps(key.interestOps() & (~key.readyOps()));
				key.interestOps(SelectionKey.OP_READ);
			} else {
				break;
			}
		}
		while (true) {
			SelectionKey key = manager.popNeedCancelKey();
			if (key != null) {
				ml.log("add node from "
						+ ((SocketChannel) key.channel()).socket()
								.getInetAddress().getHostAddress()+" to delete node");
				manager.deletenode(key);
				deletenodeAction(key);
				key.cancel();
			} else {
				break;
			}
		}
	}
	private ServerSocketChannel startServer(int port, Selector selector) throws IOException,
	ClosedChannelException {
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		ServerSocket serverSocket = serverChannel.socket();
		serverSocket.bind(new InetSocketAddress(port));
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		return serverChannel;
}
	public segment getNewSegement() {
		return pipeline.popNode();
	}
	public packet getChannelRecv(SelectionKey key){
		//System.out.println("key is :"+key.toString());
		//System.out.println("pipeline is null?"+(pipeline == null));
		return pipeline.popNode(key);
	}
	public void pushWriteSegement(SelectionKey key,packet p){
		synchronized(waitWritePipeLine){
		waitWritePipeLine.addNode(key, p);
		waitWritePipeLine.notifyAll();
		}
	}
	public SelectionKeyManager getNodeList(){
		return manager;
	}
	public void flush(){
		synchronized(waitWritePipeLine){
		try {
			processWrite();
		} catch (IOException e) {
			e.printStackTrace();
		}
		waitWritePipeLine.notifyAll();
		}
	}
	public void close(){
		try {
			selector.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
