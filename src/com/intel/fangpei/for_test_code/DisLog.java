package com.intel.fangpei.for_test_code;

import org.apache.commons.logging.Log;

public class DisLog {
public DisLogObj StartServer(String port){
	DisLogObj li = new DisLogObj(port);
	return li;
}
public DisLogObj StartClient(String ip,int port,Log LOG,int level){
	DisLogObj li = new DisLogObj(ip,port,LOG,level);
	return li;
}

}
