package com.intel.fangpei.Schema;

import java.text.DateFormat;
import java.util.Date;
import java.util.Random;

public class GenerateFunction {
	Random r = new Random();

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
		DateFormat df = DateFormat.getDateInstance();
		Date date = new Date();
		return "" + df.format(date);
	}

	public String GenRandom2() {
		Date date = new Date();
		return "" + date.getYear();
	}

	public String GenRandom3() {
		Date date = new Date();
		return "" + date.getMonth();
	}

	public String GenRandom4() {
		Date date = new Date();
		return "" + date.getDay();
	}

	public String GenRandom5() {
		Random r = new Random();
		return "" + r.nextInt(100);
	}

	public String GenRandom6() {
		Random r = new Random();
		return "" + r.nextInt(100000);
	}
}
