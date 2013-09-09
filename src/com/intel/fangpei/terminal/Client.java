package com.intel.fangpei.terminal;

import java.nio.channels.SocketChannel;
import com.intel.fangpei.network.NIOHandler;
import com.intel.fangpei.util.CommandPhraser;

public abstract class Client implements Runnable {
	NIOHandler connect = null;
	SocketChannel client = null;
	int connectType = -1;
	CommandPhraser cp = null;

	public Client() {
		cp = new CommandPhraser();
	}

	@Override
	public void run() {

	}
}
