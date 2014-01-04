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
 * ÓÐ´ýÍêÉÆ;
 */
public void removeNode(SelectionKey key){
	if(head.key == key){
		head = head.next;
		return;
	}
	packetNode p = head.next;
	while(p != null){
		if(p.key == key){
			
		}
	}
}
public int remain(){
	return len;
}
}
