package com.intel.fangpei.BasicMessage;
/*
 * unuse class
 */
public abstract class UserMessage {
String userCommand = null;
public UserMessage(){
	this.userCommand = userCommand();
	if(userCommand == null){
		return ;
	}
}
public byte getCommand(){
	return   99;
}
public abstract String userCommand();
}
