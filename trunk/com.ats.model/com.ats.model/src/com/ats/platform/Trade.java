package com.ats.platform;

import java.util.Date;

public class Trade {
	private Date time;
	private double price;
	private int size;
	private Instrument instrument;
	
	
	public Trade(Instrument instrument, Date time, double price, int size) {
		super();
		this.instrument = instrument;
		this.time = time;
		this.price = price;
		this.size = size;
	}
	
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public Date getDateTime() {
		return time;
	}
	public void setDateTime(Date time) {
		this.time = time;
	}

	public Instrument getInstrument() {
		return instrument;
	}
	
	
}
