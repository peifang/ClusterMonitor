package com.intel.fangpei.SystemInfoCollector;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetFlags;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.OperatingSystem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarNotImplementedException;
import org.hyperic.sigar.Swap;

import com.intel.fangpei.util.SystemUtil;
public class SysInfo {
	private static HashMap<String,String> HardWareRes =new HashMap<String,String>();
	private static SysInfo sysinfo = null;
	private SysInfo(){
	init();	
	}
	public static synchronized SysInfo GetSysHandler(){
		if(sysinfo == null){
			sysinfo = new SysInfo();
		}
			return sysinfo;
	}
	private void init(){
		SystemUtil.initSysParameter(HardWareRes);
	}
	public void Refresh() throws Exception{
		getCpuCount();
		getCpuTotal();
		testCpuPerc();
		getPhysicalMemory();
		getPlatformName();
		testGetOSInfo();
		testWho();
		testFileSystemInfo();
		HardWareRes.put("NetWork_FQDN",getFQDN());
		HardWareRes.put("NetWork_IP",getDefaultIpAddress());
		HardWareRes.put("NetWork_MAC",getMAC());
		testNetIfList();
	//	getEthernetInfo();
	}
 // 1.CPU资源信息
 // a)CPU数量（单位：个）
 public static void getCpuCount() throws SigarException {
  Sigar sigar = new Sigar();
  try {
	  HardWareRes.put("CPU_Count", ""+sigar.getCpuInfoList().length);
  } finally {
   sigar.close();
  }
 }
 // b)CPU的总量（单位：HZ）及CPU的相关信息
 public static void getCpuTotal() {
  Sigar sigar = new Sigar();
  CpuInfo[] infos;
  try {
   infos = sigar.getCpuInfoList();
   for (int i = 0; i < infos.length; i++) {// 不管是单块CPU还是多CPU都适用
    CpuInfo info = infos[i];
    HardWareRes.put("CPU_Mhz",""+info.getMhz());// CPU的总量MHz
    HardWareRes.put("CPU_Vendor",info.getVendor());// 获得CPU的卖主，如：Intel
    HardWareRes.put("CPU_Celeron", info.getModel());// 获得CPU的类别，如：Celeron
    HardWareRes.put("CPU_Cache",""+info.getCacheSize());// 缓冲存储器数量
   }
  } catch (SigarException e) {
   e.printStackTrace();
  }
 }
 // c)CPU的用户使用量、系统使用剩余量、总的剩余量、总的使用占用量等（单位：100%）
 public static void testCpuPerc() {
  Sigar sigar = new Sigar();
  // 方式一，主要是针对一块CPU的情况
 /* CpuPerc cpu;
  try {
   cpu = sigar.getCpuPerc();
   printCpuPerc(cpu);
  } catch (SigarException e) {
   e.printStackTrace();
  }*/
  // 方式二，不管是单块CPU还是多CPU都适用
  CpuPerc cpuList[] = null;
  try {
   cpuList = sigar.getCpuPercList();
  } catch (SigarException e) {
   e.printStackTrace();
   return;
  }
  String tmp = "";
  for (int i = 0; i < cpuList.length; i++) {
	  tmp+=    "|User |" + CpuPerc.format(cpuList[i].getUser())// 用户使用率
              +"|Sys  |" + CpuPerc.format(cpuList[i].getSys())// 系统使用率
              +"|Wait |" + CpuPerc.format(cpuList[i].getWait())// 当前等待率
              +"|Nice |" + CpuPerc.format(cpuList[i].getNice())//
              +"|Idle |" + CpuPerc.format(cpuList[i].getIdle())// 当前空闲率
              +"|Total|" + CpuPerc.format(cpuList[i].getCombined());// 总的使用率
  }
  HardWareRes.put("CPU_Perc", tmp);
 }
 // 2.内存资源信息
 public static void getPhysicalMemory() {
  // a)物理内存信息
  Sigar sigar = new Sigar();
  Mem mem;
  try {
   mem = sigar.getMem();
   // 内存总量
   HardWareRes.put("Memory_Total","" + mem.getTotal() / 1024L + "K av");
   // 当前内存使用量
   HardWareRes.put("Memory_Used", mem.getUsed() / 1024L + "K used");
   // 当前内存剩余量
   HardWareRes.put("Memory_Free", mem.getFree() / 1024L + "K free");
   // b)系统页面文件交换区信息
   Swap swap = sigar.getSwap();
   // 交换区总量
   HardWareRes.put("Memory_Swap_Total", swap.getTotal() / 1024L + "K av");
   // 当前交换区使用量
   HardWareRes.put("Memory_Swap_Used",  swap.getUsed() / 1024L + "K used");
   // 当前交换区剩余量
   HardWareRes.put("Memory_Swap_Free",  swap.getFree() / 1024L + "K free");
  } catch (SigarException e) {
   e.printStackTrace();
  }
 }
 // 3.操作系统信息
 // a)取到当前操作系统的名称：
 public static void getPlatformName() {
  String hostname = "";
  try {
   hostname = InetAddress.getLocalHost().getHostName();
  } catch (Exception exc) {
   Sigar sigar = new Sigar();
   try {
    hostname = sigar.getNetInfo().getHostName();
   } catch (SigarException e) {
    hostname = "localhost.unknown";
   } finally {
    sigar.close();
   }
  }
  HardWareRes.put("System_Name", hostname);
 }
 // b)取当前操作系统的信息
 public static void testGetOSInfo() {
  OperatingSystem OS = OperatingSystem.getInstance();
  // 操作系统内核类型如： 386、486、586等x86
  HardWareRes.put("OS.getArch",OS.getArch());
		  HardWareRes.put("OS.getCpuEndian",OS.getCpuEndian());//
  HardWareRes.put("OS.getDataModel" , OS.getDataModel());//
  // 系统描述
  HardWareRes.put("OS.getDescription" ,OS.getDescription());
  HardWareRes.put("OS.getMachine" ,OS.getMachine());//
  // 操作系统类型
  HardWareRes.put("OS.getName" ,OS.getName());
  HardWareRes.put("OS.getPatchLevel" ,OS.getPatchLevel());//
  // 操作系统的卖主
  HardWareRes.put("System_Vendor", OS.getVendor());
  // 卖主名称
  HardWareRes.put("OS.getVendorCodeName" , OS.getVendorCodeName());
  // 操作系统名称
  HardWareRes.put("OS.getVendorName", OS.getVendorName());
  // 操作系统卖主类型
  HardWareRes.put("OS.getVendorVersion" , OS.getVendorVersion());
  // 操作系统的版本号
  HardWareRes.put("OS.getVersion" ,OS.getVersion());
 }
 // c)取当前系统进程表中的用户信息
 public static void testWho() {
  try {
   Sigar sigar = new Sigar();
   org.hyperic.sigar.Who[] who = sigar.getWhoList();
   StringBuilder sb = new StringBuilder();
   if (who != null && who.length > 0) {
    for (int i = 0; i < who.length; i++) {
    	org.hyperic.sigar.Who _who = who[i];
     sb.append(	 "|id    |"+i
    		 	+"|device|"+_who.getDevice()
    		    +"|host  |"+ _who.getHost()
    		    +"|time  |"+ _who.getTime()
    		    +"|user  |"+_who.getUser()// 当前系统进程表中的用户名
    		    );
    }
   }
	HardWareRes.put("RegUser",sb.toString());
  } catch (SigarException e) {
   e.printStackTrace();
  }
 }
 // 4.资源信息（主要是硬盘）
 // a)取硬盘已有的分区及其详细信息（通过sigar.getFileSystemList()来获得FileSystem列表对象，然后对其进行编历）：
 public static void testFileSystemInfo() throws Exception {
	 StringBuilder sb = new StringBuilder();
  Sigar sigar = new Sigar();
  FileSystem fslist[] = sigar.getFileSystemList();
  //String dir = System.getProperty("user.home");// 当前用户文件夹路径
  for (int i = 0; i < fslist.length; i++) {
   FileSystem fs = fslist[i];
   /* 分区的盘符名称
    分区的文件夹名称
    文件系统类型，比如 FAT32、NTFS
 文件系统类型名，比如本地硬盘、光驱、网络文件系统等*/
   sb.append("|DevName     |" + fs.getDevName()
		    +"|DirName     |" + fs.getDirName()
		    +"|Flags       |"+ fs.getFlags()
   			+"|SysTypeName |" + fs.getSysTypeName()
   			+"|TypeName    |" + fs.getTypeName()
   			+"|Type	       |" + fs.getType());
   FileSystemUsage usage = null;
   try {
    usage = sigar.getFileSystemUsage(fs.getDirName());
   } catch (SigarException e) {
    if (fs.getType() == 2)
     throw e;
    continue;
   }
   switch (fs.getType()) {
   case 0: // TYPE_UNKNOWN ：未知
    break;
   case 1: // TYPE_NONE
    break;
   case 2: // TYPE_LOCAL_DISK : 本地硬盘
	    // 文件系统总大小
	    // 文件系统剩余大小
	    // 文件系统可用大小
	    // 文件系统已经使用量
	    // 文件系统资源的利用率
	double usePercent = usage.getUsePercent() * 100D;
    sb.append("|Total  |" + usage.getTotal() + "KB"

     		 +"|Free   |" + usage.getFree() + "KB"

             +"|Avail  |" + usage.getAvail() + "KB"

             +"|Used   |" + usage.getUsed() + "KB"
    		 +"|Usage  |" + usePercent + "%");
    break;
   case 3:// TYPE_NETWORK ：网络
    break;
   case 4:// TYPE_RAM_DISK ：闪存
    break;
   case 5:// TYPE_CDROM ：光驱
    break;
   case 6:// TYPE_SWAP ：页面交换
    break;
   }
   sb.append("|DiskReads  |" + usage.getDiskReads()
		   	+"|DiskWrites |" + usage.getDiskWrites());
  }
  HardWareRes.put("Disk_info", sb.toString());
  return;
 }
 // 5.网络信息
 // a)当前机器的正式域名
 public static String getFQDN() {
  Sigar sigar = null;
  try {
   return InetAddress.getLocalHost().getCanonicalHostName();
  } catch (UnknownHostException e) {
   try {
    sigar = new Sigar();
    return sigar.getFQDN();
   } catch (SigarException ex) {
    return null;
   } finally {
    sigar.close();
   }
  }
 }
 // b)取到当前机器的IP地址
 public static String getDefaultIpAddress() {
  String address = null;
  try {
   address = InetAddress.getLocalHost().getHostAddress();
   // 没有出现异常而正常当取到的IP时，如果取到的不是网卡循回地址时就返回
   // 否则再通过Sigar工具包中的方法来获取
   if (!NetFlags.LOOPBACK_ADDRESS.equals(address)) {
    return address;
   }
  } catch (UnknownHostException e) {
   // hostname not in DNS or /etc/hosts
  }
  Sigar sigar = new Sigar();
  try {
   address = sigar.getNetInterfaceConfig().getAddress();
  } catch (SigarException e) {
   address = NetFlags.LOOPBACK_ADDRESS;
  } finally {
   sigar.close();
  }
  return address;
 }
 // c)取到当前机器的MAC地址
 public static String getMAC() {
  Sigar sigar = null;
  try {
   sigar = new Sigar();
   String[] ifaces = sigar.getNetInterfaceList();
   String hwaddr = null;
   for (int i = 0; i < ifaces.length; i++) {
    NetInterfaceConfig cfg = sigar.getNetInterfaceConfig(ifaces[i]);
    if (NetFlags.LOOPBACK_ADDRESS.equals(cfg.getAddress())
      || (cfg.getFlags() & NetFlags.IFF_LOOPBACK) != 0
      || NetFlags.NULL_HWADDR.equals(cfg.getHwaddr())) {
     continue;
    }
    /*
     * 如果存在多张网卡包括虚拟机的网卡，默认只取第一张网卡的MAC地址，如果要返回所有的网卡（包括物理的和虚拟的）则可以修改方法的返回类型为数组或Collection
     * ，通过在for循环里取到的多个MAC地址。
     */
    hwaddr = cfg.getHwaddr();
    break;
   }
   return hwaddr != null ? hwaddr : null;
  } catch (Exception e) {
   return null;
  } finally {
   if (sigar != null)
    sigar.close();
  }
 }
 // d)获取网络流量等信息
 public static void testNetIfList() throws Exception {
  Sigar sigar = new Sigar();
  String ifNames[] = sigar.getNetInterfaceList();
  for (int i = 0; i < ifNames.length; i++) {
   String name = ifNames[i];
   NetInterfaceConfig ifconfig = sigar.getNetInterfaceConfig(name);
  //HardWareRes.put("NetWork_Name",name);// 网络设备名
  //HardWareRes.put("NetWork_IP",ifconfig.getAddress());// IP地址
  //HardWareRes.put("NetWork_NetMask",ifconfig.getNetmask());// 子网掩码
   if ((ifconfig.getFlags() & 1L) <= 0L) {
    print("!IFF_UP...skipping getNetInterfaceStat");
    continue;
   }
   try {
    NetInterfaceStat ifstat = sigar.getNetInterfaceStat(name);
    HardWareRes.put("NetWork_RxPackets",""+ifstat.getRxPackets());// 接收的总包裹数
    HardWareRes.put("NetWork_TxPackets",""+ifstat.getTxPackets());// 发送的总包裹数
    HardWareRes.put("NetWork_RxBytes",""+ifstat.getRxBytes());// 接收到的总字节数
    HardWareRes.put("NetWork_TxBytes",""+ifstat.getTxBytes());// 发送的总字节数
    HardWareRes.put("NetWork_RxErrors",""+ifstat.getRxErrors());// 接收到的错误包数
    HardWareRes.put("NetWork_TxErrors",""+ifstat.getTxErrors());// 发送数据包时的错误数
    HardWareRes.put("NetWork_RxDropped",""+ifstat.getRxDropped());// 接收时丢弃的包数
    HardWareRes.put("NetWork_TxDropped",""+ifstat.getTxDropped());// 发送时丢弃的包数
   } catch (SigarNotImplementedException e) {
   } catch (SigarException e) {
    print(e.getMessage());
   }
  }
 }
 static void print(String msg) {
  System.out.println(msg);
 }
 // e)一些其他的信息
 public static void getEthernetInfo() {
  Sigar sigar = null;
  try {
   sigar = new Sigar();
   String[] ifaces = sigar.getNetInterfaceList();
   for (int i = 0; i < ifaces.length; i++) {
    NetInterfaceConfig cfg = sigar.getNetInterfaceConfig(ifaces[i]);
    if (NetFlags.LOOPBACK_ADDRESS.equals(cfg.getAddress())
      || (cfg.getFlags() & NetFlags.IFF_LOOPBACK) != 0
      || NetFlags.NULL_HWADDR.equals(cfg.getHwaddr())) {
     continue;
    }
    System.out.println("cfg.getAddress() = " + cfg.getAddress());// IP地址
    System.out
      .println("cfg.getBroadcast() = " + cfg.getBroadcast());// 网关广播地址
    System.out.println("cfg.getHwaddr() = " + cfg.getHwaddr());// 网卡MAC地址
    System.out.println("cfg.getNetmask() = " + cfg.getNetmask());// 子网掩码
    System.out.println("cfg.getDescription() = "
      + cfg.getDescription());// 网卡描述信息
    System.out.println("cfg.getType() = " + cfg.getType());//
    System.out.println("cfg.getDestination() = "
      + cfg.getDestination());
    System.out.println("cfg.getFlags() = " + cfg.getFlags());//
    System.out.println("cfg.getMetric() = " + cfg.getMetric());
    System.out.println("cfg.getMtu() = " + cfg.getMtu());
    System.out.println("cfg.getName() = " + cfg.getName());
    System.out.println();
   }
  } catch (Exception e) {
   System.out.println("Error while creating GUID" + e);
  } finally {
   if (sigar != null)
    sigar.close();
  }
 }
	public static byte[] serialize(HashMap<String, String> hashMap){ 
        try { 
        ByteArrayOutputStream mem_out = new ByteArrayOutputStream(); 
            ObjectOutputStream out = new ObjectOutputStream(mem_out); 
 
            out.writeObject(hashMap); 
 
            out.close(); 
           mem_out.close(); 
 
           byte[] bytes =  mem_out.toByteArray(); 
           return bytes; 
        } catch (IOException e) { 
            return null; 
        } 
    }
    public static HashMap<String, String> deserialize(byte[] bytes){ 
        try { 
            ByteArrayInputStream mem_in = new ByteArrayInputStream(bytes); 
            ObjectInputStream in = new ObjectInputStream(mem_in); 
 
            HashMap<String, String> hashMap = (HashMap<String, String>)in.readObject(); 
 
             in.close(); 
             mem_in.close(); 
 
             return hashMap; 
        } catch (StreamCorruptedException e) { 
            return null; 
        } catch (ClassNotFoundException e) { 
            return null; 
        }   catch (IOException e) { 
            return null; 
        } 
     }
 public byte[]  GetSysInfoBytes(){
	return serialize(HardWareRes);
 }
 public HashMap<String,String> GetSysInfoMap(byte[] b){
	 return deserialize(b);
 }
 public HashMap<String,String> GetSysInfoMap(){
	 return HardWareRes;
 }
}

