package com.intel.fangpei.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.intel.fangpei.BasicMessage.packet;

public class ClientUtil {
	public static final int Admin = 0;
	public static final int Node = 1;

	public ClientUtil() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * This function is for Client to send data to Server with the defined
	 * Socket Channel.If the send fails in the timeout time ,it will return
	 * false, else return true. packet components:
	 */

	public static boolean sendToServer(SocketChannel sc, packet one) {
		int d = 0;
		ByteBuffer bb = one.getBuffer();
		bb.flip();
		int len = bb.remaining();
		long timeout = 5000;
		long now = System.currentTimeMillis();
		while (d < len) {
			try {
				d += sc.write(bb);
			} catch (IOException e) {
				System.out.println("Connection broken!");
				return false;
			}
			if (System.currentTimeMillis() - now > timeout) {
				System.out.println("client send to server timeout,cancel!");
				return false;
			}
		}
		return true;
	}

	/*
	 * client read data from server This function will block until it reads some
	 * data or the Channel is fail
	 */
	public static ByteBuffer ClientReadWithBlock(SocketChannel sc) {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer.clear();
		try {
			while (sc.read(buffer) < 1)
				;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buffer;

	}

	/*
	 * client read data from server you can set the timeout with this function
	 */
	public static String ClientReadWithWait(SocketChannel sc) {
		StringBuilder sb = new StringBuilder();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer.clear();
		long timeout = 5000;
		long now = System.currentTimeMillis();
		while (!sb.toString().trim().endsWith("#!")) {
			try {
				sc.read(buffer);
				sb.append(new String(buffer.array()));
				buffer.clear();
			} catch (IOException e) {
				return null;
			}
			if (System.currentTimeMillis() - now > timeout) {
				System.out.println(sb.toString());
				System.out.println("client read timeout,cancel!");
				return null;
			}
		}
		return sb.toString().trim();

	}

	/*
	 * client read data from server this function will return immediately when
	 * ever it read or not read some data.when the data it read is empty or the
	 * channel is not ready ,it will return null ,else return the String.
	 */
	public static String ClientReadNoWait(SocketChannel sc) {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer.clear();
		try {
			sc.read(buffer);
		} catch (IOException e) {
			return null;
		}
		String s = new String(buffer.array()).trim();
		if (s.equals(""))
			return null;
		else
			return s;
	}

}
