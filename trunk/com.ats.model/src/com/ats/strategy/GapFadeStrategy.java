package com.ats.strategy;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

import com.ats.platform.Bar;
import com.ats.platform.BarSeries;
import com.ats.platform.JOrder;
import com.ats.platform.Strategy;
import com.ats.platform.TimeSpan;
import com.ats.platform.Trade;

public class GapFadeStrategy extends Strategy {
	private static final Logger logger = Logger.getLogger(GapFadeStrategy.class);
	
	private static final String GAP_UP = "gap up";
	private static final String GAP_DOWN = "gap down";
	private double delta;
	private double prevClose;
	private double riskPerTrade;
	
	public GapFadeStrategy() {
		super();
	}
	
	@Override
	public void init() {
		requestTimeSeries(TimeSpan.daily);
		setTradingInterval("6:00", "12:40");
		riskPerTrade = 1000;
	}
	
	public void onOrderFilled(JOrder order) {
		cancelAllOrders();
		if( hasPosition() ) {
			int size = order.getQuantity();
			if( GAP_UP.equals(order.getText())) {
				logger.debug("Gap up filled, placing tgt = " + prevClose + ", stoploss = " + (order.getAvgPrice() + delta));
				sendOrder(buyTrailingStopOrder(size, order.getAvgPrice() + Math.abs(delta/2), Math.abs(delta), "gap up stop loss"));
			} else if( GAP_DOWN.equals(order.getText())) {
				logger.debug("Gap down filled, placing tgt = " + prevClose + ", stoploss = " + (order.getAvgPrice() + delta));
				sendOrder(sellTrailingStopOrder(size, order.getAvgPrice() - Math.abs(delta/2), Math.abs(delta), "gap down stop loss"));
			}
		}
	}
	
	
	@Override
	public void onBarOpen(Bar bar) {
		if( bar.getTimeSpan() != TimeSpan.daily ) {
			return;
		}
		BarSeries series = getSeries(TimeSpan.daily);
		if( series.size() < 2 ) {
			return;
		}
		Bar ago = series.ago(1);
		prevClose = ago.getClose();
		delta = bar.getOpen() - prevClose;
		double tickSize = getInstrument().getTickSize();
		if( Math.abs(delta) > 4 * tickSize && Math.abs(delta) < 80 * tickSize ) {
			// TODO: Order Manager should reject orders of 0 size
			int size = (int)(riskPerTrade/(Math.abs(delta) * getInstrument().getMultiplier()));
			logger.debug("Calculated size = " + size );
			if( size <= 0 ) {
				return;
			}
			if( delta < 0 ) {
				// gap down
				
				JOrder order = buyOrder(size);
				order.setText(GAP_DOWN);
				sendOrder(order);
			} else {
				// gap up
				JOrder order = sellOrder(size);
				order.setText(GAP_UP);
				sendOrder(order);
			}
		}
	}

}
