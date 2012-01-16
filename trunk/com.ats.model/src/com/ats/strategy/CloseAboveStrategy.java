package com.ats.strategy;

import org.apache.log4j.Logger;

import com.ats.platform.Bar;
import com.ats.platform.BarSeries;
import com.ats.platform.Strategy;
import com.ats.platform.TimeSpan;
import com.ats.platform.Trade;

/**
 * testing strategy which will buy when the bar moves above the previous bar high,
 * flips when it moves below prev bar low.
 * 
 * @author Adrian
 *
 */
public class CloseAboveStrategy extends Strategy {
	private static final Logger logger = Logger.getLogger(CloseAboveStrategy.class);
	
	private double prevHigh;
	private double prevLow;
	private double tickSize;
	private int period = 5;
	
	private double dollarRisk = 1000;
	private double maxPosnDollar = 50000;
	
	private double minPrice = 10;
	private int minVol = 200000;
	boolean meetsThreshold = false;

	public CloseAboveStrategy() {
		super();
		addParam("Period", 5);
	}
	
	@Override
	public void init() {
		requestTimeSeries(TimeSpan.daily);
		
		period = getParam("Period").intValue();
		
		// TODO: allow default size calculation to come from the strategy
		defaultSize = 100;
		tickSize = getInstrument().getTickSize();
	}
	
	@Override
	public void onTrade(Trade trade) {
		if( ! meetsThreshold ) {
			return;
		}
		// do anything? 
		if( trade.getPrice() > prevHigh && !isLong() ) {
			BarSeries series = getSeries(TimeSpan.daily);
			Bar today = series.ago(0);
			int maxPosSize = (int)(maxPosnDollar / trade.getPrice());
			double currRisk = trade.getPrice() - Math.min(today.getLow(), prevLow) + (10*tickSize);
			int size = Math.min((int)(dollarRisk/(Math.abs(currRisk) * getInstrument().getMultiplier())), maxPosSize);
			if( size <= 100 ) {
				return;
			}
			goLong(size);
		} else if( trade.getPrice() < prevLow && !isShort() ) {
			BarSeries series = getSeries(TimeSpan.daily);
			Bar today = series.ago(0);
			double currRisk = Math.max(today.getHigh(), prevHigh) - trade.getPrice() + (10*tickSize);
			int size = Math.min((int)(dollarRisk/(Math.abs(currRisk) * getInstrument().getMultiplier())), 2000);
			if( size <= 100 ) {
				return;
			}
			goShort(size);
		}
	}
	
	@Override
	public void onBar(Bar bar) {
		if( bar.getLow() < minPrice || bar.getVolume() < minVol ) {
			cancelAllOrders();
			goFlat();
			meetsThreshold = false;
			return;
		}
		meetsThreshold = true;
		cancelAllOrders();
		prevHigh = bar.getHigh();
		prevLow = bar.getLow();
	}
}
