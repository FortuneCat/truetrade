package com.ats.engine.backtesting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ats.db.PlatformDAO;
import com.ats.engine.DataManager;
import com.ats.platform.Bar;
import com.ats.platform.BarSeries;
import com.ats.platform.Instrument;
import com.ats.platform.TimeSpan;
import com.ats.platform.Trade;

/**
 * Uses stored data to generate simulated ticks.
 * 
 * @author Adrian
 *
 */
public class BacktestDataManager extends DataManager {

	private TimeSpan span;
	
	private List<Instrument> current = new ArrayList<Instrument>();

	public BacktestDataManager() {
		super();
	}
	
	@Override
	protected BarSeries reqHistData(Instrument instrument, BarSeries series) {
		// for backtesting, we load historical data in the reqMktData() section
		return series;
	}
	
	private synchronized void addInstrument(Instrument instr) {
		current.add(instr);
	}
	private synchronized void completedInstrument(Instrument instr) {
		current.remove(instr);
		if( current.size() <= 0 ) {
			// complete, so empty listeners
			reset();
		}
	}
	
	public synchronized boolean isComplete() {
		return current.size() <= 0;
	}

	@Override
	protected void reqMktData(final Instrument instrument) {
		addInstrument(instrument);
		
		new Thread(new Runnable() {
			public void run() {
				// TODO: use the StrategyEngine's saved time span
				BarSeries series = PlatformDAO.getBarSeries(instrument, span);
				final double tickSize = instrument.getTickSize();
				for( int i = 0; i < series.size(); i++ ) {
					Bar bar = series.itemAt(i);
					
					// fake trades using a zig-zag pattern
					
					// TODO: confirm sufficient liquidity exists to decompose like this

					boolean isUpBar = bar.getOpen() <= bar.getClose(); 

					double totalDollarRange = bar.getHigh() - bar.getLow();
					if( isUpBar ) {
						totalDollarRange += (bar.getHigh() - bar.getOpen());
						totalDollarRange += (bar.getClose() - bar.getLow());
					} else {
						totalDollarRange += (bar.getOpen() - bar.getLow());
						totalDollarRange += (bar.getHigh() - bar.getClose());
					}
					int numTicks = (int)(totalDollarRange / tickSize);
					if( numTicks == 0 ) {
						// totally flat
						Trade trade = new Trade(instrument,
								bar.getBeginTime(), 
								bar.getOpen(), 
								bar.getVolume());
						fireTrade(instrument, trade);
						continue;
					}
					int volPerTrade = bar.getVolume() / numTicks;
					int millisPerTrade = (int)(bar.getEndTime().getTime() - bar.getBeginTime().getTime())/numTicks; 
					
					if( isUpBar ) {
						// O -> L -> H -> C
						
						// O -> L
						int dist = (int)((bar.getOpen() - bar.getLow()) / instrument.getTickSize());
						long lastDate = bar.getBeginTime().getTime();
						for( int tick = 0; tick < dist; tick++ ) {
							lastDate += millisPerTrade;
							Trade trade = new Trade(instrument,
									new Date(lastDate), 
									bar.getOpen() - (tick * tickSize), 
									volPerTrade);
							fireTrade(instrument, trade);
						}
						
						// L -> H
						dist = (int)((bar.getHigh() - bar.getLow()) / tickSize);
						for( int tick = 0; tick < dist; tick++ ) {
							lastDate += millisPerTrade;
							Trade trade = new Trade(instrument,
									new Date(lastDate), 
									bar.getLow() + (tick * tickSize), 
									volPerTrade);
							fireTrade(instrument, trade);
						}
						
						// H -> C
						dist = (int)((bar.getHigh() - bar.getClose()) / tickSize);
						for( int tick = 0; tick < dist; tick++ ) {
							lastDate += millisPerTrade;
							Trade trade = new Trade(instrument,
									new Date(lastDate), 
									bar.getHigh() - (tick * tickSize), 
									volPerTrade);
							fireTrade(instrument, trade);
						}
						
						// C
						Trade trade = new Trade( instrument,
								bar.getEndTime(),
								bar.getClose(),
								bar.getVolume() - ((numTicks-1)*volPerTrade));
						fireTrade(instrument, trade);

					} else {
						// O -> H -> L -> C
						
						// O -> H
						int dist = (int)((bar.getHigh() - bar.getOpen()) / tickSize);
						long lastDate = bar.getBeginTime().getTime();
						for( int tick = 0; tick < dist; tick++ ) {
							lastDate += millisPerTrade;
							Trade trade = new Trade(instrument,
									new Date(lastDate), 
									bar.getOpen() + (tick * tickSize), 
									volPerTrade);
							fireTrade(instrument, trade);
						}
						
						// H -> L
						dist = (int)((bar.getHigh() - bar.getLow()) / tickSize);
						for( int tick = 0; tick < dist; tick++ ) {
							lastDate += millisPerTrade;
							Trade trade = new Trade(instrument,
									new Date(lastDate), 
									bar.getHigh() - (tick * tickSize), 
									volPerTrade);
							fireTrade(instrument, trade);
						}
						
						// L -> C
						dist = (int)((bar.getClose() - bar.getLow()) / tickSize);
						for( int tick = 0; tick < dist; tick++ ) {
							lastDate += millisPerTrade;
							Trade trade = new Trade(instrument,
									new Date(lastDate), 
									bar.getLow() + (tick * tickSize), 
									volPerTrade);
							fireTrade(instrument, trade);
						}
						
						// C
						Trade trade = new Trade( instrument,
								bar.getEndTime(),
								bar.getClose(),
								bar.getVolume() - ((numTicks-1)*volPerTrade));
						fireTrade(instrument, trade);
					}
					
					//fireBar(instrument, bar);
				}
				completedInstrument(instrument);
			}
		}).start();
		
		// TODO: allow trades to occur at specified, fine-grained level.
//		TimeSpan simSpan = getDefinition().getBacktestSimulatedTimeSpan();
//		if( simSpan.getSpanInMillis() > getDefinition().getBacktestDataTimeSpan().getSpanInMillis() ) {
//			simSpan = getDefinition().getBacktestDataTimeSpan();
//		}

		// TODO: how do we know when to start?  Could add as a property, or use smarts to calculate
		// For now, we'll start at the beginning and force the strategies to ensure they have enough
		// historical data to trade.
		
		// TODO: need to keep all BarSeries in synch so that a series can ask for daily historical data
		// and know that the most recent bar is "today's", not some time in the future!
		

		// TODO: break up signals at a more fine-grained level so there aren't gaps
		
		// For simplicity sake, divide each period into quarters and place a trade at the O/H/L/C in
		// a zig-zag.  An up bar would go: O-L-H-C, and a down bar would go O-H-L-C.  Volume is
		// distributed evenly.
	}

	public TimeSpan getSpan() {
		return span;
	}

	public void setSpan(TimeSpan span) {
		this.span = span;
	}
	
}
