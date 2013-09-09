package com.intel.fangpei.terminal;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.ArrayUtils;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.FileSystem.Directory;
import com.intel.fangpei.SystemInfoCollector.SysInfo;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.network.NIONodeHandler;
import com.intel.fangpei.task.DiskGeneDataTask;
import com.intel.fangpei.task.DiskPutDataTask;
import com.intel.fangpei.task.ExtendTask;
import com.intel.fangpei.task.HbaseGeneDataTask;
import com.intel.fangpei.task.HbasePutDataTask;
import com.intel.fangpei.util.ConfManager;
import com.intel.fangpei.util.HbaseUtil;

public class Node extends Client {
	MonitorLog ml = null;
	SysInfo si = null;
	String serverip = "";
	int port = 0;

	Node(String serverip, int port) {
		this.serverip = serverip;
		this.port = port;
		this.connect = new NIONodeHandler(serverip, port);
		try {
			ml = new MonitorLog();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		si = SysInfo.GetSysHandler();
	}

	@Override
	public void run() {
		try {
			new Thread(connect).start();
			packet p = null;
			while (true) {
				if (!connect.isEmpty()) {
					p = connect.getReceivePacket();
					if (!Do(p)) {
						System.exit(0);
					}
				} else {
					Thread.sleep(100);
				}
			}
		} catch (Exception e) {
		}
	}

	public static void main(String[] args) {
		ConfManager.addResource(null);
		String ip = ConfManager.getConf("selectsocket.server.ip");
		if(ip == null){
			System.out.println("not config ip ...exit...");
			System.exit(0);
		}
		int port = ConfManager.getInt("selectsocket.server.port", 1234);
		for (int i = 0; i < 1; i++) {
			Node c = new Node(ip, port);
			new Thread(c).start();
		}
	}

	public boolean Do(packet message) throws IOException {
		String[] args =null;
		if(message.getArgs() != null){
		args = new String(message.getArgs()).split(" ");
		}
		/*
		 * we need to unpack the received packet and check the command !
		 */
		byte opt = message.getCommand();
		switch (opt) {
		case (byte) 9:
			ml.log("messge from server ,[close] the node");
			packet one = new packet(BasicMessage.NODE, BasicMessage.OP_QUIT);
			connect.addSendPacket(one);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		case (byte) 1:
			if(args.length < 2){
				ml.log("no args to exec,return");
				return true;
			}
		if(args.length == 2){
			extendTask("com.intel.developer.extend."+args[1]);
		}else{
			extendTask("com.intel.developer.extend."+args[1],(String[])ArrayUtils.subarray(args, 2, args.length));
		}
			ml.log("complete execute!" + System.currentTimeMillis());
			return true;
		case (byte) 2:
			ml.log(args == null ? "no args!" : new String(message
					.getArgs()));
			return true;
		case (byte) 3:
			ml.log("load data into hbase,command parameter is :"+message.getArgs() == null ? "no args!" : new String(message
					.getArgs()));
			if(!loadData_Hbase(args)){
				ml.error("load data fail!");
				return true;
			}
			return true;
		case (byte) 4:
			ml.log(args == null ? "no args!" : new String(message
					.getArgs()));
			HbaseUtil hbase = new HbaseUtil(args[1], args[2], args[3]);
			hbase.CreateTable((String[]) ArrayUtils.subarray(args, 4,
					args.length));
			return true;
		case (byte)5:
			ml.log("load data into disk,command parameter is :"+args == null ? "no args!" : new String(message
					.getArgs()));
			if(!loadData_Disk(args)){
				ml.error("load data fail!");
				return true;
			}
			return true;
		case (byte)11:
		try {
			si.Refresh();
		} catch (Exception e) {
			ml.log(e.getMessage());
			return false;
		}
	packet p = new packet(BasicMessage.NODE,BasicMessage.OP_SYSINFO,si.GetSysInfoBytes());
	connect.addSendPacket(p);
			return true;
		default:
			return true;
		}
	}
	private boolean extendTask(String  classname){
		ExtendTask et = new ExtendTask(ml,classname);
		new Thread(et).start();
		return true;
	}
	private boolean extendTask(String  classname,String[] args){
		ExtendTask et = new ExtendTask(ml,classname,args);
		new Thread(et).start();
		return true;
	}
	private boolean loadData_Disk(String[] args) {
		String path = args[1];
		File schema = new File(path);
		if(!schema.exists()){
			ml.error("Get a load data command ,but the schema path" +
					" parameter isn't contain any usable schema file");
			return false;	
		}
		long predictGenes = 0;
		try{
			predictGenes = Long.parseLong(args[2]);
		}catch (Exception e){
	ml.error("Get a load data command ,but the pridictgenes" +
			" parameter is not a int or long type");
			return false;
		}
		Directory d = new Directory();
		d.initDir();
		DiskGeneDataTask g = new DiskGeneDataTask(ml,schema, predictGenes);
		DiskPutDataTask p = new DiskPutDataTask(ml,g, predictGenes,d);
		Thread a = new Thread(g);
		Thread b = new Thread(p);
		a.start();
		b.start();
		return true;
	}
	private boolean loadData_Hbase(String[] args) {		
		String path = args[1];
		File schema = new File(path);
		if(!schema.exists()){
			ml.error("Get a load data command ,but the schema path" +
					" parameter isn't contain any usable schema file");
			return false;	
		}
		long predictGenes = 0;
		try{
			predictGenes = Long.parseLong(args[2]);
		}catch (Exception e){
	ml.error("Get a load data command ,but the pridictgenes" +
			" parameter is not a int or long type");
			return false;
		}
		HbaseGeneDataTask g = new HbaseGeneDataTask(ml,schema, predictGenes);
		HbasePutDataTask p = new HbasePutDataTask(ml,g, predictGenes,null, null, null, null);
		Thread a = new Thread(g);
		Thread b = new Thread(p);
		a.start();
		b.start();
		return true;
	}
}
