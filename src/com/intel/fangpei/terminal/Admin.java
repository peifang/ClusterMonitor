package com.intel.fangpei.terminal;

import java.nio.ByteBuffer;

import org.apache.hadoop.hbase.util.Bytes;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.network.NIOAdminHandler;
import com.intel.fangpei.util.CommandPhraser;
import com.intel.fangpei.util.ConfManager;
import com.intel.fangpei.util.HbaseUtil;

public class Admin extends Client {
	public static boolean debug = false;
	/*
	 * buffer to buffer the packet of command packet components: [Client Type]
	 * byte [version] int [arg size] int [command] byte [args...] byte[]
	 */
	ByteBuffer buffer = ByteBuffer.allocate(1024);
	String serverip = "";
	int port = 0;

	public Admin(String serverip, int port) {
		this.serverip = serverip;
		this.port = port;
		this.connect = new NIOAdminHandler(serverip, port);
	}

	@Override
	public void run() {
		try {
			new Thread(connect).start();
			while (true) {
				byte[] b = new byte[1024];
				System.out.print("--->]:");
				System.in.read(b);
				DoCommand(new String(b).trim());
				b = new byte[1024];
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// maybe some command need admin to receive server's request.
	private String DoCommand(String command) throws Exception {
		String[] s = null;
		packet one = null;
		byte COMMAND = CommandPhraser.GetUserInputCommand(command);
		if(COMMAND == BasicMessage.OP_HELP){
			printHelp();
		}
		if (COMMAND == BasicMessage.OP_HTABLE_CREATE) {
			s = HbaseUtil.GetUserInputData_CreateTable();
			one = new packet(BasicMessage.ADMIN, COMMAND,
					Bytes.toBytes(formString(s)));

		}
		if (COMMAND == BasicMessage.OP_lOAD_HBASE) {
			if(command.split(" ").length !=3){
			System.out
					.println("wrong parameter numbers for put data into HBase" +
							"\n\t[usage]put [schema path] [num]");
			return "";
			}
			one = new packet(BasicMessage.ADMIN, COMMAND,
					Bytes.toBytes(command.substring(command.indexOf(" "),
							command.length())));
		}
		if (COMMAND == BasicMessage.OP_lOAD_DISK) {
			if(command.split(" ").length !=3){
			System.out
					.println("wrong parameter numbers for put data into disk" +
							"\n\t[usage]file [schema path] [num]");
			return "";
			}
			one = new packet(BasicMessage.ADMIN, COMMAND,
					Bytes.toBytes(command.substring(command.indexOf(" "),
							command.length())));
		}
		if (COMMAND == BasicMessage.OP_EXEC) {
			try{
			one = new packet(BasicMessage.ADMIN, COMMAND,
					Bytes.toBytes(command.substring(command.indexOf(" "),
							command.length())));
			}catch(IndexOutOfBoundsException e){
				System.out.println("exec [classname]");
				return "";
			}
		}
		if(COMMAND == BasicMessage.OP_SYSINFO){
		one = new packet(BasicMessage.ADMIN,COMMAND);
		}
		if (COMMAND == BasicMessage.OP_CLOSE) {
		one = new packet(BasicMessage.ADMIN, COMMAND);
		}
		if (COMMAND == BasicMessage.OP_QUIT) {
		one = new packet(BasicMessage.ADMIN, COMMAND);
		}
		if (COMMAND == BasicMessage.OP_SH) {
			one = new packet(BasicMessage.ADMIN, COMMAND,
					Bytes.toBytes(command.substring(command.indexOf(" "),
							command.length())));
		}
		if(one == null)
			return "";
		else
		connect.addSendPacket(one);
		return "";// need to return the server's response!
	}

	private String formString(String[] s) {
		StringBuilder sb = new StringBuilder();
		int len = s.length;
		for (int i = 0; i < len; i++) {
			sb.append(" " + s[i]);
		}
		return sb.toString();
	}
	public static void printHelp(){
		System.out.println("the command line Admin Usage:");
		System.out.println("exec          execute a class which extend ExtendHandler");
		System.out.println("              idh");
		System.out.println("                    start|stop  hdfs|hbase|zookeeper|mapreduce|all");
		System.out.println("                    status");
		System.out.println("file          Distributed generate data and load it on the disk ");
		System.out.println("progress      [not complete command,soon...]");
		System.out.println("put           Distributed generate data and load it on HBase");
		
		System.out.println("create htable create htable with some options");
		System.out.println("close         close the cluster's nodes demon");
		System.out.println("quit          close the admin process");
		System.out.println("sysinfo       get the cluster's system info");
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
			Admin c = new Admin(ip, port);
			new Thread(c).start();
		}
	}
}

/*
 * 
 */
/*
 * socket1 = new Socket("127.0.0.1",1234); OutputStream outStream =
 * socket1.getOutputStream(); InputStream inputStream =
 * socket1.getInputStream(); o = new ObjectOutputStream(outStream); i = new
 * ObjectInputStream(inputStream);
 * 
 * // ÒµÎñÂß¼­ Date date = new Date(); SimpleDateFormat bartDateFormat = new
 * SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.sss"); String starttime =
 * bartDateFormat.format(date); TimeCounter timerTotal = new
 * TimeCounter("ThreadTotalTime"); try { Class.forName(driveName);
 * 
 * } catch (ClassNotFoundException e) { System.out.println("classnofound");
 * System.exit(1); } Statement st; Random random = new Random(); String sql
 * ="select * from extern limit 1"; try { Connection con =
 * DriverManager.getConnection( "jdbc:hive://"+ip+":10000/default", "", ""); st
 * = con.createStatement(); timerTotal.beginAndEnter(); st.executeQuery(sql);
 * timerTotal.leaveAndEnd(); } catch (SQLException e) { // TODO Auto-generated
 * catch block e.printStackTrace(); } long t = timerTotal.getCounter();
 * averagetime+=t; enroll roll =new enroll(sql,""+averagetime);
 * o.writeObject(roll); o.flush(); finally{ try{ o.close(); socket1.close();
 * }catch (IOException e){ e.printStackTrace(); } }
 */

