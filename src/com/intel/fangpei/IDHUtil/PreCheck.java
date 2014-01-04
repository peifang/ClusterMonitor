package com.intel.fangpei.IDHUtil;

import java.io.File;
import java.io.IOException;
import com.intel.fangpei.network.rpc.RpcClient;
import com.intel.fangpei.process.ProcessFactory;
import com.intel.fangpei.process.ProcessManager;

public class PreCheck {
	/*
	 * check whether the system version is supported and other
	 * configuration is configured OK!
	 */
	public static void check(){
		RpcClient client = RpcClient.getInstance();
        Object[] params = new Object[]
                { new Integer(2)};
        Object o = client.execute("RpcRemoteMessage.nodeIpList", params);
        System.out.println("received class type:"+o.getClass().getName());
        Object[] iplist = (Object[])o;
        String[] shparams = new String[iplist.length+2];
        shparams[0]="sh";
        shparams[1]="bandwidth.sh";
        for(int p=2;p < iplist.length+2;p++){
        	shparams[p] = (String) iplist[p-2];
        }
		int procid = ProcessFactory.buildNewProcess(shparams);
		ProcessManager.get(procid).setWorkDir("/usr/bin");
		File logfile = new File("/var/log/checklog");
		if(!logfile.exists())
			try {
				logfile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		ProcessManager.start(procid,logfile);
	}

}
