package com.intel.fangpei.for_test_code;

import java.nio.ByteBuffer;

public class test {
	public static void main(String args[]) {
		ByteBuffer bb = ByteBuffer.allocate(1024);
		bb.putInt(123);
		System.out.println(bb.mark() + " " + bb.limit() + " " + bb.remaining());
		bb.putInt(56);
		System.out.println(bb.mark() + " " + bb.limit() + " " + bb.remaining());
		bb.put((byte) 1);
		System.out.println(bb.mark() + " " + bb.limit() + " " + bb.remaining());
		bb.flip();
		System.out.println(bb.mark() + " " + bb.limit() + " " + bb.remaining());

		
		System.out.println(bb.getInt());
		System.out.println(bb.mark() + " " + bb.limit() + " " + bb.remaining());
		System.out.println(bb.getInt());
		//byte[] b = new byte[bb.remaining()];
		//bb.get(b, 0, bb.remaining());
		bb.rewind();
		//bb.put(b);
		System.out.println(bb.mark() + " " + bb.limit() + " " + bb.remaining()
				+ " " + bb.arrayOffset());
		bb.putInt(123);
		System.out.println(bb.mark() + " " + bb.limit() + " " + bb.remaining()
				+ " " + bb.arrayOffset());
	}

}
