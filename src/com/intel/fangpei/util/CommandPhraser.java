package com.intel.fangpei.util;

import java.io.IOException;

import com.intel.fangpei.BasicMessage.BasicMessage;
import com.intel.fangpei.BasicMessage.ServiceMessage;

public class CommandPhraser {

	public CommandPhraser() {
		// TODO Auto-generated constructor stub
	}

	public static byte GetUserInputCommand(String command) throws IOException {
		String[] s = command.split(" ");
		if (s[0].equals("?")||s[0].equalsIgnoreCase("help")){
			return BasicMessage.OP_HELP;
		}
		if (s[0].equals("exec")) {
			return BasicMessage.OP_EXEC;
		}
		if (s[0].equals("close")) {
			return BasicMessage.OP_CLOSE;
		}
		if (s[0].equals("quit")) {
			return BasicMessage.OP_QUIT;
		}
		if (s[0].equals("progress")) {

		}
		if(s[0].equals("sysinfo")){
			return BasicMessage.OP_SYSINFO;
		}
		if(s[0].equals("sh")){
			return BasicMessage.OP_SH;
		}
		if(s[0].equals("thread")){
			return ServiceMessage.THREAD;
		}
		if(s[0].equals("service")){
			return ServiceMessage.SERVICE;
		}
		return (byte) -1;
	}

}
