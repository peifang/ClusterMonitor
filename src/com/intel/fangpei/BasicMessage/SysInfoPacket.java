package com.intel.fangpei.BasicMessage;

import java.util.HashMap;

import com.intel.fangpei.SystemInfoCollector.SysInfo;
import com.intel.fangpei.logfactory.MonitorLog;
/**
 * packet to transfer sysinfo data.
 * @author Administrator
 *
 */
public class SysInfoPacket {
	MonitorLog ml = null;
	public SysInfoPacket(MonitorLog ml ){
		this.ml = ml;
	SysInfo s = SysInfo.GetSysHandler();
	try {
		s.Refresh();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	HashMap<String,String> hm = s.GetSysInfoMap();
	ml.log(hm.get("CPU_Count"));
	ml.log(hm.get("CPU_Perc"));
	}
}
