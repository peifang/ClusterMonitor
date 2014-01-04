package com.intel.fangpei.network.rpc;

import java.io.IOException;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import com.intel.fangpei.util.ReflectFactory;

public class RpcServer {
private WebServer webServer = null;
private XmlRpcServer xmlRpcServer = null;
private boolean isRunning = false;
public RpcServer(int port){
    webServer    = new WebServer(port);
	}
public void StartRPCServer(){
    xmlRpcServer = webServer.getXmlRpcServer();
    PropertyHandlerMapping phm = new PropertyHandlerMapping();
    /* Load handler definitions from a property file.
     * The property file might look like:
     *   Calculator=org.apache.xmlrpc.demo.Calculator
     *   org.apache.xmlrpc.demo.proxy.Adder=org.apache.xmlrpc.demo.proxy.AdderImpl
     */
    try {
		phm.load(ReflectFactory.getInstance().getContextClassLoader(),
				"com/intel/fangpei/network/rpc/Rpc.properties");
	} catch (IOException e) {
		System.out.println("1");
		e.printStackTrace();
	} catch (XmlRpcException e) {
		System.out.println("2");
		e.printStackTrace();
	}

//      phm.addHandler("Calculator",
//          org.apache.xmlrpc.demo.Calculator.class);
//      phm.addHandler(org.apache.xmlrpc.demo.proxy.Adder.class.getName(),
//          org.apache.xmlrpc.demo.webserver.proxy.impls.AdderImpl.class);
     
    xmlRpcServer.setHandlerMapping(phm);
  
    XmlRpcServerConfigImpl serverConfig =
        (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
    serverConfig.setEnabledForExtensions(true);
    serverConfig.setContentLengthOptional(false);
    try {
		webServer.start();
		isRunning = true;
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
public void stop(){
	webServer.shutdown();
}
public boolean isRunning(){
	return isRunning;
}
public static void main(String[] args){
	new RpcServer(1235).StartRPCServer();
}
}
