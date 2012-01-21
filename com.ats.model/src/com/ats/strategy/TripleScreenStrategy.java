package com.ats.strategy;

import com.ats.platform.Bar;
import com.ats.platform.BarSeries;
import com.ats.platform.JOrder;
import com.ats.platform.Strategy;
import com.ats.platform.TimeSpan;

/**
 * This is strategy described in "Trading For A Living" Dr Alexander Elder.
 * 
 * @author Krzysztof Kazmierczyk
 */
public abstract class TripleScreenStrategy extends Strategy {

	protected static final TimeSpan defaultTimespan = TimeSpan.daily;

	public enum TrendDirection {
		RISING, FALLING, FLAT
	};

	/** Checks if we want to use short selling */
	protected boolean tradeShort = false;
	
	/** Indicates whether we can open more positions if we have open any. */
	protected boolean openMore = false;

	/**
	 * Original triple screen mentioned only two types of long trend (rising and
	 * falling). My idea is to use additional trend - flat which means that the
	 * market is neither rising nor failing. This property indicates what we
	 * should do when the market goes flat.
	 * <p/>
	 * If this indicator is set to {@code false}, then all positions all
	 * automatically closed. Otherwise we close only when long position is in
	 * falling trend or short one in rising trend.
	 */
	protected boolean closeOnFlat = false;

	protected TrendDirection recentTrendDirection = null;
	/** Indicates what type of the positions we can open */
	protected TrendDirection recentTrendNoFlat = null;
	
	/**
	 * Risk as multiply of difference between highest high and lowest low from
	 * last 5 bars
	 */
	protected double currRisk = 1.0;
	/**
	 * Indicates how should created stop loss below lowest low multipled by
	 * highest high and lowest low from last 5 bars
	 */ 
	protected double stopLossBelowDiff = 0.5;

	protected abstract TrendDirection getLongTrendDirection(Bar bar);
	protected abstract boolean isBuySignalFromShortTrend(Bar bar);
	
	protected void submitPosition(Bar bar) {
		cancelAllOrders();
		if (TrendDirection.RISING.equals(recentTrendNoFlat)) {
			sendOrder(buyLimitOrder(1, bar.getHigh(), "long"));
		} else {
			sendOrder(sellLimitOrder(1, bar.getLow(), "short"));
		}
	}
	
	/** This method is executed when trend changed direction and we should close our position */
	protected void requestFlat(Bar bar) {
		goFlat("Long trend goes flat");
	}

	public TripleScreenStrategy() {
		addParam("Current max risk", 1.0);
		addParam("Stop loss below lowest of two bars", 0.5);
	}
	
	@Override
	public void init() {
		super.init();
		currRisk = getParam("Current max risk").doubleValue();
		stopLossBelowDiff = getParam("Stop loss below lowest of two bars").doubleValue();
	}
	
	@Override
	public void onBar(Bar bar) {
		TrendDirection currentTrendDirection = getLongTrendDirection(bar);

		if (currentTrendDirection == null)
			return;
		if (recentTrendDirection == null)
			recentTrendDirection = currentTrendDirection;
		if (recentTrendNoFlat == null && !TrendDirection.FLAT.equals(currentTrendDirection))
			recentTrendNoFlat = currentTrendDirection;

		// Checking if we should close all positions.
		if (recentTrendDirection != currentTrendDirection) {
			if (closeOnFlat
					|| (!TrendDirection.FLAT.equals(currentTrendDirection) 
							&& !currentTrendDirection.equals(recentTrendNoFlat))) {
				requestFlat(bar);
				if (!TrendDirection.FLAT.equals(currentTrendDirection))
					recentTrendNoFlat = currentTrendDirection;
			}
			recentTrendDirection = currentTrendDirection;
		}
		
		// Checking if we should buy or not.
		if (!TrendDirection.FLAT.equals(currentTrendDirection)
				&& (openMore || !hasOpenPosition())
				&& (tradeShort || TrendDirection.RISING.equals(currentTrendDirection))
				&& isBuySignalFromShortTrend(bar)) {
			submitPosition(bar);
		}
	}
	
	/**
	 * This method has been created only for testing purposes for
	 * {@link TripleScreenStrategyTest} class
	 */
	protected boolean hasOpenPosition() {
		return hasPosition();
	}
	
	@Override
	public void onOrderFilled(JOrder order) {
		BarSeries series = getSeries(defaultTimespan);
		final double avgDiff = (series.highestHigh(5).getHigh() - series
				.lowestLow(5).getLow()); 
		if (TrendDirection.RISING.equals(recentTrendNoFlat) && "long".equals(order.getText())) {
			final double minPriceTwoDays = series.lowestLow(2).getLow();
			sendOrder(sellTrailingStopOrder(order.getFilledSize(),
					minPriceTwoDays - stopLossBelowDiff * avgDiff, avgDiff * currRisk, "Long Stoploss"));
		} else if ("short".equals(order.getText())) {
			final double maxPriceTwoDays = series.highestHigh(2).getHigh();
			sendOrder(buyTrailingStopOrder(order.getFilledSize(),
					maxPriceTwoDays + stopLossBelowDiff  * avgDiff, avgDiff * currRisk, "Short Stoploss"));
		}
	}
}
