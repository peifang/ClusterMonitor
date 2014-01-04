package com.intel.fangpei.network.rpc;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.client.util.ClientFactory;

import com.intel.fangpei.util.ConfManager;

public class RpcClient {
	private static XmlRpcClient client = null;
	private static RpcClient rpc = null;
	private RpcClient(){
		try {
			String serverip = ConfManager.getConf("selectsocket.server.ip");
			int     rpcport = ConfManager.getInt("selectsocket.rpc.port", 1235);
			if(serverip == null){
				System.out.println("Cann't find RPC Server IP,Exit!");
				System.exit(1);
			}
			init(new URL("http://"+serverip+":"+rpcport+"/xmlrpc"));
		} catch (MalformedURLException e) {
			System.out.println("URL not formated!");
			e.printStackTrace();
		}
	}
	public static synchronized RpcClient getInstance(){
		if(rpc == null){
			rpc = new RpcClient();
		}
		return rpc;
	}
public void init(URL url){
    // create configuration
    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    config.setServerURL(url);//(new URL("http://127.0.0.1:8080/xmlrpc"));
    config.setEnabledForExtensions(true);  
    config.setConnectionTimeout(60 * 1000);
    config.setReplyTimeout(60 * 1000);

    client = new XmlRpcClient(); 
    // use Commons HttpClient as transport
    client.setTransportFactory(
        new XmlRpcCommonsTransportFactory(client));
    // set configuration
    client.setConfig(config);
}
public Object execute(String function,Object[] params){
	if(client == null){
		return null;
	}
    // make the a regular call
   try {
	return client.execute(function, params);
} catch (XmlRpcException e) {
	System.out.println("**********Xml Rpc Exception !***********");
	e.printStackTrace();
	return null;
}
}
}
