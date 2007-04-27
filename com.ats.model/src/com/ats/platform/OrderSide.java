package com.ats.platform;

public enum OrderSide {
	BUY("BUY"),
	SELL("SELL");
	
	private String ibCmd;
	
	private OrderSide(String cmd) {
		this.ibCmd = cmd;
	}
	public String getIbCmd() {
		return ibCmd;
	}
}
