package com.ats.engine;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.ats.engine.JExecution;
import com.ats.platform.Bar;
import com.ats.platform.Instrument;
import com.ats.platform.OrderSide;
import com.ats.platform.Trade;
import com.ats.platform.Position.PositionSide;
import com.ats.utils.Utils;

public class TradeSummary implements TickListener, Comparable {
	private List<JExecution> executions = new ArrayList<JExecution>();
	private PositionSide side = PositionSide.FLAT;
	private PositionSide entrySide;
	private int totalBuyQty;
	private double avgBuyPrice;
	private int totalSellQty;
	private double avgSellPrice;
	
	// summary properties
	private double lastPrice;  // for unrealized profit
	private double highPriceFromEntry;
	private double lowPriceFromEntry;
	
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append("entrySide", entrySide)
			.append("side", side)
			.append("totalBuyQty", totalBuyQty)
			.append("totalSellQty", totalSellQty)
			.toString();
	}
	
	public int compareTo(Object obj) {
		int ret = -1;
		if( obj instanceof TradeSummary ) {
			TradeSummary that = (TradeSummary)obj;
			ret = this.getBeginDate().compareTo(that.getBeginDate());
		}
		return ret;
	}
	

	public void onTrade(Trade trade) {
		lastPrice = trade.getPrice();
		if( highPriceFromEntry < lastPrice ) {
			highPriceFromEntry = lastPrice;
		}
		if( lowPriceFromEntry > lastPrice ) {
			lowPriceFromEntry = lastPrice;
		}
	}
	
	public double getUnrealizedProfit() {
		if( side == PositionSide.LONG ) {
			return (totalBuyQty - totalSellQty)*(lastPrice - avgBuyPrice);
		} else if( side == PositionSide.SHORT ) {
			return (totalSellQty - totalBuyQty) * (avgSellPrice - lastPrice);
		}
		return 0;
	}
	

	
	public double getMaxDrawdown() {
		// TODO: should follow the drawdown as the trade progresses.  This is an
		// inaccurate approximation
		double drawdown = 0;
		if( entrySide == PositionSide.LONG ) {
			drawdown = (avgBuyPrice - lowPriceFromEntry) * totalBuyQty;
		} else {
			drawdown = (highPriceFromEntry - avgSellPrice) * totalBuyQty;
		}
		return drawdown;
	}
	
	public double getLossFromTop() {
		double lft = 0;
		if( entrySide == PositionSide.LONG ) {
			lft = (highPriceFromEntry - avgSellPrice) * totalBuyQty;
		} else {
			lft = (avgBuyPrice - lowPriceFromEntry) * totalBuyQty;
		}
		return lft;
	}
	
	public boolean isComplete() {
		return executions.size() > 0 && side == PositionSide.FLAT;
	}
	
	public Date getBeginDate() {
		return executions.get(0).getDateTime();
	}
	public Date getEndDate() {
		return executions.get(executions.size()-1).getDateTime();
	}
	public Instrument getInstrument() {
		return executions.get(0).getInstrument();
	}
	
	public double getRealizedPnL( ) {
		int realizedSize = Math.min(totalBuyQty, totalSellQty);
		return (avgSellPrice - avgBuyPrice) * realizedSize * executions.get(0).getInstrument().getMultiplier();
	}
	
	public double getRealizedNetPnL() {
		double grossPnL = getRealizedPnL();
		double commission = 0;
		// calc comission
		if (Utils.getPreferenceStore().getBoolean(Utils.COMMISSION_SHARE)) {
			commission = (getTotalBuyQty() + getTotalSellQty())
					* Utils.getPreferenceStore().getDouble(
							Utils.COMMISSION_SHARE_VALUE);
		} else if (Utils.getPreferenceStore()
				.getBoolean(Utils.COMMISSION_ORDER)) {
			commission = 1 * Utils.getPreferenceStore().getDouble(
					Utils.COMMISSION_ORDER_VALUE);
		} else if (Utils.getPreferenceStore()
				.getBoolean(Utils.COMMISSION_TRANS)) {
			commission = (getAvgBuyPrice() * getTotalBuyQty() + getAvgSellPrice()
					* getTotalSellQty())
					* Utils.getPreferenceStore().getDouble(
							Utils.COMMISSION_TRANS_VALUE) / 100;
		}
		double netPnL = grossPnL - commission;
		return netPnL;
	}
	
	public JExecution addExecutionIfPossible(JExecution exec) {
		if( isComplete() ) {
			// full up
			return exec;
		}
		JExecution ret = null;
		if( executions.size() == 0 ) {
			// brand new
			executions.add(exec);
			Factory.getInstance().getDataManager().addTickListener(exec.getInstrument(), this);
			if( exec.getSide() == OrderSide.BUY ) {
				side = PositionSide.LONG;
				entrySide = side;
				totalBuyQty = exec.getQuantity();
				avgBuyPrice = exec.getPrice();
			} else {
				side = PositionSide.SHORT;
				entrySide = side;
				totalSellQty = exec.getQuantity();
				avgSellPrice = exec.getPrice();
			}
		} else {
			// adding to existing trade
			
			// check for overflow
			if( ( exec.getSide() == OrderSide.BUY && side == PositionSide.SHORT )
					|| (exec.getSide() == OrderSide.SELL && side == PositionSide.LONG ) ) {
				int netSize = Math.abs(totalBuyQty - totalSellQty); 
				if( netSize < exec.getQuantity() ) {
					// overflow, so create new execution to return and
					// give it all size not used in current trade
					ret = (JExecution)exec.clone();
					ret.setQuantity(exec.getQuantity() - netSize);
					
					exec.setQuantity(netSize);
				}
			}
			
			// add execution to current trade
			executions.add(exec);
			double orderValue = exec.getPrice() * (double)exec.getQuantity();
			if( exec.getSide() == OrderSide.BUY ) {
				avgBuyPrice = ((avgBuyPrice * (double)totalBuyQty) + orderValue)/(double)(totalBuyQty + exec.getQuantity());
				totalBuyQty += exec.getQuantity();
			} else {
				avgSellPrice = ((avgSellPrice * (double)totalSellQty) + orderValue)/(double)(totalSellQty + exec.getQuantity());
				totalSellQty += exec.getQuantity();
			}
			if( totalSellQty == totalBuyQty ) {
				side = PositionSide.FLAT;
				Factory.getInstance().getDataManager().removeTickListener(exec.getInstrument(), this);
			}
		}
		return ret;
	}

	public double getAvgBuyPrice() {
		return avgBuyPrice;
	}

	public void setAvgBuyPrice(double avgBuyPrice) {
		this.avgBuyPrice = avgBuyPrice;
	}

	public double getAvgSellPrice() {
		return avgSellPrice;
	}

	public void setAvgSellPrice(double avgSellPrice) {
		this.avgSellPrice = avgSellPrice;
	}

	public List<JExecution> getExecutions() {
		return executions;
	}

	public void setExecutions(List<JExecution> executions) {
		this.executions = executions;
	}

	public PositionSide getSide() {
		return side;
	}

	public void setSide(PositionSide side) {
		this.side = side;
	}

	public int getTotalBuyQty() {
		return totalBuyQty;
	}

	public void setTotalBuyQty(int totalBuyQty) {
		this.totalBuyQty = totalBuyQty;
	}

	public int getTotalSellQty() {
		return totalSellQty;
	}

	public void setTotalSellQty(int totalSellQty) {
		this.totalSellQty = totalSellQty;
	}

	public void onBar(Bar bar) {
		// do nothing
	}
	public void onBarOpen(Bar bar) {
		// do nothing
	}

	public double getHighPriceFromEntry() {
		return highPriceFromEntry;
	}

	public void setHighPriceFromEntry(double highPriceFromEntry) {
		this.highPriceFromEntry = highPriceFromEntry;
	}

	public double getLastPrice() {
		return lastPrice;
	}

	public void setLastPrice(double lastPrice) {
		this.lastPrice = lastPrice;
	}

	public double getLowPriceFromEntry() {
		return lowPriceFromEntry;
	}

	public void setLowPriceFromEntry(double lowPriceFromEntry) {
		this.lowPriceFromEntry = lowPriceFromEntry;
	}

	public PositionSide getEntrySide() {
		return entrySide;
	}

	public void setEntrySide(PositionSide entrySide) {
		this.entrySide = entrySide;
	}
}
