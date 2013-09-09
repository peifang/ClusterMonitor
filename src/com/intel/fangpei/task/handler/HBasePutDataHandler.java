package com.intel.fangpei.task.handler;

import org.apache.hadoop.hbase.client.Put;

import com.intel.fangpei.BasicMessage.Bash;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.task.DataSourcePool;
import com.intel.fangpei.util.ConfManager;

public class HBasePutDataHandler extends PutDataHandleable<Bash<Put>>{
	HBaseHandler hbaseHandler = new HBaseHandler();
	String hostname = null;
	String port = null;
	String master = null;
	String tableName = null;

	public HBasePutDataHandler(MonitorLog ml,String hostname, String port, String master,
			String tableName) {
		this.hostname = hostname;
		this.port = port;
		this.master = master;
		this.tableName = tableName;
		super.reportnum = ConfManager.getInt("node.hbase.put.reportWindows",100000);
		super.relaxnum = ConfManager.getInt("node.hbase.put.relaxWindows",90);
		hbaseHandler.connect(hostname, port, master);
		hbaseHandler.switchToHTable(tableName);
		super.ml = ml;
	}
	@Override
	public boolean setDataSource(DataSourcePool<Bash<Put>>[] pools, long predictputs) {
		if(pools == null){
			return false;
		}
			super.source = pools;
			super.predictputs = predictputs;
			return true;
	}
	@Override
	public boolean setDataSource(DataSourcePool<Bash<Put>> pools, long predictputs) {
		if(pools == null){
			return false;
		}
			super.source = new DataSourcePool[1];
			super.source[0] = pools;
			super.predictputs = predictputs;
			return true;
	}
	@Override
	public void run() {
		commitTask();
	}

	@Override
	public boolean putOne(Bash<Put> put) {
		if(put == null){
		//	ml.error("cann't get source data ,will soon retry...");
			return false;
		}
		return true;
	}
	public long getPridictPuts(){
		return predictputs;
	}
}
