package com.intel.fangpei.util;

public class Line<K,V> {
private Node head = null;
private Node end = null;
private int len =0;
private Object lock = new Object();

public class Node{
	K k = null;
	V v = null;
	Node next = null;
    Node(K k,V v) {
		this.k = k;
		this.v = v;
	}
}
public class segment{
	public segment(K k, V v) {
		this.k = k;
		this.v = v;
	}
	public K k = null;
	public V v = null;
}
private void addNode(Node node){
	//synchronized(lock){
	if(len == 0){
		head = end = node;
	}
	end.next = node;
	end = end.next;
	len++;
	//lock.notify();
	//}
}
public void addNode(K k,V v){
	addNode(new Node(k,v));
}
public segment popNode(){
	//synchronized(lock){
	if(len == 0){
		return null;
	}else{
		K k = head.k;
		V v =  head.v;
		head = head.next;
		len--;
		//lock.notify();
		return new segment(k,v);
	}
	//}
}
public boolean hasNext(){
	if(len == 0)
		return false;
	else{
		return true;
	}
}
public int remain(){
	return len;
}
}
