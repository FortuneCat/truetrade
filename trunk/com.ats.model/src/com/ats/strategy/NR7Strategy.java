package com.ats.strategy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.ColorConstants;

import com.ats.platform.Bar;
import com.ats.platform.BarSeries;
import com.ats.platform.ChartContribution;
import com.ats.platform.JOrder;
import com.ats.platform.OrderType;
import com.ats.platform.Strategy;
import com.ats.platform.TALib;
import com.ats.platform.TimeSpan;
import com.ats.platform.Bar.BarField;
import com.ats.platform.Position.PositionSide;

public class NR7Strategy extends Strategy {
	private static final String LONG_STOP = "Long Stop";
	private static final String SHORT_STOP = "Short Stop";

	private static final String UPTREND_ENTRY = "uptrend entry";
	private static final String DOWNTREND_ENTRY = "downtrend entry";

	private static final Logger logger = Logger.getLogger(NR7Strategy.class);
	
	private int period;
	
	private double currRisk;
	
	private double dollarRisk = 500;
	
	public NR7Strategy() {
		super();
		addParam("Period", 9);
		defaultSize = 1;
	}
	
	@Override
	public void init() {
		requestTimeSeries(TimeSpan.min15);

		period = getParam("Period").intValue();
		
	}
	
	@Override
	public void onOrderFilled(JOrder order) {
		if( hasPosition() ) {
			if( UPTREND_ENTRY.equals(order.getText())) {
				// set stoploss
				sendOrder(sellTrailingStopOrder(order.getFilledSize(), order.getAvgPrice() - currRisk, currRisk, "Long Stoploss"));
			} else if( DOWNTREND_ENTRY.equals(order.getText())) {
				// set stoploss
				sendOrder(buyTrailingStopOrder(order.getFilledSize(), order.getAvgPrice() + currRisk, currRisk, "Short Stoploss"));
			}
		} else {
			// a stop or target hit
			cancelAllOrders();
		}
	}
	
	private boolean isEndOfDay(Bar bar) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(bar.getEndTime());
		cal.set(Calendar.HOUR_OF_DAY, 12);
		cal.set(Calendar.MINUTE, 50);
		return ! cal.getTime().after(bar.getEndTime());
	}
	
	private boolean isNewPositionCutoff(Bar bar) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(bar.getEndTime());
		cal.set(Calendar.HOUR_OF_DAY, 12);
		cal.set(Calendar.MINUTE, 0);
		return ! cal.getTime().after(bar.getEndTime());
	}

	@Override
	public void onBar(Bar bar) {
		if( bar.getTimeSpan() != TimeSpan.min15 ) {
			return;
		}
		
		// if has enough data
		BarSeries series = getSeries(TimeSpan.min15);
		if( series.size() < period + 3 ) {
			return;
		}

		if( isEndOfDay(bar) ) {
			// 10 minutes or less to go, close down for the day
			cancelAllOrders();
			goFlat();
			return;
		}
		
		if( isNewPositionCutoff(bar) ) {
			// last time to take trades;
			return;
		}

		if( hasPosition() ) {
			return;
		}
		
		// use stop orders as breakouts.  If it hasn't triggered yet, cancel
		// and allow entry signals to place new one
		for( JOrder order : getOutstandingOrders() ) {
			if( UPTREND_ENTRY.equals(order.getText()) || DOWNTREND_ENTRY.equals(order.getText())) {
				cancelOrder(order);
			}
		}
		
		double fast[] = TALib.ema(series, BarField.close, period, series.size()- period-2, series.size()-1);
		
		double currFast = fast[fast.length-1];
		double prevFast = fast[fast.length-2];
		double tickSize = getInstrument().getTickSize();
		currRisk = bar.getHigh() - bar.getLow() + tickSize;
		int size = Math.min((int)(dollarRisk/(Math.abs(currRisk) * getInstrument().getMultiplier())), 10);
		if( size <= 0 ) {
			return;
		}

		if( currFast > prevFast ) {
			// in an uptrend
			if( isNR7(series) ) {
				sendOrder(buyStopOrder(size, bar.getHigh() + tickSize, UPTREND_ENTRY));
			}
			
		} else if( currFast < prevFast && (prevFast - currFast > tickSize) ) {
			// in a downtrend
			if( isNR7(series) ) {
				sendOrder(sellStopOrder(size, bar.getLow() - tickSize, DOWNTREND_ENTRY));
			}
			
		}
	}
	
	private boolean isNR7(BarSeries series) {
		Bar curr = series.ago(0);
		double currSpan = curr.getHigh() - curr.getLow();
		for( int i = 1; i < 7; i++ ) {
			Bar bar = series.ago(i);
			if( bar.getHigh() - bar.getLow() <= currSpan ) {
				return false;
			}
		}
		return true;
	}


	@Override
	public List<ChartContribution> getContributions(BarSeries series) {
		List<ChartContribution> ret = new ArrayList<ChartContribution>(2);
		
//		ChartContribution contrib = new ChartContribution();
//		contrib.setData(TALib.ema(series, BarField.close, period));
//		contrib.setLabel("EMA(" + period + ")");
//		contrib.setColor(ColorConstants.red);
//		ret.add(contrib);
		
		return ret;
	}
	
	
	
}
