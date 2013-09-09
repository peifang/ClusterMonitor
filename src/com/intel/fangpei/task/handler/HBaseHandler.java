package com.intel.fangpei.task.handler;

import java.io.IOException;

import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTablePool;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.hadoop.hbase.util.HBaseUtility;

public  class HBaseHandler {
	protected MonitorLog ml = null;
	HBaseAdmin hbaseadmin = null;
	HTablePool pool = null;
	HTable table = null;
	private long deleteLine = 0;
	private int createTableNum = 0;
	private int deleteTableNum = 0;

	public HBaseHandler() {
		try {
			ml = new MonitorLog();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public HBaseAdmin getHBaseAdmin() {
		if (hbaseadmin != null)
			return hbaseadmin;
		try {
			hbaseadmin = HBaseUtility.create().getHBaseAdmin();
			return hbaseadmin;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public boolean createHTable(String tableName) {
		if (hbaseadmin == null)
			hbaseadmin = getHBaseAdmin();
		try {
			hbaseadmin.createTable(new HTableDescriptor(tableName));
			createTableNum++;
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteHTable(String tableName) {
		if (hbaseadmin == null)
			hbaseadmin = getHBaseAdmin();
		try {
			hbaseadmin.disableTable(tableName);
			hbaseadmin.deleteTable(tableName);
			deleteTableNum++;
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public boolean connect(String hostname, String port, String master) {
		/*
		 * if(pool !=null) try { pool.close(); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); return false; }
		 * Configuration HBASE_CONFIG = new Configuration();
		 * HBASE_CONFIG.set("hbase.zookeeper.quorum", hostname);
		 * HBASE_CONFIG.set("hbase.zookeeper.property.clientPort", port);
		 * HBASE_CONFIG.set("hbase.master", master); pool = new
		 * HTablePool(HBASE_CONFIG, 1000);
		 */
		return true;
	}

	public boolean switchToHTable(String tableName) {
		/*
		 * if(table != null) try { table.close(); } catch (IOException e) { //
		 * TODO Auto-generated catch block e.printStackTrace(); return false; }
		 * table = (HTable) pool.getTable(tableName);
		 */
		return true;
	}

	protected int getCreateHTableNum() {
		return createTableNum;
	}

	protected int getDeleteHTableNum() {
		return deleteTableNum;
	}

	protected long getDeleteLine() {
		return deleteLine;
	}

	protected void setDeleteLine(long deleteLine) {
		this.deleteLine = deleteLine;
	}

}
