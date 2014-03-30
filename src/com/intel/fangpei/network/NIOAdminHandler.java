package com.intel.fangpei.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.SystemInfoCollector.SysInfo;
import com.intel.fangpei.terminal.Admin;
/**
 * <h2>help class</h2>
 * <p>help Admin to send and receive data with server</p>
 * @author Administrator
 *
 */
public class NIOAdminHandler extends NIOHandler {
	public NIOAdminHandler(String ip, int port) {
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
			packet one = new packet(BasicMessage.ADMIN, BasicMessage.OP_LOGIN);
			addSendPacket(one);
			ml.log("connected!");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			ml.error("connect server failed! please check the server's status");
			ml.error(e1.getMessage());
			System.exit(1);
		}
		packet p = null;
		while (true) {
			try {
				processRead();
				processWrite();
				if (!isEmpty()) {
					p = getReceivePacket();
					if(Admin.debug)
					System.out.println("process a packet:"+p.getBuffer());
					processAdminreceived(p);
				} else {
					Thread.sleep(100);
					continue;
				}
				p = null;

			} catch (IOException e) {
				ml.error(e.getMessage());
				ml.error("exit...");
				System.exit(1);
			} catch (InterruptedException e) {
				ml.error(e.getMessage());
				ml.error("exit...");
				System.exit(1);
			}

		}

	}

	private void processAdminreceived(packet p) {
		ByteBuffer bb = p.getBuffer();
		bb.flip();
		int version = bb.getInt();
		int argsize = bb.getInt();
		byte clientType = bb.get();
		byte command = bb.get();
		if(command == BasicMessage.OP_SYSINFO){
			byte[] args = new byte[bb.remaining()];
			bb.get(args);
			System.out.println();
			System.out.println("system info get(in summary):");
			HashMap hm = SysInfo.deserialize(args);
			System.out.println("NetWork_FQDN:"+hm.get("NetWork_FQDN"));
			System.out.println("NetWork_IP:"+hm.get("NetWork_IP"));
		}
		if(command == BasicMessage.OP_MESSAGE){
			byte[] args = new byte[bb.remaining()];
			bb.get(args);
			System.out.println(new String(args));
		}
	}

}
