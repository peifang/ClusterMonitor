package com.intel.fangpei.terminalmanager;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.network.NIOServerHandler;
import com.intel.fangpei.network.SelectionKeyManager;

public abstract class SlaveManager {
	MonitorLog ml = null;
	SelectionKey key = null;
	SelectionKeyManager keymanager = null;
	ByteBuffer buffer = null;
	int version = 0;
	int argsize = 0;
	byte clientType = 0;
	byte command = 0;
	byte[] args = null;
	NIOServerHandler nioserverhandler = null;
	public SlaveManager(SelectionKeyManager keymanager,NIOServerHandler nioserverhandler) {
		try {
			ml = new MonitorLog();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.keymanager = keymanager;
		this.nioserverhandler = nioserverhandler;
	}

	public abstract boolean Handle(SelectionKey key,packet p);

	protected void unpacket() {
		if(buffer == null){
			return;
		}
		buffer.flip();
		version = buffer.getInt();
		argsize = buffer.getInt();
		clientType = buffer.get();
		command = buffer.get();
		System.out.println("the packet is:"+argsize+":"+buffer.remaining());
		if (argsize != 0) {
			args = new byte[buffer.remaining()];
			buffer.get(args);
		}
	}

}
