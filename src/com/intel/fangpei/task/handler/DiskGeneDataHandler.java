package com.intel.fangpei.task.handler;

import java.io.File;
import java.util.Random;

import com.intel.fangpei.BasicMessage.Bash;
import com.intel.fangpei.Schema.ReferenceTree;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.task.DataSourcePool;
import com.intel.fangpei.util.ConfManager;

public class DiskGeneDataHandler extends GeneDataHandleable<Bash<String>> {
	int bashnum = 0;
	int processbash = 0;
	public DiskGeneDataHandler(MonitorLog ml,File schema, long predictgenes) {
		super.ml = ml;
		super.DataSourcePoolSize = ConfManager.getInt("node.disk.gene.pool_size", 100000);
		super.source = new DataSourcePool<Bash<String>>(DataSourcePoolSize);
		super.reportnum = ConfManager.getInt("node.disk.gene.reportWindows",500);
		super.relaxnum = ConfManager.getInt("node.disk.gene.relaxWindows",100);
		super.predictgenes = predictgenes;
		super.rt = new ReferenceTree(schema);
		super.rt.CreateTree();
	}
	public DiskGeneDataHandler(MonitorLog ml,File schema,DataSourcePool<Bash<String>> dataTask, long predictgenes) {
		super.ml = ml;
		super.source = dataTask;
		super.reportnum = ConfManager.getInt("node.disk.gene.reportWindows",500);
		super.relaxnum = ConfManager.getInt("node.disk.gene.relaxWindows",100);
		super.predictgenes = predictgenes;
		rt = new ReferenceTree(schema);
		rt.CreateTree();
		
	}

	@Override
	public void run() {
		bashnum = ConfManager.getInt("node.disk.gene.bash",5);
		commitTask();
	}

	@Override
	protected Bash<String> geneOne() {
		processbash = bashnum ;
		Bash<String> b =  new Bash<String>();
		while(processbash -->0)
		b.add(rt.getdata()+"\n");
		return b;
	}
	public DataSourcePool<Bash<String>> getPool() {
		return source;
	}

}
