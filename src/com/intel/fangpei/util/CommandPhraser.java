package com.intel.fangpei.util;

import java.io.IOException;

import com.intel.fangpei.BasicMessage.BasicMessage;

public class CommandPhraser {

	public CommandPhraser() {
		// TODO Auto-generated constructor stub
	}

	public static byte GetUserInputCommand(String command) throws IOException {
		String[] s = command.split(" ");
		if (s[0].equals("?")||s[0].equalsIgnoreCase("help")){
			return BasicMessage.OP_HELP;
		}
		if (s[0].equals("put")) {
			return BasicMessage.OP_lOAD_HBASE;
		}
		if (s[0].equals("file")) {
			return BasicMessage.OP_lOAD_DISK;
		}
		if (s[0].equals("exec")) {
			return BasicMessage.OP_EXEC;
		}
		if (s[0].equals("create")) {
			if (s[1].equals("htable")) {
				return BasicMessage.OP_HTABLE_CREATE;
			}
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
		return (byte) -1;
	}

}
