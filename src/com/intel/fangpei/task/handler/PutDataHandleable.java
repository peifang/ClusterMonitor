package com.intel.fangpei.task.handler;

import org.apache.hadoop.hbase.client.Put;

import com.intel.fangpei.BasicMessage.Bash;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.task.DataSourcePool;
import com.intel.fangpei.util.ConfManager;

public abstract class PutDataHandleable<type extends Object> implements TaskHandleable {
	MonitorLog ml = null;
	DataSourcePool<type>[] source = null;
	protected int poollen = 0;
	long predictputs = 0;
	private double percent = 0.000;
	protected long insertLines = 0;
	private int reportwindow = 100000;
	private int relaxwindow  = 90;
	private int i = 0;
	private type tmp = null; 
	private int failout = 50;
	protected int reportnum = 0;
	protected int relaxnum = 0;
	@Override
	public void commitTask() {
		if(reportnum ==0 ||relaxnum == 0 ){
			ml.error("you may fail to set reportwindow and relaxwindow?");
			return;
		}
		poollen = source.length;
		reportwindow = reportnum;
		relaxwindow = relaxnum;
		//ml.log("before insert lines: "+insertLines+", predicputs = "+predictputs);
		if (source == null) {
			//ml.error("no data source is defined");
			System.exit(1);
		}
		int fails = 0;
		while (insertLines < predictputs) {
			if (!putOne(Getone())) {
				fails ++;
				if(fails > failout){
					ml.error("cann't get source pool data timeout,"
							+"please check the Gene Threads!");
					return;
				}
				try {
					Thread.sleep(relaxwindow);
					continue;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			fails = 0;
			insertLines++;
			reportnum--;
			if (reportnum == 0) {
				//ml.log("insert lines: "+insertLines+", predicputs = "+predictputs);
				percent = (insertLines) /(predictputs+0.00);
				reportnum = reportwindow;
			}
		}
		//System.out.println("--asdasd"+insertLines);
		percent = 1.000;

	}
	public type Getone(){
		tmp = source[0].getone();
		if(tmp!=null){
			return tmp;
		}
		for(i = 1;i < poollen;i++){
			tmp = source[i].getone();
			if(tmp!=null){
				return tmp;
			}
		}
		return null;
	}
	public abstract boolean setDataSource(DataSourcePool<type>[] pools, long predictputs);
	public abstract boolean setDataSource(DataSourcePool<type> pools, long predictputs);
	public abstract boolean putOne(type put);
	@Override
	public double taskCompletePercent(){
		return percent;
	}
}
