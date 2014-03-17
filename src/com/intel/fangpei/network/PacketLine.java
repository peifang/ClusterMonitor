package com.intel.fangpei.network;

import java.nio.channels.SelectionKey;

import com.intel.fangpei.BasicMessage.packet;
/*
 *   <-end                         <-head
 *      |                              |
 *add-> -------------------------------- -> pop out
 * @author fangpei.fp
 *
 */
/***
 * this help class is a support for KeyHandle Class
 * @author fangpei.fp
 *
 */
public class PacketLine {
private packetNode head = null;
private packetNode end = null;
private int len =0;
private Object lock = new Object();

public class packetNode{
	SelectionKey key = null;
	packet p = null;
	packetNode next = null;
    packetNode(SelectionKey key,packet p) {
		this.key = key;
		this.p = p;
	}
}
public class segment{
	public segment(SelectionKey key, packet p) {
		this.key = key;
		this.p = p;
	}
	public SelectionKey key = null;
	public packet p = null;
}
private void addNode(packetNode node){
	synchronized(lock){
	if(len == 0){
		head = end = node;
		len++;
		lock.notify();
		return;
	}
	end.next = node;
	end = end.next;
	len++;
	lock.notify();
	}
}
public void addNode(SelectionKey key,packet p){
	addNode(new packetNode(key,p));
}
public segment popNode(){
	synchronized(lock){
	if(len == 0){
		return null;
	}else{
		SelectionKey key = head.key;
		packet p =  head.p;
		head = head.next;
		len--;
		lock.notify();
		return new segment(key,p);
	}
	}
}
public boolean hasNext(){
	if(len == 0)
		return false;
	else{
		return true;
	}
}
/*
 * remove all node which key is this key
 */
public void removeNode(SelectionKey key){
	synchronized(lock){
	while(head.key == key){
		head = head.next;
		len -- ;
	}
	if(head!=null&&head.next!=null){
	packetNode pre = head;
	packetNode pro = head.next;
	while(pro != null){
		if(pro.key == key){
			pre.next = pro.next;
			pro = pre.next;
			len --;
			continue;
		}
		pre = pro;
		pro = pro.next;
	}
	end = pre;
	}else{
	end = head;
	}
	lock.notifyAll();
	}
}
/**
 * pop the first packet of the key
 * @param key
 * @return
 */
public packet popNode(SelectionKey key){
	synchronized(lock){
	if(head.key == key){
		packet p = head.p;
		head = head.next;
		len -- ;
		lock.notifyAll();
		return p;
	}
	if(len > 1){
	packetNode pre = head;
	packetNode pro = head.next;
	while(pro != null){
		if(pro.key == key){
			packet p = pro.p;
			if(pro.next == null){
				end = pre;
			}
			pre.next = pro.next;
			len --;
			lock.notifyAll();
			return p;
		}
		pre = pro;
		pro = pro.next;
	}
	}
	lock.notifyAll();
	}
	return null;
}
public int remain(){
	return len;
}
}
