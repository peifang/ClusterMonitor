package com.intel.fangpei.network;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.LinkedList;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.network.PacketLine.segment;

public class NIONodeHandler extends NIOHandler {
	private static LinkedList<packet> processRequestSendqueue = new LinkedList<packet>();
	public NIONodeHandler(String ip, int port) {
		super(ip, port);
	}

	@Override
	public void processError(Exception e) {
		// TODO Auto-generated method stub

	}
public synchronized static void processRequest(packet p){
	processRequestSendqueue.add(p);
}
	@Override
	public void run() {
		try {
			ml.log("start to connect server...");
			processConnect();
			packet one = new packet(BasicMessage.NODE, BasicMessage.OP_LOGIN);
			addSendPacket(one);
			ml.log("connected!");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			ml.error("connect server failed! please check the server's status");
			ml.error(e1.getMessage());
			System.exit(1);
		}
		while (true) {
			while(!processRequestSendqueue.isEmpty()){
				addSendPacket(processRequestSendqueue.pop());
			}
			try {
				processRead();
				processWrite();
				if (!isEmpty()) {
					Thread.sleep(100);
					continue;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				ml.error(e.getMessage());
				ml.error("exit...");
				System.exit(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				ml.error(e.getMessage());
				ml.error("exit...");
				System.exit(1);
			}

		}

	}

}
