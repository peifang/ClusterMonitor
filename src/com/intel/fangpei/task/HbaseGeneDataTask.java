package com.intel.fangpei.task;

import java.io.File;

import org.apache.hadoop.hbase.client.Put;

import com.intel.fangpei.BasicMessage.Bash;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.task.handler.HBaseGeneDataHandler;
import com.intel.fangpei.util.ConfManager;

public class HbaseGeneDataTask extends GeneDataTask<Bash<Put>>{
	public HbaseGeneDataTask(MonitorLog ml,File GenDataSchame, long genNum) {
		super(GenDataSchame, genNum);
		//new HBaseGeneDataHandler(ml,GenDataSchame,super.getPool(),genNum);
		super.ml = ml;
		super.DataSourcePoolNum =  ConfManager.getInt("node.hbase.gene.pool_num",1); 
		super.GeneDatathreads_forOnePool = ConfManager.getInt("node.hbase.gene.threads_one_pool",1);
		super.reportwindow = ConfManager.getInt("node.hbase.gene.reportWindows",5000);
		super.threadsNum = DataSourcePoolNum*GeneDatathreads_forOnePool;
		super.genehandler = new HBaseGeneDataHandler[threadsNum];
		super.pools = new DataSourcePool[DataSourcePoolNum];

		for(int j = 0; j < GeneDatathreads_forOnePool;j++){
			if(j == 0){
				genehandler[0] = new HBaseGeneDataHandler(ml,GenDataSchame,super.getGeneNum()/threadsNum+super.getGeneNum()%threadsNum);
				pools[0] = genehandler[0].getPool();
			}else{
				genehandler[j] = new HBaseGeneDataHandler(ml,GenDataSchame,genehandler[0].getPool(),super.getGeneNum()/threadsNum);
			}
		}
		for(int i = 1;i < DataSourcePoolNum;i++){
			for(int j = 0; j < GeneDatathreads_forOnePool;j++){
				if(j == 0){
					genehandler[i*GeneDatathreads_forOnePool] = new HBaseGeneDataHandler(ml,GenDataSchame,super.getGeneNum()/threadsNum);
					pools[i] = genehandler[i*GeneDatathreads_forOnePool].getPool();
				}else{
					genehandler[i*GeneDatathreads_forOnePool+j] = new HBaseGeneDataHandler(ml,GenDataSchame,genehandler[i*GeneDatathreads_forOnePool].getPool(),super.getGeneNum()/threadsNum);
				}
			}
		}
	}
	@Override
	public void run() {
		for(int i = 0;i < DataSourcePoolNum;i++){
			for(int j = 0; j < GeneDatathreads_forOnePool;j++){
				new Thread(genehandler[i*GeneDatathreads_forOnePool+j]).start();
			}
		}
		while(true){
			ml.log("Gene progress:"+getPercent());
			if(getPercent() > 0.9999999){
				ml.log("Gene data Task has complete!" +
						"waiting for Put Thread to fetch over"+
						"remaining:"+remain());
				if(remain() == 0){
					ml.log("already fetch over!exit ...");
					for(int i = threadsNum;i>0;i--){
						ml.log("Gene Thread "+(i-1)+" have Gened Num:"+genehandler[i-1].getGenedNum());
					}
					break;
				}
				
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
		for(int i = threadsNum;i>0;i--){
			percent += genehandler[i-1].taskCompletePercent();
		}
		return percent/threadsNum;
	}
	private long remain(){
		long totalremain = 0;
		for(int i = pools.length;i>0;i--){
			totalremain += pools[i-1].size();
		}
		return totalremain;
	}
	public DataSourcePool<Bash<Put>>[] getPools(){
		return pools;
	}

}
