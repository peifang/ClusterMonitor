package com.intel.fangpei.network;

import java.io.IOException;
import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;

public class NIONodeHandler extends NIOHandler {
	public NIONodeHandler(String ip, int port) {
		super(ip, port);
	}

	@Override
	public void processError(Exception e) {
		// TODO Auto-generated method stub

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
