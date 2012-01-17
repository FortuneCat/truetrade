package com.ats.strategy;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.ats.platform.Bar;
import com.ats.platform.BarSeries;
import com.ats.platform.BarType;
import com.ats.platform.Instrument;
import com.ats.platform.TimeSpan;
import com.ats.strategy.TripleScreenStrategy.TrendDirection;
import com.ib.client.Contract;

/**
 * @author Krzysztof Kazmierczyk
 */
public class TripleScreenStrategyTest {

	private BarSeries series;
	
	private class DummyTripleScreenStrategy extends TripleScreenStrategy{
		
		public TrendDirection longTrend;
		public boolean shouldSubmitPosition = false;
		public int openPositions = 0;
		
		public void setBuyMore(boolean value) {
			this.buyMore = value;
		}
		
		public void setTradeShort(boolean value) {
			this.tradeShort = value;
		}
		
		public void setCloseOnFlat(boolean value) {
			this.closeOnFlat = value;
		}
		
		@Override
		protected void submitPosition(Bar bar) {
			if (TrendDirection.RISING.equals(recentTrendDirection))
				openPositions++;
			else
				openPositions--;
		}
		
		@Override
		protected TrendDirection getLongTrend(Bar bar) {
			return longTrend;
		}
		
		@Override
		protected boolean buySignalFromShortTrend(Bar bar) {
			return shouldSubmitPosition;
		}
		
		@Override
		protected void requestFlat(Bar bar) {
			openPositions = 0;
		}
	};
	
	public void initializeData2() {
		final int milisinday = 1000 * 3600 * 24;
		final TimeSpan span = TimeSpan.daily;
		List<Bar> history = new ArrayList<Bar>();
		for (int i = 0; i < 1000; i++) {
			history.add(new Bar(BarType.time, span, milisinday, i * milisinday, 10, 10, 10, 10, 100));
		}
		series = new BarSeries(new Instrument(new Contract()), BarType.time, TimeSpan.daily);
		series.addHistory(history);
	}
	
	@Before
	public void setUp() throws Exception {
		initializeData2();
	}

	@Test
	public void testOnBar() {
		DummyTripleScreenStrategy s1 = new DummyTripleScreenStrategy();
		s1.setBuyMore(false);
		s1.setCloseOnFlat(true);
		s1.setTradeShort(true);
		s1.longTrend = TrendDirection.FALLING;
		s1.shouldSubmitPosition = false;
		s1.onBar(series.ago(0));
		Assert.assertEquals(0, s1.openPositions);
		s1.onBar(series.ago(0));
		Assert.assertEquals(0, s1.openPositions);
		s1.shouldSubmitPosition = true;
		s1.onBar(series.ago(0));
		Assert.assertEquals(-1, s1.openPositions);
		s1.onBar(series.ago(0));
		Assert.assertEquals(-1, s1.openPositions);
		s1.setBuyMore(true);
		s1.onBar(series.ago(0));
		Assert.assertEquals(-2, s1.openPositions);
		s1.longTrend = TrendDirection.FLAT;
		s1.onBar(series.ago(0));
		Assert.assertEquals(0, s1.openPositions);
		s1.onBar(series.ago(0));
		Assert.assertEquals(0, s1.openPositions);
		s1.setTradeShort(false);
		s1.longTrend = TrendDirection.FALLING;
		s1.onBar(series.ago(0));
		Assert.assertEquals(0, s1.openPositions);
		s1.setTradeShort(true);
		s1.setCloseOnFlat(false);
		s1.onBar(series.ago(0));
		Assert.assertEquals(-1, s1.openPositions);
		s1.longTrend = TrendDirection.FLAT;
		s1.onBar(series.ago(0));
		Assert.assertEquals(-1, s1.openPositions);
		s1.longTrend = TrendDirection.FALLING;
		s1.onBar(series.ago(0));
		Assert.assertEquals(-2, s1.openPositions);
		s1.longTrend = TrendDirection.RISING;
		s1.onBar(series.ago(0));
		Assert.assertEquals(1, s1.openPositions);
		s1.longTrend = TrendDirection.FLAT;
		s1.onBar(series.ago(0));
		Assert.assertEquals(1, s1.openPositions);
		s1.longTrend = TrendDirection.FALLING;
		s1.onBar(series.ago(0));
		Assert.assertEquals(-1, s1.openPositions);
	}

}
