package com.ats.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ats.platform.Bar;
import com.ats.platform.BarSeries;
import com.ats.platform.BarType;
import com.ats.platform.Instrument;
import com.ats.platform.TimeSpan;
import com.ats.platform.Trade;

public abstract class DataManager {
	private static final Logger logger = Logger.getLogger(DataManager.class);
	
	// all listeners for each instrument
	private Map<Instrument, List<TickListener>> listeners = new HashMap<Instrument, List<TickListener>>();
	
	/** all of the current market data requests */
	private List<Instrument> mktDataRequests = new ArrayList<Instrument>();
	
	/** the bar series.  They include all time scales for which a listener has
	 * requested.  This means that, if two strategies are running with the
	 * same instrument, they will both get the same onBar() messages.  Each listener
	 * is responsible for filtering out extraneous onBar() messages.
	 */
	private  Map<Instrument, List<BarSeries>> barSeries = new HashMap<Instrument, List<BarSeries>>();
	
	public void reset() {
		listeners = new HashMap<Instrument, List<TickListener>>();
		mktDataRequests = new ArrayList<Instrument>();
		barSeries = new HashMap<Instrument, List<BarSeries>>();
	}
	
	public synchronized final void addTickListener(Instrument instrument, TickListener listener) {
		List<TickListener> l = listeners.get(instrument);
		if( l == null ) {
			l = new ArrayList<TickListener>();
			listeners.put(instrument, l);
		}
		if( l.contains(listener)) {
			// logger.debug("attempted to add a duplicate listener");
		} else {
			l.add(listener);
		}
	}
	
	public synchronized final void removeTickListener(Instrument instrument, TickListener listener ) {
		List<TickListener> l = listeners.get(instrument);
		if( l != null ) {
			l.remove(listener);
		}
	}
	
	protected final void fireTrade(Instrument instrument, Trade trade) {
		List<BarSeries> tmp = barSeries.get(instrument);
		List<BarSeries> seriesList = new ArrayList<BarSeries>(tmp.size());
		seriesList.addAll(tmp);
		for(BarSeries series : seriesList ) {
			// TODO: need to automatically time-out bars in real life
			if( (!series.canAddTrade(trade)) && series.size() > 0 ) {
				fireBar(instrument, series.getMostRecent());
			}
			Bar bar = series.addTrade(trade);
			if( bar != null ) {
				fireBarOpen(instrument, series.getMostRecent());
			}
		}
		List<TickListener> list = listeners.get(instrument);
		TickListener tl[] = new TickListener[list.size()];
		tl = list.toArray(tl);
		for( int i = 0; i < tl.length; i++ ) {
			tl[i].onTrade(trade);
		}
	}
	
	protected final void fireBar(Instrument instrument, Bar bar) {
		List<TickListener> list = listeners.get(instrument);
		TickListener tl[] = new TickListener[list.size()];
		tl = list.toArray(tl);
		for( int i = 0; i < tl.length; i++ ) {
			tl[i].onBar(bar);
		}
	}
	
	protected final void fireBarOpen(Instrument instrument, Bar bar) {
		List<TickListener> list = listeners.get(instrument);
		TickListener tl[] = new TickListener[list.size()];
		tl = list.toArray(tl);
		for( int i = 0; i < tl.length; i++ ) {
			tl[i].onBarOpen(bar);
		}
	}
	
	public synchronized BarSeries getSeries(Instrument instrument, TimeSpan span) {
		List<BarSeries> series = barSeries.get(instrument);
		if( series == null ) {
			series = new ArrayList<BarSeries>();
			barSeries.put(instrument, series);
		}
		for(BarSeries bs : series) {
			if( bs.getTimeSpan().equals(span)) {
				return bs;
			}
		}
		return null;
	}

	public synchronized BarSeries requestSeries(Instrument instrument, TimeSpan span) {
		BarSeries bs = getSeries(instrument, span);
		
		// could not find it, so backfill and then add
		if( bs == null ) {
			bs = new BarSeries(instrument, BarType.time, span);
		}
		reqHistData(instrument, bs);
		barSeries.get(instrument).add(bs);
		if( ! this.mktDataRequests.contains(instrument) ) {
			this.mktDataRequests.add(instrument);
			reqMktData(instrument);
		}
		return bs;
	}


	protected abstract void reqMktData(Instrument instrument);

	protected abstract BarSeries reqHistData(Instrument instrument, BarSeries series);
}
