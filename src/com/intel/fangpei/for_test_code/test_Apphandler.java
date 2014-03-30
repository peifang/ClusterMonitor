package com.intel.fangpei.for_test_code;

import com.intel.fangpei.BasicMessage.AppHandler;
import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.OpHandler;
import com.intel.fangpei.BasicMessage.packet;

public class test_Apphandler{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		OpHandler handler1= new OpHandler(BasicMessage.OP_QUIT){
				@Override
				public void Action(packet p) {
					System.out.println("hello 1");
				}
	};
	OpHandler handler2= new OpHandler(BasicMessage.OK){
		@Override
		public void Action(packet p) {
			System.out.println("hello 2");
		}
};
AppHandler handler = new AppHandler(0);
handler.register(handler2);
handler.register(handler1);
handler.exec(new packet(BasicMessage.ADMIN,BasicMessage.OK));
handler.exec(new packet(BasicMessage.ADMIN,BasicMessage.OP_QUIT));
}
}
