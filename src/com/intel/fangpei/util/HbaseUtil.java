package com.intel.fangpei.util;

import java.io.IOException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.io.hfile.Compression.Algorithm;
import org.apache.hadoop.hbase.regionserver.StoreFile.BloomType;

import com.intel.hadoop.hbase.util.HBaseUtility;

public class HbaseUtil {
	private String HtableName = "";
	private String Compress = null;
	private String Bloomfilter = null;

	public HbaseUtil(String HtableName, String Compress, String Bloomfilter) {
		this.Compress = Compress;
		this.Bloomfilter = Bloomfilter;
		this.HtableName = HtableName;
	}

	public HbaseUtil(String HtableName) {
		this.HtableName = HtableName;
	}

	private HTableDescriptor CreateColumnsFamily(String[] cfs) {
		int cfnum = cfs.length;
		HTableDescriptor htd = new HTableDescriptor(HtableName);
		HColumnDescriptor[] cd = new HColumnDescriptor[cfnum];
		for (int i = 0; i < cfnum; i++) {
			System.out.println("new columns:" + cfs[i]);
			cd[i] = new HColumnDescriptor(cfs[i].trim());
			if (Compress.equals("y") || Compress.equals("Y"))
				cd[i].setCompressionType(Algorithm.SNAPPY);
			if (Bloomfilter.equals("y") || Bloomfilter.equals("Y"))
				cd[i].setBloomFilterType(BloomType.ROW);
			cd[i].setReplication((short) 3);
			htd.addFamily(cd[i]);
		}
		return htd;
	}

	public boolean CreateTable(String[] cfs) {
		HTableDescriptor htable = CreateColumnsFamily(cfs);
		try {
			HBaseUtility.create().getHBaseAdmin().createTable(htable);

			return true;
		} catch (IOException e) {

			e.printStackTrace();
			return false;
		}

	}

	public boolean CreateTableWithPreRegion(String[] cfs, byte[][] b) {
		HTableDescriptor htable = CreateColumnsFamily(cfs);
		try {
			HBaseUtility.create().getHBaseAdmin().createTable(htable, b);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public static String[] GetUserInputData_CreateTable() {
		byte[] b = new byte[1024];
		String[] htableinfo = new String[3];
		for (int i = 0; i < 3; i++) {
			htableinfo[i] = null;
		}
		try {
			System.out.print("please input Htable name:\n");
			System.in.read(b);
			htableinfo[0] = new String(b).trim();
			b = new byte[1024];
			System.out.print("need compress?(Y or N):\n");
			System.in.read(b);
			htableinfo[1] = new String(b).trim();
			b = new byte[1024];
			System.out.print("need bloomfilter?(Y or N):\n");
			System.in.read(b);
			htableinfo[2] = new String(b).trim();
			b = new byte[1024];
			System.out
					.print("for create table "
							+ htableinfo[0]
							+ ",please input the "
							+ "column family\n(mutilple column family use space to split):\n");
			System.in.read(b);
			String[] alldatas = (String[]) ArrayUtils.addAll(htableinfo,
					new String(b).trim().split(" "));
			return alldatas;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static boolean StartTask(String[] s) {
		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
