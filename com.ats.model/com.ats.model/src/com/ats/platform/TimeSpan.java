package com.ats.platform;

public enum TimeSpan {
	sec1( 1, "20 S", 1),
	sec5( 2, "75 S", 5),
	sec15( 3, "180 S", 15),
	sec30( 4, "1 D", 30),
	min1( 5, "1 D", 60),
	min2( 6, "1 D", 120),
	min5( 7, "1 D", 300),
	min15( 8, "1 D", 900),
	min30( 9, "2 D", 1800),
	hourly( 10, "4 D", 3600),
	daily( 11, "3 W", 24*3600),
	unknown(-1, "", -1);
	
	private int ibBarParam;
	private String ibDuration;
	private int spanInSecs;
	
	private TimeSpan(int param, String ibDur, int span) {
		this.ibBarParam = param;
		this.ibDuration = ibDur;
		this.spanInSecs = span;
	}

	public int getIbBarParam() {
		return ibBarParam;
	}

	public String getIbDuration() {
		return ibDuration;
	}

	public int getSpanInSecs() {
		return spanInSecs;
	}
	
	public int getSpanInMillis() {
		return spanInSecs * 1000;
	}
	
}
