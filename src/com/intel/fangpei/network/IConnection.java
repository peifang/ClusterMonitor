package com.intel.fangpei.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import com.intel.fangpei.BasicMessage.packet;

public interface IConnection {
	/**
	 * 添加一个包到发送队列
	 * 
	 * @param out
	 *            OutPacket子类
	 */
	public void addSendPacket(packet out);

	public void clearSendQueue();

	public String getId();

	public void dispose();

	public InetSocketAddress getRemoteAddress();

	public SelectableChannel channel();

	public INIOHandler getNIOHandler();

	public boolean isEmpty();

	public int receive() throws IOException;

	public void send(ByteBuffer buffer);

	public boolean isConnected();
}
