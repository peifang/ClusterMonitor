package com.intel.fangpei.task;

import org.apache.hadoop.hbase.client.Put;

import com.intel.fangpei.BasicMessage.Bash;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.task.handler.HBasePutDataHandler;
import com.intel.fangpei.util.ConfManager;

public class HbasePutDataTask extends PutDataTask<Bash<Put>>{
	HBasePutDataHandler puthandle[] = null;
	int threads = 1;
	int threads_onepool = 1;
	int pool_num= 0;
	String hostname = null;
	String port = null;
	String master = null;
	String tablename = null;
	int reportwindow = 0;
	public HbasePutDataTask(MonitorLog ml,GeneDataTask<Bash<Put>> sourceTask, long needs,String hostname,
			String port, String master, String tablename) {
		super(sourceTask,needs);
		this.hostname = hostname;
		this.master = master;
		this.port = port;
		this.tablename = tablename;
		reportwindow = ConfManager.getInt("node.hbase.put.reportWindows",5000);
			super.ml = ml;

	}
	@Override
	public void run() {
		threads_onepool = ConfManager.getInt("node.hbase.put.threads_one_pool",1);
		pool_num = ConfManager.getInt("node.hbase.gene.pool_num",1);
		int genethreads_one_pool = ConfManager.getInt("node.hbase.gene.threads_one_pool",1);
		int gene_threads =  genethreads_one_pool*pool_num;
		threads = threads_onepool*pool_num;
		
		puthandle = new HBasePutDataHandler[threads];
		DataSourcePool<Bash<Put>>[] pools = ((HbaseGeneDataTask)sourceTask).getPools();
		if(pools.length != pool_num){
			ml.error("unexpected pool num ! "+pool_num+" VS "+pools.length);
			System.exit(1);
		}
		long pool  = (needs/gene_threads)*genethreads_one_pool;
		long pool0 = (needs/gene_threads)*genethreads_one_pool+needs%gene_threads;
		puthandle[0] = new HBasePutDataHandler(ml,hostname,port,master,tablename);
		puthandle[0].setDataSource(pools[0], (pool0)/threads_onepool+
													(pool0)%threads_onepool);
		new Thread(puthandle[0]).start();
		for(int j = 1; j < threads_onepool;j++){
			puthandle[j] = new HBasePutDataHandler(ml,hostname,port,master,tablename);
			puthandle[j].setDataSource(pools[0], (pool0)/threads_onepool);
			new Thread(puthandle[j]).start();
			}
		

		for(int i = 1 ;i < pool_num;i++){
			for(int j = 0; j < threads_onepool;j++){
				if(j == 0){
					puthandle[i*threads_onepool+j] = new HBasePutDataHandler(ml,hostname,port,master,tablename);
					puthandle[i*threads_onepool+j].setDataSource(pools[i],  pool/threads_onepool+pool%threads_onepool);
				}else{
					puthandle[i*threads_onepool+j] = new HBasePutDataHandler(ml,hostname,port,master,tablename);
					puthandle[i*threads_onepool+j].setDataSource(pools[i],  pool/threads_onepool);
				}
			new Thread(puthandle[i*threads_onepool+j]).start();
			}
		}
		while(true){
			ml.log("Put progress:"+getPercent());
			if(getPercent() > 0.9999999){
				ml.log("load data Task has complete!");
				for(int i = threads;i>0;i--){
					ml.log("thread"+(i-1)+" complete :"+puthandle[i-1].getPridictPuts());
				}
				break;
			}
			try {
				Thread.sleep(reportwindow);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private double getPercent(){
		double percent = 0.0000000;
		for(int i = threads;i>0;i--){
			percent += puthandle[i-1].taskCompletePercent();
		}
		return percent/threads;
	}

}
