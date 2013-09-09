package com.intel.fangpei.BasicMessage;

import java.util.HashMap;
import java.util.Map;

public class BasicMessage {
	Map<Integer, String> s = new HashMap<Integer, String>();
	public static final byte NODE = (byte) 81;
	public static final byte ADMIN = (byte) 80;
	public static final byte SERVER = (byte) 79;
	public static final byte OP_LOGIN = (byte) 15;
	public static final byte OP_QUIT = (byte) 10;
	public static final byte OP_CLOSE = (byte) 9;
	public static final byte OP_EXEC = (byte) 1;
	public static final byte OP_MESSAGE = (byte) 2;
	public static final byte OP_SYSINFO = (byte) 11;
	public static final byte OP_lOAD_HBASE = (byte) 3;
	public static final byte OP_lOAD_DISK = (byte) 5;
	public static final byte OP_lOAD_HIVE = (byte) 6;
	public static final byte OP_HTABLE_CREATE = (byte) 4;
	public static final int VERSION = 1;

	public static final byte OK = (byte) 5;
}
