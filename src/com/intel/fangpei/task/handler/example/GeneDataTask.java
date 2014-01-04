package com.intel.fangpei.task.handler.example;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.task.DataSourcePool;
import com.intel.fangpei.task.ExtendTask;
import com.intel.fangpei.task.Task;
import com.intel.fangpei.task.handler.Extender;

public class GeneDataTask<type extends Object> extends Extender{
	protected MonitorLog ml = null;
    protected DataSourcePool<type> pool = null;
    protected DiskGeneDataWorker worker = null;
    protected int threadsNum = 0;
    protected int reportwindow = 0;
	/*
	 * define a collection as the container of all the gene puts. the type of
	 * the collection's elements should be type.
	 */
	File GenDataSchame = null;
	private long genNum = 0;

	public GeneDataTask(File GenDataSchame, long genNum) {
		try {
			this.ml=new MonitorLog();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.GenDataSchame = GenDataSchame;
		this.genNum = genNum;
	}
	public long getGeneNum(){
		return genNum;
	}
	public DataSourcePool<type> getPools(){
		return pool;
	}
	@Override
	public void commitTask() {
		worker=new DiskGeneDataWorker(ml,GenDataSchame,genNum);
		worker.work();
	}

}
