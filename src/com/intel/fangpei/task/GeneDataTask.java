package com.intel.fangpei.task;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import com.intel.fangpei.BasicMessage.Bash;
import com.intel.fangpei.logfactory.MonitorLog;
import com.intel.fangpei.task.handler.GeneDataHandleable;
import com.intel.fangpei.task.handler.HBaseGeneDataHandler;

public abstract class GeneDataTask<type extends Object> implements Task{
	protected MonitorLog ml = null;
    protected DataSourcePool<type>[] pools = null;
    protected GeneDataHandleable<type> genehandler[] = null;
    protected int threadsNum = 0;
    protected int reportwindow = 0;
    protected int DataSourcePoolNum = 0;
    protected int GeneDatathreads_forOnePool = 0;
	/*
	 * define a collection as the container of all the gene puts. the type of
	 * the collection's elements should be type.
	 */
	File GenDataSchame = null;
	private long genNum = 0;

	public GeneDataTask(File GenDataSchame, long genNum) {
		this.GenDataSchame = GenDataSchame;
		this.genNum = genNum;
	}
	public long getGeneNum(){
		return genNum;
	}
	public DataSourcePool<type>[] getPools(){
		return pools;
	}
	/*@Override
	public void run() {
		// can change to hivegeneDataHandler;
		geneHandle = new HBaseGeneDataHandler(sourcepool, 1000000);
		new Thread((Runnable) geneHandle).start();
		/*
		 * HTablePool _tablePool = new
		 * HTablePool(HBaseUtility.create().getConfiguration(),
		 * Integer.MAX_VALUE); HTableInterface table =
		 * (HTable)_tablePool.getTable(tableName); BaseTableGenerator btc = new
		 * BaseTableGenerator(); try { int onceput = 10000; long putNum =
		 * genNum/onceput; for(int i =0;i<putNum;i++){ ArrayList<Put> _pt = new
		 * ArrayList<Put>(); for(int j=0;j<10000;j++){
		 * _pt.add(btc.generatePut(0)); } table.put(_pt); } _tablePool.close();
		 * } catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
	//}

	// 以下为测试的put代码
	public class BaseTableGenerator {
		long basetime = Long.parseLong("1357549800098");
		long seconds = 15 * 24 * 60 * 60 * 1000;
		String alltelnum = "";
		String date = null;
		String call_type_id = "";
		String calling = "";
		String called = "";
		String opc = "";
		String dpc = "";
		String o_prov_id = "";
		String d_prov_id = "";
		String o_city_id = "";
		String d_city_id = "";
		String o_oper_id = "";
		String d_oper_id = "";
		String call_result = null;
		String simi = null;
		String charge_type = "";
		String result_num = "";
		String restime_duration = "";
		String seize_duration = "";
		String talk_duration = "";

		private class ClumData {
			private String[] data = null;

			public ClumData() {
				String start_time = date;
				String call_controltype = "1";
				call_type_id = "" + callType();
				String s_ip = "169759234";
				String d_ip = "1577983";
				long l = random();
				opc = "" + l;
				dpc = "" + (l + 15);
				o_oper_id = "" + l % 10;
				d_oper_id = "" + (l % 10 + 3);
				String trunkgrp_id = "0";
				charge_type = "" + l % 5;
				call_result = "" + l % 200;
				result_num = "6.0000000";
				seize_duration = "52552.0000000";
				talk_duration = "0.0000000";
				String maxtalk_duration = "0.0000000";
				restime_duration = "26618.0000000";
				String max_restime = "4969.0000000";
				String frontno = "92";
				String remoteno = "2";
				String[] oneraw = { start_time, call_controltype, call_type_id,
						s_ip, d_ip, opc, dpc, o_prov_id, d_prov_id, o_city_id,
						d_city_id, o_oper_id, d_oper_id, trunkgrp_id,
						charge_type, call_result, result_num, seize_duration,
						talk_duration, maxtalk_duration, restime_duration,
						max_restime, frontno, remoteno, };
				data = oneraw;
			}

			public String[] getData() {
				return data;
			}
		}

		private Date getRandomTime(int days) {
			Random r = new Random();
			// long seconds = days * 24 * 60 * 60*1000;已经成为全剧变量
			Date d = new Date();
			d.setTime(r.nextLong() % seconds + basetime);
			return d;
		}

		public Put generatePut(long arg0) {
			Random r = new Random();
			String calledNum = OtelphoneNumberGenerator();
			int calledRand = r.nextInt();
			if (calledRand % 4 == 0)
				called = "17951" + calledNum;
			else if (calledRand % 4 == 1)
				called = "0086" + calledNum;
			else
				called = calledNum;

			String telnum = DtelphoneNumberGenerator();
			int args = r.nextInt();
			if (args % 4 == 0)
				calling = "17951" + telnum;
			else if (args % 4 == 1)
				calling = "0086" + telnum;
			else
				calling = telnum;
			Date time = getRandomTime(30);
			SimpleDateFormat bartDateFormat = new SimpleDateFormat(
					"yyyyMMddHHmmsssss");
			date = bartDateFormat.format(time);
			simi = GetSimi(telnum, 0.05);
			String sData = "";
			ClumData cd = new ClumData();
			String[] data = cd.getData();
			int len = data.length;
			for (int i = 0; i < len - 1; i++) {
				sData += data[i] + "|";
			}
			sData += data[len - 1];
			Put p = new Put(Bytes.toBytes(RawKeyGen()));
			p.add("ALLDATA".getBytes(), "DATA".getBytes(), sData.getBytes());
			return p;
		}

		private String OtelphoneNumberGenerator() {
			Random r = new Random();
			int s3Num1 = 135;
			int s3Num2 = 127;
			int s3Num3 = 158;
			int s3Num4 = 139;
			int s3Num5 = 130;
			int s3Num6 = 138;
			int s3Num7 = 136;
			int s3Num8 = 159;
			int caseint = r.nextInt(8);
			switch (caseint) {
			case 0:
				return OtelphoneNumberGenerator(s3Num1);
			case 1:
				return OtelphoneNumberGenerator(s3Num2);
			case 2:
				return OtelphoneNumberGenerator(s3Num3);
			case 3:
				return OtelphoneNumberGenerator(s3Num4);
			case 4:
				return OtelphoneNumberGenerator(s3Num5);
			case 5:
				return OtelphoneNumberGenerator(s3Num6);
			case 6:
				return OtelphoneNumberGenerator(s3Num7);
			default:
				return OtelphoneNumberGenerator(s3Num8);
			}
		}

		private String DtelphoneNumberGenerator() {
			Random r = new Random();
			int s3Num1 = 135;
			int s3Num2 = 127;
			int s3Num3 = 158;
			int s3Num4 = 139;
			int s3Num5 = 130;
			int s3Num6 = 138;
			int s3Num7 = 136;
			int s3Num8 = 159;
			int caseint = r.nextInt(8);
			switch (caseint) {
			case 0:
				return DtelphoneNumberGenerator(s3Num1);
			case 1:
				return DtelphoneNumberGenerator(s3Num2);
			case 2:
				return DtelphoneNumberGenerator(s3Num3);
			case 3:
				return DtelphoneNumberGenerator(s3Num4);
			case 4:
				return DtelphoneNumberGenerator(s3Num5);
			case 5:
				return DtelphoneNumberGenerator(s3Num6);
			case 6:
				return DtelphoneNumberGenerator(s3Num7);
			default:
				return DtelphoneNumberGenerator(s3Num8);
			}
		}

		private String OtelphoneNumberGenerator(int first3Num) {
			Random r = new Random();
			int province = province();
			int city = city();
			int random4Num = r.nextInt(10000);
			String sfirst3Num = "" + first3Num;
			String srandom = null;
			if (province < 10)
				o_prov_id = "0" + province;
			else
				o_prov_id = "" + province;
			if (city < 10)
				o_city_id = "0" + city;
			else
				o_city_id = "" + city;
			if (random4Num < 10)
				srandom = "000" + random4Num;
			else if (random4Num < 100)
				srandom = "00" + random4Num;
			else if (random4Num < 1000)
				srandom = "0" + random4Num;
			else
				srandom = "" + random4Num;

			return sfirst3Num + o_prov_id + o_city_id + srandom;

		}

		private String DtelphoneNumberGenerator(int first3Num) {
			Random r = new Random();
			int province = province();
			int city = city();
			int random4Num = r.nextInt(10000);
			String sfirst3Num = "" + first3Num;
			String srandom = null;
			if (province < 10)
				d_prov_id = "0" + province;
			else
				d_prov_id = "" + province;
			if (city < 10)
				d_city_id = "0" + city;
			else
				d_city_id = "" + city;
			if (random4Num < 10)
				srandom = "000" + random4Num;
			else if (random4Num < 100)
				srandom = "00" + random4Num;
			else if (random4Num < 1000)
				srandom = "0" + random4Num;
			else
				srandom = "" + random4Num;

			return sfirst3Num + d_prov_id + d_city_id + srandom;

		}

		// 根据手机号码及开始通话时间生成RawKey
		private String RawKeyGen() {

			return date + "|" + call_type_id + "|" + o_prov_id + "|"
					+ o_city_id + "|" + o_oper_id + "|" + opc + "|" + d_prov_id
					+ "|" + d_city_id + "|" + d_oper_id + "|" + dpc + "|"
					+ charge_type + "|" + call_result + "|" + result_num + "|"
					+ restime_duration + "|" + seize_duration + "|"
					+ talk_duration;
		}

		private String GetSimi(String telNum, double imsiRandomPercentage) {
			Random r = new Random();
			Double simiDicider = r.nextDouble();

			SimpleDateFormat bartDateFormat = new SimpleDateFormat(
					"yyyyMMddHHmm");
			Date date = new Date();
			String stime = bartDateFormat.format(date); // 200810080913

			if (simiDicider < imsiRandomPercentage) {
				return stime + "000";
			} else {
				return telNum + "0000";
			}
		}

		private int ServiceProviderID() {
			return RandomNum(1, 4);
		}

		private int callType() {
			return RandomNum(0, 9);
		}

		private int province() {
			return RandomNum(0, 99);
		}

		private int city() {
			return RandomNum(0, 1000);
		}

		private int RandomNum(int from, int to) {
			Random r = new Random();
			return r.nextInt(to - from + 1) + from;
		}

		private long random() {
			Random r = new Random();
			return r.nextInt(9999999);
		}

	}

}
