package com.intel.fangpei.task.handler.example;

import java.text.DateFormat;
import java.util.Date;
import java.util.Random;

public class GenerateFunction {
	Random r = new Random();
	String h1 ="135";
	String h2 ="117";
	String h3 ="158";
	static String pp = "ABCDEFG";
	byte[] b = pp.getBytes();
	public static void main(String args[]){

	}
	public String Gen(int num) {
		switch (num) {
		case 1:
			return "" + GenRandom1();
		case 2:
			return "" + GenRandom2();
		case 3:
			return "" + GenRandom3();
		case 4:
			return "" + GenRandom4();
		case 5:
			return "" + GenRandom5();
		case 6:
			return "" + GenRandom6();
		default:
			return "";
		}
	}

	public String GenRandom1() {
//		DateFormat df = DateFormat.getDateInstance();
//		Date date = new Date();
//		return "" + df.format(date);
		int x = r.nextInt(999);
		if(x <100){
			x = 100+x;
		}
		int y = r.nextInt(999);
		if(y <100){
			y = 100+y;
		}
		int z = r.nextInt(999);
		if(z <100){
			z = 100+z;
		}
		int c = x%3;
		if(c == 0){
			return h1+x+""+y+""+z;
		}
		if(c == 1){
			return h2+x+""+y+""+z;
		}
		return h3+x+""+y+""+z;
	}

	public String GenRandom2() {
		int x = r.nextInt(127);
		int y = r.nextInt(6);
		b[y] = (byte)x;
		return new String(b);
		
	}

	public String GenRandom3() {
		Date date = new Date();
		return "" + date.toGMTString();
	}

	public String GenRandom4() {
		return ""+r.nextInt(100);
	}

	public String GenRandom5() {
		return "hello world!";
	}

	public String GenRandom6() {
		Random r = new Random();
		return "" + r.nextInt(100000);
	}
}
