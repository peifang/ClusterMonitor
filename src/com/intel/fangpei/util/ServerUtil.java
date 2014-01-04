package com.intel.fangpei.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.apache.commons.logging.Log;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.network.NIOProcess;
import com.intel.fangpei.network.NIOServerHandler;
import com.intel.fangpei.terminal.SelectSocket;

public class ServerUtil {
public static NIOServerHandler startServerHandler(String port){
	SelectSocket ss = new SelectSocket();
	return ss.startAsCommonServer(port);
}
	public static int SendToClient(SocketChannel channel, ByteBuffer buffer) {
		buffer.flip();
		int len = 0;
		try {
			len = channel.write(buffer);
		} catch (IOException e) {
			System.out.println(channel.toString() + ":connection fails!");
			return -1;
		}
		return len;
	}

	public static int SendToClientWithTimeout(SocketChannel channel,
			ByteBuffer buffer) {
		buffer.flip();
		int a = 0;
		int len = buffer.remaining();
		long timeout = 5000;
		long now = System.currentTimeMillis();
		while (len > a) {
			try {
				a += channel.write(buffer);
			} catch (IOException e) {
				System.out.println(channel.toString() + ":connection fails!");
				return -1;
			}
			if (System.currentTimeMillis() - now > timeout) {
				System.out.println("server to client"
						+ channel.socket().getInetAddress().getHostAddress()
						+ ":send timeout! ");
				return -1;
			}
		}
		return len;
	}

	public static int Receive(SocketChannel channel, ByteBuffer buffer) {
		int bytereads = 0;
		try {
			bytereads = channel.read(buffer);
		} catch (IOException e) {
			System.out
					.println("Address "
							+ (channel.socket().getInetAddress()
									.getHostAddress() + " connection fails!"));
			return -1;
		}
		return bytereads;
	}

	public static int ReceiveWithTimeout(SocketChannel channel,
			ByteBuffer buffer) {
		int reads = 0;
		TimeCounter tc = new TimeCounter(5000);
		while(!tc.isTimeout()){
			try {
				reads += channel.read(buffer);
				if(reads == buffer.capacity()){
					return reads;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return reads;
	}

	public static int SendToClient(SocketChannel sc, packet p) {
		return SendToClientWithTimeout(sc, p.getBuffer());
	}

	public static int SendToClient(SocketChannel sc, byte command, byte[] args) {
		packet one = new packet(BasicMessage.SERVER, command, args);
		return SendToClientWithTimeout(sc, one.getBuffer());

	}

	public static int SendToClient(SocketChannel sc, byte command) {
		packet one = new packet(BasicMessage.SERVER, command);
		return SendToClientWithTimeout(sc, one.getBuffer());
	}
	public static void attach(SelectionKey  key , packet attach){
		if(key.attachment() == null){
			key.attach(attach.getBuffer());
		}else{
			/*
			 * maybe the attachment is mutil-packets;
			 * a bug is here!the attachments maybe lose if 
			 * another atatchement is coming but this one have
			 * noe been processed.
			 */
			ByteBuffer front = (ByteBuffer) key.attachment();
			ByteBuffer now = attach.getBuffer();
			ByteBuffer buffer = ByteBuffer.allocate(front.capacity()+now.capacity());
			buffer.put((ByteBuffer) front.flip());
			buffer.put((ByteBuffer) now.flip());
			key.attach(buffer);
		}
	}
}
