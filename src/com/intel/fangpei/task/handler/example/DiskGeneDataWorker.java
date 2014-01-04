package com.intel.fangpei.task.handler.example;

import java.io.File;

import com.intel.fangpei.BasicMessage.Bash;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.task.DataSourcePool;
import com.intel.fangpei.util.ConfManager;

public class DiskGeneDataWorker extends GeneDataMeta<Bash<String>> {
	int bashnum = 0;
	int processbash = 0;
	public DiskGeneDataWorker(MonitorLog ml,File schema, long predictgenes) {
		super.ml = ml;
		super.DataSourcePoolSize = ConfManager.getInt("node.disk.gene.pool_size", 100000);
		super.source = new DataSourcePool<Bash<String>>(DataSourcePoolSize);
		super.reportnum = ConfManager.getInt("node.disk.gene.reportWindows",500);
		super.relaxnum = ConfManager.getInt("node.disk.gene.relaxWindows",100);
		super.predictgenes = predictgenes;
		super.rt = new ReferenceTree(schema);
		super.rt.CreateTree();
	}
	public void work() {
		bashnum = ConfManager.getInt("node.disk.gene.bash",5);
		super.work();
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
