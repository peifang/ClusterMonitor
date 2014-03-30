package com.intel.fangpei.terminalmanager;

import java.nio.channels.SelectionKey;

import org.apache.hadoop.hbase.util.Bytes;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.network.NIOServerHandler;
import com.intel.fangpei.network.SelectionKeyManager;
import com.intel.fangpei.util.SystemUtil;
/**
 * server use this class to communicate with Node.
 * @author fangpei
 *
 */
public class ClientManager extends SlaveManager{
	public ClientManager(SelectionKeyManager keymanager,NIOServerHandler nioserverhandler) {
		super(keymanager,nioserverhandler);
	}

	public boolean Handle(SelectionKey key,packet p){
		System.out.println("[ClientManager]handle this request");
		this.key = key;
		buffer = p.getBuffer();
		System.out.println("[ClientManager]this client packet is:"+SystemUtil.byteToString(p.getArgs()));
			unpacket();
			if (clientType == BasicMessage.ADMIN) {
				if (command == BasicMessage.OP_QUIT) {
					keymanager.setAdmin(null);
					key.cancel();
					return false;
				}
				if (command == BasicMessage.OP_LOGIN) {
					packet p2 = new packet(BasicMessage.SERVER,BasicMessage.OP_MESSAGE,Bytes.toBytes("[message]admin"));
					nioserverhandler.pushWriteSegement(key, p2);
					keymanager.setAdmin(key);
					ml.log("New admin login!");
				}
			} else if (clientType == BasicMessage.NODE) {
				SelectionKey admin = keymanager.getAdmin();
				if (command == BasicMessage.OP_QUIT) {
					keymanager.deletenode(key);
					return false;
				}
				if (command == BasicMessage.OP_LOGIN) {
					packet p2 = new packet(BasicMessage.SERVER,BasicMessage.OK,Bytes.toBytes(" You have registered as a new Node"));
					nioserverhandler.pushWriteSegement(key, p2);
					keymanager.addnode(key);
				}
				if(command ==BasicMessage.OP_SYSINFO){
					nioserverhandler.pushWriteSegement(admin, new packet(buffer));
					//HashMap<String,String> hm = SysInfo.deserialize(args);
					//System.out.println(hm.get("CPU_Vendor"));
					//System.out.println(hm.get("Disk_info"));
					//System.out.print(hm.get("RegUser"));
					return true;
				}
				if(command ==BasicMessage.OP_MESSAGE){
					System.out.println("[ClientManager]add NIO write interest for Admin");
					nioserverhandler.pushWriteSegement(admin,  new packet(buffer));
					return true;
				}
			}
			if (!operate()) {
				return false;
			}
		return true;
	}

	private boolean operate() {
		System.out.println("normal process");
		return true;
	}

}
