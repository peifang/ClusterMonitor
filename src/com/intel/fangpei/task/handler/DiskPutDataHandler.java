package com.intel.fangpei.task.handler;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.hadoop.hbase.client.Put;

import com.intel.fangpei.BasicMessage.Bash;
import com.intel.fangpei.FileSystem.Directory;
import com.intel.fangpei.FileSystem.Ext4Dir;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.task.DataSourcePool;
import com.intel.fangpei.util.ConfManager;

public class DiskPutDataHandler extends PutDataHandleable<Bash<String>> {
	private String dir = null;
	private int filesize =0;
	DataOutputStream out = null;
	String dirstring = null;
	private int filename = 0;
	public DiskPutDataHandler(MonitorLog ml,String dir) {
		super.reportnum = ConfManager.getInt("node.disk.put.reportwindows",100000);
		super.relaxnum = ConfManager.getInt("node.disk.put.relaxwindows",90);
		this.dir = dir;
		super.ml = ml;
	}
	@Override
	public void run() {
		if(dir == null){
			ml.error("no dir to write data!");
			return;
		}
       filesize = ConfManager.getInt("node.disk.load.file.maxsize",64*1024*1024);
		File file = null;
		try {
			file = new File(dir+filename);
			filename ++;
			if(!file.exists()){
				file.createNewFile();
			}
			out = new DataOutputStream( 
					new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			ml.error("FileNotFoundException :"+e.getMessage());
			return;
		} catch (IOException e){
			ml.error("IOException :"+e.getMessage());
			return;
		}
		commitTask();	
	}

	@Override
	public boolean putOne(Bash<String> put) {
		if(put == null){
			return false;
		}
		/*
		 * no need to use mutiple disk for one threads.
````````````````````````````````````````````````````````````*/
			if(out.size() > filesize){
				try {
					out.close();
				} catch (IOException e) {
					ml.error(e.getMessage()+":"+dir+filename);
					return false;
				}
				try {
					File tmpfile = new File(dir+filename);
					filename ++;
					if(!tmpfile.exists())
						tmpfile.createNewFile();
					out = new DataOutputStream( 
							new FileOutputStream(tmpfile));
				} catch (IOException e) {
					ml.error(e.getMessage()+":"+dir+filename);
					return false;
				}
			}
			while(put.size() > 0 ){
				try {
					out.writeBytes(put.get());
				} catch (IOException e) {
					ml.error(e.getMessage());
					try {
						out.close();
					} catch (IOException e1) {
						ml.error(e.getMessage()+":"+dir+filename);
						return false;
					}
					return false;
				}
			}
			if(super.insertLines == predictputs - 1){
				try {
					ml.log("complete insert to "+dir+", close outputStream");
					out.close();
				} catch (IOException e) {
					ml.error("IOException: "+e.getMessage());
					return false;
				}
			}
			return true;
	}

	@Override
	public boolean setDataSource(DataSourcePool<Bash<String>>[] pools,
			long predictputs) {
		if(pools == null){
			return false;
		}
			super.source = pools;
			super.predictputs = predictputs;
			return true;
	}

	@Override
	public boolean setDataSource(DataSourcePool<Bash<String>> pools, long predictputs) {
		if(pools == null){
			return false;
		}
			super.source = new DataSourcePool[1];
			super.source[0] = pools;
			super.predictputs = predictputs;
			return true;
	}
	public long getPridictPuts(){
		return predictputs;
	}
}
