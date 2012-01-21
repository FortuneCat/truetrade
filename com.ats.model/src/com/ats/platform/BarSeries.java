package com.ats.platform;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * A handy class to store PriceBars in, together with the most common operations
 * which can be performed on them.
 * 
 * The series can handle historical data as well as update itself via tick data
 * 
 * @author Adrian
 *
 */
public class BarSeries {
	private int id;
	private List<Bar> bars = new ArrayList<Bar>();
	private BarType type;
	private TimeSpan span;
	private Instrument instrument;
	
	/**
	 * default constructor for the benefit of the DAO system
	 *
	 */
	public BarSeries() {
		this(null, BarType.time, TimeSpan.daily);
	}
	
	public BarSeries(Instrument instrument, BarType type, TimeSpan span) {
		this.type = type;
		this.span = span;
		this.instrument = instrument;
	}
	
	public String toString() {
		return "BarSeries [span="+span+",instrument="+instrument+",bars="+bars+"]";
	}
	
	public double[] getDoubleData( BarField field ) {
		double[] ret = new double[bars.size()];
		Iterator<Bar> it = bars.iterator();
		for( int i = 0; it.hasNext(); i++ ) {
			Bar bar = it.next();
			switch(field) {
			case high:
				ret[i] = bar.getHigh();
				break;
			case low:
				ret[i] = bar.getLow();
				break;
			case open:
				ret[i] = bar.getOpen();
				break;
			case close:
				ret[i] = bar.getClose();
				break;
			case volume:
				ret[i] = (double)bar.getVolume();
				break;
			}
		}
		return ret;
	}
	
	/**
	 * adds a trade to the current series.  If a bar is
	 * completed, that bar is returned
	 * @param trade
	 * @return
	 */
	public synchronized Bar addTrade(Trade trade) {
		Bar ret = null;
		if( bars.size() == 0 ) {
			Bar bar = new Bar(type, span, trade.getDateTime());
			bars.add(bar);
		}
		if( getMostRecent().canAdd(trade)) {
			getMostRecent().add(trade);
		} else {
			ret = getMostRecent();
			Bar bar = new Bar(type, span, trade.getDateTime());
			bar.add(trade);
			bars.add(bar);
		}
		return ret;
	}
	
	public boolean canAddTrade(Trade trade) {
		return (bars.size() > 0 && getMostRecent().canAdd(trade));
	}
	
	public Iterator<Bar> getBarsFromOldest() {
		return bars.iterator();
	}
	
	public synchronized int size() {
		return bars.size();
	}
	
	public synchronized Bar getMostRecent() {
		return ago(0);
	}
	
	public synchronized Bar getLeastRecent() {
		return bars.get(0);
	}
	
	/**
	 * returns the price bar N-bars ago (N==0 is the most recent bar)
	 * @param index
	 * @return
	 */
	public synchronized Bar ago(int index) {
		return bars.get(bars.size() - 1 - index);
	}
	
	/**
	 * returns the price bar, N==0 is the oldest bar
	 */
	public synchronized Bar itemAt(int index) {
		return bars.get(index);
	}
	
	/**
	 * returns the highest high in this series
	 * @return
	 */
	public synchronized Bar highestHigh() {
		return highestHigh(bars.size()-1, 0);
	}
	
	public synchronized Bar highestHigh(int from) {
		return highestHigh(from, 0);
	}
	
	/**
	 * from &lt; to, where to == 0 is the most recent, and from == size()-1 is the oldest
	 * @param from
	 * @param to
	 * @return
	 */
	public synchronized Bar highestHigh(int from, int to) {
		from = Math.min(from, bars.size()-1);
		to = Math.max(to, 0);
		if (from < to) {
			final int tmp = from;
			from = to;
			to = from;
		}
		Bar ret = ago(from);
		for( int i = from; i > to; i-- ) {
			Bar curr = ago(i);
			if( curr.getHigh() > ret.getHigh() ) {
				ret = curr;
			}
		}
		return ret;
	}
	
