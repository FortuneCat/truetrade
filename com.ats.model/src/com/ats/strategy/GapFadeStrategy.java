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


/**
 * This strategy is based on ideas presented in the TradeTheMarkets.com videos.
 * In there, they give the following stats:
 * 
 * Gaps fill:
Mon: 65%
Tue: 77%
Wed: 79%
Thu: 82%
Fri: 78%

1pt: 93% (ES pts)
2pt: 90%
3pt: 82%
4pt: 86%
5pt: 60%
6pt: 77%
7pt: 71%
8pt: 54%

1/2 gap fills: 80-82%

1st day of the month: 56%
last day of the month: 84%
 * 
 * They use a 10YM pt (1ES pt?) stop on their entries, when they have
 * at least three "TTMs" on the 89 tick, 50 tick, 1min and 2min charts.  I
 * don't know what a TTM is (proprietary TradeTheMarket indicator?) so it
 * may be worth adding in a fast stochastic or MACD.
 * 
 * 
 * @author Adrian
 *
 */

public class GapFadeStrategy extends Strategy {
	private static final Logger logger = Logger.getLogger(GapFadeStrategy.class);
	
	private static final String GAP_UP = "gap up";
	private static final String GAP_DOWN = "gap down";
	private final static String STOP = "stop";
	private double delta;
	private double prevClose;
	private double riskPerTrade;
	private double stopLoss = 1.0;
	
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
			int partialSize = size / 2;
			int remainSize = size - partialSize;
			if( GAP_UP.equals(order.getText())) {
				JOrder stopOrder = buyStopOrder(partialSize, order.getAvgPrice() + stopLoss, STOP );
				JOrder stopOrder2 = buyStopOrder(remainSize, order.getAvgPrice() + stopLoss, STOP );
				JOrder partialTgt = buyLimitOrder(partialSize, order.getAvgPrice() - Math.abs(delta/2), "partial target");
				JOrder remainTgt = buyLimitOrder(remainSize, order.getAvgPrice() - Math.abs(delta), "remainder");
				sendOrder(partialTgt);
				sendOrder(remainTgt);
				sendOrder(stopOrder);
				sendOrder(stopOrder2);
			} else if( GAP_DOWN.equals(order.getText())) {
				JOrder stopOrder = sellStopOrder(partialSize, order.getAvgPrice() - stopLoss, STOP );
				JOrder stopOrder2 = sellStopOrder(remainSize, order.getAvgPrice() - stopLoss, STOP );
				JOrder partialTgt = sellLimitOrder(partialSize, order.getAvgPrice() + Math.abs(delta/2), "partial target");
				JOrder remainTgt = sellLimitOrder(remainSize, order.getAvgPrice() + Math.abs(delta), "remainder");
				sendOrder(partialTgt);
				sendOrder(remainTgt);
				sendOrder(stopOrder);
				sendOrder(stopOrder2);
			} else if( STOP.equals(order.getText())) {
				cancelAllOrders();
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
		if( Math.abs(delta) >= 4 * tickSize && Math.abs(delta) < 80 * tickSize ) {
			// TODO: Order Manager should reject orders of 0 size
			int size = (int)(riskPerTrade/(1.5 * getInstrument().getMultiplier()));
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
