package com.intel.fangpei.task.handler;

import java.io.File;
import java.util.Random;

import org.apache.hadoop.hbase.client.Put;
import com.intel.fangpei.BasicMessage.Bash;
import com.intel.fangpei.Schema.ReferenceTree;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.task.DataSourcePool;
import com.intel.fangpei.util.ConfManager;

public class HBaseGeneDataHandler extends GeneDataHandleable<Bash<Put>> {
	byte[] family = null;
	public HBaseGeneDataHandler(MonitorLog ml,File schema, long predictgenes) {
		super.ml = ml;
		super.DataSourcePoolSize = ConfManager.getInt("node.hbase.gene.pool_size", 100000);
		super.source = new DataSourcePool<Bash<Put>>(DataSourcePoolSize);
		super.reportnum = ConfManager.getInt("node.hbase.gene.reportWindows",500);
		super.relaxnum = ConfManager.getInt("node.hbase.gene.relaxWindows",100);
		super.predictgenes = predictgenes;
		super.rt = new ReferenceTree(schema);
		super.rt.CreateTree();
		String cfname = rt.getAttribute("family");
		if(cfname == null){
			ml.error("undefined comlum family name," +
					"please define it in the schema.xml");
		}else{
			family = cfname.getBytes();
		}
		
	}
	public HBaseGeneDataHandler(MonitorLog ml,File schema) {
		super.ml = ml;
		DataSourcePoolSize = ConfManager.getInt("node.hbase.gene.pool_size", 100000);
		super.source = new DataSourcePool<Bash<Put>>(DataSourcePoolSize);
		super.reportnum = ConfManager.getInt("node.hbase.gene.reportWindows",500);
		super.relaxnum = ConfManager.getInt("node.hbase.gene.relaxWindows",100);
		super.predictgenes = -1;
		rt = new ReferenceTree(schema);
		rt.CreateTree();
		String cfname = rt.getAttribute("family");
		if(cfname == null){
			ml.error("undefined comlum family name," +
					"please define it in the schema.xml");
		}else{
			family = cfname.getBytes();
		}
		
	}
	public HBaseGeneDataHandler(MonitorLog ml,File schema,DataSourcePool<Bash<Put>> dataTask, long predictgenes) {
		super.ml = ml;
		super.source = dataTask;
		super.reportnum = ConfManager.getInt("node.hbase.gene.reportWindows",500);
		super.relaxnum = ConfManager.getInt("node.hbase.gene.relaxWindows",100);
		super.predictgenes = predictgenes;
		rt = new ReferenceTree(schema);
		rt.CreateTree();
		String cfname = rt.getAttribute("family");
		if(cfname == null){
			ml.error("undefined comlum family name," +
					"please define it in the schema.xml");
		}else{
			family = cfname.getBytes();
		}
		
	}

	public HBaseGeneDataHandler(MonitorLog ml,File schema,DataSourcePool<Bash<Put>> dataTask) {
		super.ml = ml;
		super.source = dataTask;
		super.reportnum = ConfManager.getInt("node.hbase.gene.reportWindows",500);
		super.relaxnum = ConfManager.getInt("node.hbase.gene.relaxWindows",100);
		super.predictgenes = -1;
		rt = new ReferenceTree(schema);
		rt.CreateTree();
		String cfname = rt.getAttribute("family");
		if(cfname == null){
			ml.error("undefined comlum family name," +
					"please define it in the schema.xml");
		}else{
			family = cfname.getBytes();
		}

	}

	@Override
	public void run() {
		commitTask();
	}

	@Override
	protected Bash<Put> geneOne() {
		if(family == null){
			ml.warn("cann't Gene Data because of the loss of column family name");
			return null;
		}
		return new Bash<Put>();//new Put(getCode()).add(family, null, rt.getdata().getBytes());
	}
	//ID
	public byte[] getCode(){
		return (new Random().nextLong()+"").getBytes();
	}
	public DataSourcePool<Bash<Put>> getPool() {
		// TODO Auto-generated method stub
		return source;
	}

}
