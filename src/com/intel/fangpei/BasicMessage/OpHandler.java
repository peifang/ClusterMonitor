package com.intel.fangpei.BasicMessage;

import com.intel.fangpei.BasicMessage.packet;

public abstract class OpHandler {
	private byte op = 0;
	public OpHandler(byte op){
		this.setOp(op);
	}
public abstract void Action(packet p);
public byte getOp(){
	return op;
}
public void setOp(byte op){
	this.op = op;
}
public void closeHandler() {
	// TODO Auto-generated method stub
	
}
}
