package com.ats.platform;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.ats.engine.JExecution;
import com.ats.engine.TradeSummary;

public class Position {
	private static final Logger logger = Logger.getLogger(Position.class);
	
	public enum PositionSide {
		LONG,
		SHORT,
		FLAT
	}
	
	private Strategy strategy;
	private double avgPrice;
	private Date entryDate;
	private double entryPrice;
	private int entryQty;
	private Instrument instrument;
	private int quantity;
	private PositionSide side = PositionSide.FLAT;
	private List<JExecution> executions = new ArrayList<JExecution>();
	private List<TradeSummary> tradeSummary = new ArrayList<TradeSummary>();
	
	public Position(Instrument instrument) {
		this.instrument = instrument;
		side = PositionSide.FLAT;
	}
	
	public String toString() {
		return "Position=[avgPrice=" + avgPrice + ", qty=" + quantity
				+ ", side=" + side
				+ ", entyDate=" + entryDate
				+ ", instrument=" + instrument + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Position)) {
			return false;
		}
		Position that = (Position)obj;
		
		boolean equals = this.instrument.equals(that.instrument)
			&& this.strategy.equals(that.strategy)
			&& this.entryDate.equals(that.entryDate)
			&& this.side.equals(that.side);
		return equals;
	}
	
	public List<JExecution> getExecutions() {
		return executions;
	}
	
	public List<TradeSummary> getTradeSummary() {
		return tradeSummary;
	}
	
	private void addExecutionToTradeSummary(JExecution exe) {
		TradeSummary trade = null;
		if( tradeSummary.size() > 0 ) {
			TradeSummary latest = tradeSummary.get(tradeSummary.size()-1);
			if( ! latest.isComplete() ) {
				trade = latest;
			}
		}
		if( trade == null ) {
			trade = new TradeSummary();
			tradeSummary.add(trade);
		}
		
		exe = trade.addExecutionIfPossible(exe);
		if( trade.isComplete() && exe != null ) {
			trade = new TradeSummary();
			trade.addExecutionIfPossible(exe);
			tradeSummary.add(trade);
		}
	}
	
	public synchronized void addExecution(JExecution execution) {
		executions.add(execution);
		addExecutionToTradeSummary(execution);
		TradeSummary trade = tradeSummary.get(tradeSummary.size()-1);
		
		avgPrice = trade.getEntrySide() == PositionSide.LONG ? trade.getAvgBuyPrice() : trade.getAvgSellPrice();
		quantity = Math.abs(trade.getTotalBuyQty() - trade.getTotalSellQty());
		entryDate = trade.getBeginDate();
		entryQty = trade.getExecutions().get(0).getQuantity();
		entryPrice = trade.getExecutions().get(0).getPrice();
		side = quantity == 0 ? PositionSide.FLAT : trade.getEntrySide();
	}
	
	public double getAvgPrice() {
		return avgPrice;
	}
	public void setAvgPrice(double amount) {
		this.avgPrice = amount;
	}
	public Date getEntryDate() {
		return entryDate;
	}
	public void setEntryDate(Date entryDate) {
		this.entryDate = entryDate;
	}
	public double getEntryPrice() {
		return entryPrice;
	}
	public void setEntryPrice(double entryPrice) {
		this.entryPrice = entryPrice;
	}
	public int getEntryQty() {
		return entryQty;
	}
	public void setEntryQty(int entryQty) {
		this.entryQty = entryQty;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public PositionSide getSide() {
		return side;
	}
	public void setSide(PositionSide side) {
		this.side = side;
	}
	public Instrument getInstrument() {
		return instrument;
	}
	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

}
