package com.intel.fangpei.for_test_code;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.network.NIONodeHandler;
import com.intel.fangpei.network.NIOServerHandler;
import com.intel.fangpei.network.PacketLine.segment;
import com.intel.fangpei.util.ClientUtil;
import com.intel.fangpei.util.ServerUtil;

public class DisLogObj {
	Log LOG = null;
boolean isServer = false;
NIONodeHandler client = null;
NIOServerHandler server = null;
String className = null;
int reportLevel = 0;
String ip = null;
SimpleDateFormat DateFormat = new SimpleDateFormat(
		"yyyy/MM/dd HH:mm:ss:SSS");
	Date date = new Date();
	public DisLogObj(String port) {
		server = ServerUtil.startServerHandler(port);
		StackTraceElement stack[] = (new Throwable()).getStackTrace();
		this.className = stack[2].getClassName();
		try {
			ip = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			try {
				ip = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e1) {
				ip = "unknow";
			}
		}
		isServer = true;
	}

	public DisLogObj(String serverip, int port, Log LOG,int reportLevel) {
		client = ClientUtil.startNodeThread(serverip,port);
		StackTraceElement stack[] = (new Throwable()).getStackTrace();
		this.className = stack[2].getClassName();
		try {
			ip = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			try {
				ip = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e1) {
				ip = "unknow";
			}
		}
		
		this.LOG = LOG;
		this.reportLevel = reportLevel;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
/**
 * level comment:
 * <p> 1: info
 * <p> 2: debug
 * <p> 3: warn
 * <p> 4: error
 * <p> 5: fatal
 */
public void send(packet p,int level){
	byte[] b = null;
	date.setTime(System.currentTimeMillis());
	if(!isServer){
		switch(level){
		case 1:	
			b = ArrayUtils.addAll((DateFormat.format(date)+"["+ip+"]["+className+"][info]").getBytes(),p.getArgs());
		break;
		case 2:
			b = ArrayUtils.addAll((DateFormat.format(date)+"["+ip+"]["+className+"][debug]").getBytes(),p.getArgs());
			break;
		case 3:
			b = ArrayUtils.addAll((DateFormat.format(date)+"["+ip+"]["+className+"][warn]").getBytes(),p.getArgs());
			break;
		case 4:
			b = ArrayUtils.addAll((DateFormat.format(date)+"["+ip+"]["+className+"][error]").getBytes(),p.getArgs());
			break;
		case 5:
			b = ArrayUtils.addAll((DateFormat.format(date)+"["+ip+"]["+className+"][fatal]").getBytes(),p.getArgs());
			break;
		}
	}
	if(LOG!=null){
		LOG.info(new String(b));
	}
	if(level > reportLevel){
		client.addSendPacket(new packet(BasicMessage.NODE,BasicMessage.OP_MESSAGE,b));		
		}
	}
public segment receive(){
	if(server != null)
	return server.getNewSegement();
	return null;
}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
