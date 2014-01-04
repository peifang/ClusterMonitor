package com.intel.fangpei.task;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import com.ali.fangpei.service.wrapWork;
import com.intel.fangpei.BasicMessage.packet;
import com.intel.fangpei.process.ProcessFactory;
import com.intel.fangpei.process.ProcessManager;
import com.intel.fangpei.task.TaskRunner.ChildRunner;
import com.intel.fangpei.util.ServerUtil;
import com.intel.fangpei.util.SystemUtil;
/***
 * @deprecated
 * @author fangpei.fp
 *
 */
public class ServiceMonitor extends Thread{
	SocketChannel channel = null;
	boolean isRunning = false;
	public void send(wrapWork cr){
		ByteBuffer buffer = cr.toTransfer().getBuffer();
		System.out.println("serviceMonitor:"+buffer);
		if(channel == null){
			System.out.println("channel is none!");
			return;
		}
		buffer.flip();
		while (buffer.hasRemaining()) {
			try {
				channel.write(buffer);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	public void run(){
		Selector selector = null;
		try{
			selector = Selector.open();
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		ServerSocket serverSocket = serverChannel.socket();
		serverSocket.bind(new InetSocketAddress(4398));
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		}catch(Exception e){
			
		}
		/***
		 * for test ,only support win now!
		 */		
		if(SystemUtil.operationType().startsWith("Win")){
			int procid = ProcessFactory.buildNewProcessWithProcessid("java","-cp","../cluster.jar","-Djava.ext.dirs=../tools/lib","com.intel.fangpei.process.ServiceDemo","127.0.0.1","4398");	
			ProcessManager.start(procid);
		}
		isRunning = true;
		while(isRunning){
			int n = 0;
			try {
				n = selector.select(100);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (n == 0) {
				continue;
			}
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while (it.hasNext()) {
				SocketChannel channel = null;
				SelectionKey key = it.next();
				if(!key.isValid()){
					it.remove();
					continue;
				}
				if (key.isAcceptable()) {
					ServerSocketChannel server = (ServerSocketChannel) key
							.channel();
					try {
						channel = server.accept();
						System.out.println("accept a new jvm connection");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (channel == null) {
						continue;
					}
					try {
						channel.configureBlocking(false);
						channel.register(selector, SelectionKey.OP_READ);
						ByteBuffer buffer = ByteBuffer.allocate(7+Integer.SIZE/8);
						buffer.clear();
						if(ServerUtil.ReceiveWithTimeout(channel, buffer) <= 0){
							System.out.println("when accept the jvm connect,we cann't get" +
									"the procid it sends,throw the jvm");
							continue;
						}
						buffer.flip();
						System.out.println("get the serviceDemo !");
						this.channel = channel;
					} catch (IOException e) {
					}
					it.remove();
					continue;
				}
				it.remove();
				key.interestOps(key.interestOps() & (~key.readyOps()));
		}
	}
		try {
			selector.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return isRunning;
	}
	/***
	 * this method will smoothly stop the service
	 * server .
	 * But if any other new Service or Thread work 
	 * come ,the Service Monitor will Start again.
	 */
	public void Stop(){
		isRunning = false;
	}

}
