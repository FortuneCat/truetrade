package com.ats.strategy;

import com.ats.platform.Bar;
import com.ats.platform.BarSeries;
import com.ats.platform.Position;
import com.ats.platform.Position.PositionSide;
import com.ats.platform.Strategy;
import com.ats.platform.TimeSpan;

/**
 * This sample strategy trades the S&P E-Mini futures contract using a combination
 * of two indicators. One measures a shorter term noise-adjusted trend, and the other
 * one measures a longer term noise-adjusted trend.
 */
public class TrendArbitrageStrategy extends Strategy {

    // strategy parameters
    private static final String FAST_TREND_LENGTH = "Fast trend length";
    private static final String SLOW_TREND_LENGTH = "Slow trend length";
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";
    
    private double entry;
    private double exit;
    private int fastTrendLength;
    private int slowTrendLength;


    public TrendArbitrageStrategy() {

        addParam(FAST_TREND_LENGTH, 6);
        addParam(SLOW_TREND_LENGTH, 17);
        addParam(ENTRY, 5.5);
        addParam(EXIT, 1.5);
        
        // define trading interval
        
        defaultSize = 1;
    }
    
    @Override
    public void init() {
    	requestTimeSeries(TimeSpan.min5);
    	entry = getParam(ENTRY).doubleValue();
    	exit = getParam(EXIT).doubleValue();
    	fastTrendLength = getParam(FAST_TREND_LENGTH).intValue();
    	slowTrendLength = getParam(SLOW_TREND_LENGTH).intValue();
        setTradingInterval("7:00", "12:40");
    }

    /**
     * This method is invoked when a new bar is completed.
     */
    @Override
    public void onBar(Bar bar) {
		BarSeries series = getSeries(TimeSpan.daily);
		double fastTrend = calcNoiseAdjustedTrend(series, fastTrendLength);
		double slowTrend = calcNoiseAdjustedTrend(series, slowTrendLength);

		double delta = fastTrend - slowTrend;
		
		Position pos = getPosition();
		if( ( pos.getSide() == PositionSide.LONG && delta > exit )
				|| ( pos.getSide() == PositionSide.SHORT && delta < -exit ) ) { 
			goFlat();
		}
		

		if (delta > entry && slowTrend < 0) {
			goShort(defaultSize);
		} else if (delta < -entry && slowTrend > 0) {
			goLong(defaultSize);
		}

	}

    
    // where do these calculations belong? JSystemTrader placed them in an
	// Indicator
    // class, which might make sense
    private double calcNoiseAdjustedTrend(BarSeries series, int periodLength) {
        if (series.size() < periodLength) {
        	// need more data
        	return Double.NaN;
        }
        
        double path = 0;
        for (int idx = periodLength-2; idx > 0; idx-- ) {
            double change = series.ago(idx).getClose() - series.ago(idx - 1).getClose();
            path += Math.abs(change);
        }

        double change = series.ago(0).getClose() - series.ago(periodLength-1).getClose();
        double noiseAdjustedTrend = 0;
        if (path != 0) {
            noiseAdjustedTrend = Math.abs(change) * (change / path);
        }

        return noiseAdjustedTrend;
    }
    
    
}