	public double highestHigh(Date minimumDate, Date maximumDate) {
		// TODO: optimize the initial seek?  This method is called a lot
		double high = 0;
		for( int i = 0; i < bars.size(); i++) {
			Bar bar = bars.get(i);
			if( bar.getEndTime().after(maximumDate) ) {
				return high;
			}
			if( bar.getBeginTime().after(minimumDate) || bar.getBeginTime().equals(minimumDate)) {
				high = Math.max(high, bar.getHigh());
			}
		}
		return high;
	}
	public double lowestLow(Date minimumDate, Date maximumDate) {
		// TODO: optimize the initial seek?  This method is called a lot
		double low = -1;
		for( int i = 0; i < bars.size(); i++ ) {
			Bar bar = bars.get(i);
			if( bar.getEndTime().after(maximumDate) ) {
				return low;
			}
			if( bar.getBeginTime().after(minimumDate) || bar.getBeginTime().equals(minimumDate)) {
				if( low < 0 ) { 
					low = bar.getLow();
				} else {
					low = Math.min(low, bar.getLow());
				}
			}
		}
		return low;
	}


	/**
	 * returns the lowest low in this series
	 * @return
	 */
	public synchronized Bar lowestLow() {
		return lowestLow(bars.size()-1, 0);
	}
	
	public synchronized Bar lowestLow(int from) {
		return lowestLow(from, 0);
	}
	
	/**
	 * from &lt; to, where to == 0 is the most recent, and from == size() is the oldest
	 * @param from
	 * @param to
	 * @return
	 */
	public synchronized Bar lowestLow(int from, int to) {
		from = Math.min(from, bars.size()-1);
		to = Math.max(to, 0);
		if (from < to) {
			final int tmp = from;
			from = to;
			to = from;
		}
		Bar ret = ago(from);
		for( int i = from; i > to; i-- ) {
			Bar curr = ago(i);
			if( curr.getLow() < ret.getLow() ) {
				ret = curr;
			}
		}
		return ret;
	}
	
	/**
	 * adds an historical bar. Can only be done before any tick
	 * data or other bars have been added.  Must be in properly
	 * sorted order.
	 */
	public synchronized void addHistory(Bar history) {
		TreeSet<Bar> set = new TreeSet<Bar>(bars);
		set.add(history);
		bars = new ArrayList<Bar>();
		bars.addAll(set);
	}
	
	public synchronized void addHistory(List<Bar> history) {
		TreeSet<Bar> set = new TreeSet<Bar>(bars);
		set.addAll(history);
		bars = new ArrayList<Bar>();
		bars.addAll(set);
	}
	
	public void addHistory(BarSeries series) {
		bars.addAll(series.bars);
	}

	public BarType getType() {
		return type;
	}

	public TimeSpan getTimeSpan() {
		return span;
	}

	/**
	 * merges the series data
	 * 
	 * @param downSeries
	 */
	public synchronized void mergeHistory(BarSeries downSeries) {
		if( downSeries == null ) {
			return;
		}
		addHistory(downSeries);
	}

	public Instrument getInstrument() {
		return instrument;
	}
	/**
	 * For use by the data store system only
	 * @param instrument
	 */
	@Deprecated
	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public int getTimespanId() {
		return span.ordinal();
	}
	public void setTimespanId(int id) {
		span = TimeSpan.values()[id];
	}
	
	/**
	 * only for use by the DB import process 
	 * @param bars
	 */
	@Deprecated
	public void setBars(List<Bar> bars) {
		this.bars = bars;
	}

	public BarSeries convertTimeSpan(TimeSpan tmpSpan) {
		BarSeries series = new BarSeries(instrument, type, tmpSpan);
		for (Bar bar : bars) {
			if (series.size() > 0 && series.getMostRecent().canAdd(bar)) {
				series.getMostRecent().add(bar);
			} else {
				Bar tmpBar = new Bar(type, tmpSpan, bar.getBeginTime());
				tmpBar.add(bar);
				series.bars.add(tmpBar);
			}
		}
		return series;
	}

}
