package com.ats.strategy;

import com.ats.platform.Bar;
import com.ats.platform.Strategy;

/**
 * This is strategy described in "Trading For A Living" Dr Alexander Elder.
 * 
 * @author Krzysztof Kazmierczyk
 */
public abstract class TripleScreenStrategy extends Strategy {

	public enum TrendDirection {
		RISING, FALLING, FLAT
	};

	/** Checks if we want to use short selling */
	protected boolean tradeShort = false;
	
	/** Indicates whether */
	protected boolean buyMore = false;

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
	protected TrendDirection recentTrendNoFlat = null;
	protected boolean alreadyBought = false;

	protected abstract TrendDirection getLongTrend(Bar bar);
	protected abstract boolean buySignalFromShortTrend(Bar bar);
	protected abstract void submitPosition(Bar bar);
	
	/** This method is executed when trend changed direction and we should close our position */
	protected void requestFlat(Bar bar) {
		goFlat("Long trend goes flat");
	}

	@Override
	public void onBar(Bar bar) {
		TrendDirection currentTrendDirection = getLongTrend(bar);

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
				alreadyBought = false;
				if (!TrendDirection.FLAT.equals(currentTrendDirection))
					recentTrendNoFlat = currentTrendDirection;
			}
			recentTrendDirection = currentTrendDirection;
		}
		
		// Checking if we should buy or not.
		if (!TrendDirection.FLAT.equals(currentTrendDirection)
				&& (buyMore || !alreadyBought)
				&& (tradeShort || TrendDirection.RISING.equals(currentTrendDirection))
				&& buySignalFromShortTrend(bar)) {
			alreadyBought = true;
			submitPosition(bar);
		}
	}

}
