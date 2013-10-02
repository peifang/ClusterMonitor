package com.intel.fangpei.IDHUtil;

public class IDHSummary {
/*
 * Dirs that is used by IDH,we must check every dirs is created and have
 * the right access rules especially the dirs under /hadoop/.
 */
public static final String root = "/hadoop";
public static final String data = "/data";
public static final String drbd = "/drbd";
public static final String hadoop_image_local = "/hadoop_image_local";
public static final String mapred = "/mapred";

/*
 * Check Disks that is used by IDH . we also need to confirm that 
 * the system disk is not full. We should also warn the admin if
 * the monitor logs is to large. 
 */

/*
 * Service name .
 * 
 */
public static final String zookeeper = "zookeeper-server";
public static final String datanode = "hadoop-datanode";
public static final String namenode = "hadoop-namenode";

/*
 * log directory
 */
public static final String hadooplog = "/var/log/hadoop";

	
}
