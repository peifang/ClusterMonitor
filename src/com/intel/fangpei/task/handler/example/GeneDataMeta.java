package com.intel.fangpei.task.handler.example;

import org.apache.hadoop.hbase.client.Put;

import com.intel.fangpei.BasicMessage.Bash;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.task.DataSourcePool;
import com.intel.fangpei.task.handler.Extender;
import com.intel.fangpei.task.handler.TaskHandleable;
import com.intel.fangpei.util.ConfManager;

public abstract class GeneDataMeta<type extends Object>{
	MonitorLog ml = null;
	ReferenceTree rt = null;
    protected long DataSourcePoolSize = 0;
	protected DataSourcePool<type> source = null;
	protected long predictgenes = 0;
	protected double percent = 0.0;
	long genelines = 0;
	private int reportwindow = 100000;
	private int relaxwindow = 100;
	protected int reportnum = 0;
	protected int relaxnum = 0;
	private int failout = 5000;
	public void work() {
		if(reportnum ==0 ||relaxnum == 0 ){
			ml.error("you may fail to set reportwindow and relaxwindow?");
			return;
		}
		reportwindow = reportnum;
		relaxwindow = relaxnum;
		if(source == null){
			//error;
			return;
		}
		int fails = 0;
		if (predictgenes == -1) {
			while (true) {
				if (!source.putone(geneOne())) {
					fails++;
					if(fails > failout){
						ml.error("cann't Gene data timeout,"
								+"please check the Put Threads!");
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
				genelines++;
				reportnum--;
				if (reportnum == 0) {
					reportnum = reportwindow;
				}
			}
		}
		
		while (genelines < predictgenes) {
			if (!source.putone(geneOne())) {
				fails++;
				if(fails > failout){
					ml.error("cann't Gene data timeout,"
							+"please check the Put Threads!");
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
			genelines++;
			reportnum--;
			if (reportnum == 0) {
				percent = genelines / (predictgenes+0.00);
				reportnum = reportwindow;
			}
		}
		percent = 1.0;

	}
	protected abstract type geneOne();
	public  long remain(){
		return predictgenes - genelines;
	}
	public  boolean isUnlimit(){
		return 	predictgenes == -1?true:false;
	}
	public String reportStatus() {
		// TODO Auto-generated method stub
		return "gene data handler have complete:"+percent;
	} 
	public long getGenedNum(){
		return genelines;
	}
	public DataSourcePool<type> getPool() {
		// TODO Auto-generated method stub
		return source;
	}
	public long getPredictNum(){
		return predictgenes;
	}
}
