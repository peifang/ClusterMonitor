package com.intel.fangpei.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;

public class ServerUtil {

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
		String sb = "";
		long timeout = 5000;
		long now = System.currentTimeMillis();
		while (!sb.trim().endsWith("#!")) {
			if (System.currentTimeMillis() - now > timeout) {
				// System.out.println(sb.toString());
				// System.out.println("Address "+(channel.socket().getInetAddress().getHostAddress()+"read timeout,cancel!"));
				return 0;
			}
			try {
				channel.read(buffer);

				sb = new String(buffer.array());
			} catch (IOException e) {
				System.out
						.println("Address "
								+ (channel.socket().getInetAddress()
										.getHostAddress() + " connection fails!"));
				return -1;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int as = buffer.get(buffer.position());
		// buffer.position(buffer.position() - 2);
		System.out.println(as + " " + buffer.mark());
		return 1;
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
}
