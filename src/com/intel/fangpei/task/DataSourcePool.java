/*
 * This is the pool for @GeneDataHandleable to store it's data and @GeneDataTask 
 * Thread need to pass this object to putTask's putThreads for they can
 * store the data in the pool into Hbase or hive or other data container. 
 */
package com.intel.fangpei.task;

import java.util.ArrayList;
import java.util.LinkedList;

public class DataSourcePool<type extends Object> {
	LinkedList<type> source = new LinkedList<type>();
	private long maxSize = 0;

	public DataSourcePool() {
		maxSize = Long.MAX_VALUE;
	}

	public DataSourcePool(long maxSize) {
		this.maxSize = maxSize;
	}

	public boolean putone(type put) {
		if(put == null){
			return false;
		}
		synchronized (source) {
			if (isfull()) {
				source.notify();
				return false;
			}
			source.addLast(put);
			source.notify();
			return true;
		}
	}

	public type getone() {
		synchronized (source) {
			if (source.isEmpty()) {
				source.notify();
				return null;
			}
			type put = source.removeFirst();
			source.notify();
			return put;
		}
	}

	public ArrayList<type> gets(int num) {
		synchronized (source) {
			if (source.size() < num) {
				source.notify();
				return null;
			}
			ArrayList<type> list = new ArrayList<type>(num);
			while (num-- > 0)
				list.add(source.removeFirst());
			source.notify();
			return list;
		}
	}

	public boolean isEmpty() {
		return source.isEmpty();
	}

	public boolean isfull() {
		return (source.size() >= maxSize);
	}

	public long size() {
		// TODO Auto-generated method stub
		return source.size();
	}
	public long maxSize(){
		return maxSize;
	}
}
