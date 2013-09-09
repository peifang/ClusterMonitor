package com.intel.fangpei.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.hadoop.hbase.client.Put;

import com.intel.fangpei.BasicMessage.Bash;
import com.intel.fangpei.FileSystem.Directory;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.task.handler.DiskPutDataHandler;
import com.intel.fangpei.task.handler.HBasePutDataHandler;
import com.intel.fangpei.task.handler.PutDataHandleable;
import com.intel.fangpei.util.ConfManager;

public class DiskPutDataTask extends PutDataTask<Bash<String>> {
	DiskPutDataHandler puthandle[] = null;
	int threads = 1;
	int threads_one_pool = 1;
	int pool_num= 0;
	Directory d = null;
	int reportwindow = 0;
	private long putStartTime = 0;
	private long putEndTime = 0;
	private Thread[] putThreads = null;
	public DiskPutDataTask(MonitorLog ml,DiskGeneDataTask sourceTask, long needs,Directory d) {
		super(sourceTask,needs);
		this.d = d;
		reportwindow = ConfManager.getInt("node.disk.put.reportWindows",5000);
			super.ml = ml;

	}
	@Override
	public void run() {
		threads_one_pool = ConfManager.getInt("node.disk.put.threads_one_pool",1);
		pool_num = ConfManager.getInt("node.disk.gene.pool_num",1);
		int genethreads_one_pool = ConfManager.getInt("node.disk.gene.threads_one_pool",1);
		int gene_threads =  genethreads_one_pool*pool_num;

		DataSourcePool<Bash<String>>[] pools = sourceTask.getPools();
		if(pools.length != pool_num){
			ml.error("unexpected pool num ! "+pool_num+" VS "+pools.length);
			System.exit(1);
		}
		/*
		 * here ,we get all the dirs we can use for dataPutThreads.
		 * and we use some policy to apply all these resource to 
		 * put threads.
		 */
		ArrayList<String> dirs = d.getDirList();
		/*
		 * validate the directories;
		 */
		dirs = validateDir(dirs);
		int dirnum = dirs.size()-1;
		/*if no dir declare ,return!*/
		if(dirnum < 0) {
			ml.error("no dir is configured at all," +
					"please check Conf node.disk.load.dir.");
			return;
		}
		ml.log("we have "+dirs.size()+" folder can be used to storage Gened data!");
		putThreads = new Thread[dirs.size()];
		puthandle = new DiskPutDataHandler[dirs.size()];
		threads = dirs.size();
		boolean threadsunsupport = false;
		long pool  = (needs/gene_threads)*genethreads_one_pool;
		long pool0 = (needs/gene_threads)*genethreads_one_pool+needs%gene_threads;
		puthandle[0] = new DiskPutDataHandler(ml,dirs.get(dirnum));
		puthandle[0].setDataSource(pools[0], (pool0)/threads_one_pool+
													(pool0)%threads_one_pool);
		putThreads[0] = new Thread(puthandle[0]);
		dirnum -- ;
		for(int j = 1; j < threads_one_pool;j++){
			if(dirnum < 0){
				ml.warn("because we have less dir to distribute for "
						+"Put threads,so some put threads " +
						"for some DatasourcePool may not start...");
				threadsunsupport = true;
				break;
			}
			puthandle[j] = new DiskPutDataHandler(ml,dirs.get(dirnum));
			puthandle[j].setDataSource(pools[0], (pool0)/threads_one_pool);
			putThreads[j] = new Thread(puthandle[j]);
			dirnum -- ;
			}
		

		for(int i = 1 ;i < pool_num;i++){
			for(int j = 0; j < threads_one_pool;j++){
				if(dirnum < 0){
					ml.warn("because we have less dir to distribute for "
							+"Put threads,so some put threads " +
							"for some DatasourcePool may not start...");
					threadsunsupport = true;
					break;

				}
				if(j == 0){
					puthandle[i*threads_one_pool+j] = new DiskPutDataHandler(ml,dirs.get(dirnum));
					puthandle[i*threads_one_pool+j].setDataSource(pools[i],  pool/threads_one_pool+pool%threads_one_pool);
				}else{
					puthandle[i*threads_one_pool+j] = new DiskPutDataHandler(ml,dirs.get(dirnum));
					puthandle[i*threads_one_pool+j].setDataSource(pools[i],  pool/threads_one_pool);
				}
				putThreads[i*threads_one_pool+j] = new Thread(puthandle[i*threads_one_pool+j]);
			dirnum -- ;
			}
		}
		putStartTime = System.currentTimeMillis();
		ml.log("put start:"+putStartTime);
		for(int i = 0;i < threads;i++){
			putThreads[i].start();
		}
		while(true){
			ml.log("Put progress:"+getPercent());
			if(getPercent() > 0.9999999){
				putEndTime = System.currentTimeMillis();
				ml.log("load data Task has complete!,use time :"+(putEndTime - putStartTime));
				for(int i = threads;i>0;i--){
					ml.log("thread"+(i-1)+" complete :"+puthandle[i-1].getPridictPuts());
				}
				break;
			}
			try {
				Thread.sleep(reportwindow);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	private ArrayList<String> validateDir(ArrayList<String> dirs) {
		Iterator<String> i = dirs.iterator();
		String next = null;
		ArrayList<String> useableDir = new ArrayList<String>();
		while(i.hasNext()){
			next = i.next() ;		
			if(new File(next).isDirectory()){
				useableDir.add(next);
			}
		}
		return useableDir;
	}
	private double getPercent(){
		double percent = 0.0000000;
		for(int i = threads;i>0;i--){
			percent += puthandle[i-1].taskCompletePercent();
		}
		return percent/threads;
	}
}
