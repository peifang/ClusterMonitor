package com.intel.fangpei.for_test_code;

import java.nio.ByteBuffer;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.network.NIONodeHandler;
import com.intel.fangpei.network.NIOServerHandler;
import com.intel.fangpei.network.PacketLine.segment;
import com.intel.fangpei.util.ClientUtil;
import com.intel.fangpei.util.Line.Node;
import com.intel.fangpei.util.ServerUtil;

public class test {

	public static void main(String args[]) {
		DisLog ping= new DisLog();
		DisLogObj o1 = ping.StartServer("56784");
		DisLogObj o2 = ping.StartClient("127.0.0.1", 56784, null, 3);
		o2.send(new packet(BasicMessage.NODE,BasicMessage.OP_MESSAGE,"hello world!"), 4);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println(new String(o1.receive().p.getArgs()));
		NIOServerHandler server2 = ServerUtil.startServerHandler("457");
		NIONodeHandler node2 = ClientUtil.startNodeThread("127.0.0.1", 457);
		node2.addSendPacket(new packet(BasicMessage.NODE,BasicMessage.OP_MESSAGE,"hello world!"));
		node2.addSendPacket(new packet(BasicMessage.NODE,BasicMessage.OP_MESSAGE,"hello world2!"));
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ClientUtil.print(System.getenv());
		segment s = server2.getNewSegement();
		
		System.out.println("received:"+new String(s.p.getArgs()));
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		segment s2 = server2.getNewSegement();
		System.out.println("received:"+new String(s2.p.getArgs()));
		ByteBuffer bb = ByteBuffer.allocate(1024);
		bb.putInt(123);
		System.out.println(bb.mark() + " " + bb.limit() + " " + bb.remaining());
		bb.putInt(56);
		System.out.println(bb.mark() + " " + bb.limit() + " " + bb.remaining());
		bb.put((byte) 1);
		System.out.println(bb.mark() + " " + bb.limit() + " " + bb.remaining());
		bb.flip();
		System.out.println(bb.mark() + " " + bb.limit() + " " + bb.remaining());

		
		System.out.println(bb.getInt());
		System.out.println(bb.mark() + " " + bb.limit() + " " + bb.remaining());
		System.out.println(bb.getInt());
		//byte[] b = new byte[bb.remaining()];
		//bb.get(b, 0, bb.remaining());
		bb.rewind();
		//bb.put(b);
		System.out.println(bb.mark() + " " + bb.limit() + " " + bb.remaining()
				+ " " + bb.arrayOffset());
		bb.putInt(123);
		System.out.println(bb.mark() + " " + bb.limit() + " " + bb.remaining()
				+ " " + bb.arrayOffset());
	}

}
