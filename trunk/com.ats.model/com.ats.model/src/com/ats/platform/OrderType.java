package com.ats.platform;

public enum OrderType {
	market("MKT"),
	limit("LMT"),
	stop("STP"),
	stopLimit("STPLMT"),
	trailingStop("TRAIL");  // no support yet for MKTCLS, LMTCLS, REL, VWAP
	
	private String cmd;
	
	private OrderType(String cmd) {
		this.cmd = cmd;
	}

	public String getIbCmd() {
		return cmd;
	}
}
