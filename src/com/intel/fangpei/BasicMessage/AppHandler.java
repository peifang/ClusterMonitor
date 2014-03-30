package com.intel.fangpei.BasicMessage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.intel.fangpei.BasicMessage.packet;

public class AppHandler{
	private long countAccept = 0;
	private long countReject = 0;
	private int handlerID = 0;
	private HashMap<Byte, OpHandler> handlers = new HashMap<Byte, OpHandler>();
	/**
	 * construction of AppHandler.
	 * @param id you define the handler ID
	 */
	public AppHandler(int id) {
		this.handlerID = id;
	}
	public void register(OpHandler app) {
		handlers.put(app.getOp(), app);
		System.out.println("[AppHandler]register app:"+app.getOp());
	}
	public void unRegister(byte code){
		StopApp(code);
		handlers.remove(code);
		System.out.println("[AppHandler]unregister app code:"+code);
	}
	public void unRegisterAll(){
		StopAllApp();
		handlers.clear();
	}
	private void StopApp(byte code){
		OpHandler op = handlers.get(code);
		if(op == null)
			return;
		op.closeHandler();
	}
	private void StopAllApp() {
		Iterator<OpHandler> i = handlers.values().iterator();
		while(i.hasNext()){
			i.next().closeHandler();
		}
	}

	public int handlerID() {
		return handlerID;
	}
	public void exec(packet p ){
		if(p == null){
			return;
		}
		byte opcode = p.getCommand();
		if (handlers.containsKey(opcode)) {
			OpHandler op = handlers.get(opcode);
			op.Action(p);
		} else {
			System.out.println("command no support");
		}
	}
}
