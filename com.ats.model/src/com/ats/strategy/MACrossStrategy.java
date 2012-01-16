package com.ats.strategy;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import com.ats.platform.Bar;
import com.ats.platform.BarField;
import com.ats.platform.BarSeries;
import com.ats.platform.ChartContribution;
import com.ats.platform.JOrder;
import com.ats.platform.Strategy;
import com.ats.platform.TALib;
import com.ats.platform.TimeSpan;

public class MACrossStrategy extends Strategy {
	private static final Logger logger = Logger.getLogger(MACrossStrategy.class);
	
	private int slowPeriod;
	private int fastPeriod;
	
	public MACrossStrategy() {
		super();
		addParam("Slow Period", 18);
		addParam("Fast Period", 9);
	}
	
	@Override
	public void init() {
		requestTimeSeries(TimeSpan.daily);
//		requestTimeSeries(TimeSpan.min30);
		
		slowPeriod = getParam("Slow Period").intValue();
		fastPeriod = getParam("Fast Period").intValue();
		
		// TODO: allow default size to come from strat engine
		defaultSize = 100;
	}
	
	@Override
	public void onOrderFilled(JOrder order) {
		// TODO Auto-generated method stub
		super.onOrderFilled(order);
	}

	@Override
	public void onBar(Bar bar) {
		BarSeries series = getSeries(TimeSpan.daily);
		if( series.size() < slowPeriod + 3 ) {
			return;
		}
		double slow[] = TALib.sma(series, BarField.close, slowPeriod, series.size()- slowPeriod-2, series.size()-1);
		double fast[] = TALib.sma(series, BarField.close, fastPeriod, series.size()- fastPeriod-2, series.size()-1);
		
		// look for cross-over
		if( slow == null || slow.length < 2 || fast == null || fast.length < 2 ) {
			// not enough data
			return;
		}
		double currFast = fast[fast.length-1];
		double prevFast = fast[fast.length-2];
		double currSlow = slow[slow.length-1];
		double prevSlow = slow[slow.length-2];
		
		if( currFast > currSlow && prevFast <= prevSlow ) {
			goLong(defaultSize);
			return;
		} else if( currFast < currSlow && prevFast >= prevSlow ) {
			goShort(defaultSize);
			return;
		}
		
		// check for approaching cross
//		double fastSlope = currFast - prevFast;
//		double slowSlope = currSlow - prevSlow;
//		double prevDist = prevSlow - prevFast;
//		double currDist = currSlow - currFast;
//		
//		if( slowSlope > 0 && (fastSlope > 2*slowSlope) && (prevDist > currDist) ) {
//			goLong(100);
//			return;
//		}
//		if( slowSlope < 0 && (fastSlope < 2*slowSlope ) && (Math.abs(prevDist) > Math.abs(currDist)) ) {
//			goShort(100);
//			return;
//		}
		
	}


	@Override
	public List<ChartContribution> getContributions(BarSeries series) {
		List<ChartContribution> ret = new ArrayList<ChartContribution>(2);
		
		ChartContribution contrib = new ChartContribution();
		contrib.setData(TALib.sma(series, BarField.close, slowPeriod));
		contrib.setLabel("SMA(" + slowPeriod + ")");
		contrib.setColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		ret.add(contrib);
		
		contrib = new ChartContribution();
		contrib.setData(TALib.sma(series, BarField.close, fastPeriod));
		contrib.setLabel("SMA(" + slowPeriod + ")");
		contrib.setColor(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
		ret.add(contrib);
		
		return ret;
	}
	
	
	
}
