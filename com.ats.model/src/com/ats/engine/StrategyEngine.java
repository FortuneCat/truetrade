package com.ats.engine;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;

import com.ats.platform.Bar;
import com.ats.platform.BarSeries;
import com.ats.platform.BarType;
import com.ats.platform.Instrument;
import com.ats.platform.JOrder;
import com.ats.platform.Strategy;
import com.ats.platform.TimeSpan;
import com.ats.platform.Trade;

/**
 * Runs a trading strategy. There is a one-to-one map between the strategy class
 * and the strategy runner. That is, if 5 strategies are selected to run,
 * there will be 5 instances of the StrategyRunner created.
 * 
 * A StrategyRunner should be created for every mode in which the strategy
 * will process data: live/simulation and backtesting.  The StrategyRunner will be the
 * single focal point for all of the Strategy's interaction with the backend and su
 * must handle all order and data functions including supplying tick and bar data,
 * and handling orders.
 * 
 * The different handling of orders, different data sources etc. will be handled
 * by external helper classes.
 */
public abstract class StrategyEngine implements Runnable, TickListener, ExecutionListener {
	private static final Logger logger = Logger.getLogger(StrategyEngine.class);
	
	private StrategyDefinition definition;
	private Strategy strategy;
	
	private Instrument instrument;
	private OrderManager orderManager;
	
	private DataManager dataManager;
	
	//private final Map<TimeSpan, BarSeries> allSeries = new HashMap<TimeSpan, BarSeries>();
	private List<TimeSpan> timeSpans = new ArrayList<TimeSpan>();
	
	
    // trading time range
	private int startHour = -1;
	private int startMin = -1;
	private int endHour = -1;
	private int endMin = -1;

    
    public StrategyEngine(StrategyDefinition definition, Instrument instrument, OrderManager orderManager, DataManager dataManager) {
    	this.definition = definition;
    	this.instrument = instrument;
    	this.orderManager = orderManager;
    	this.dataManager = dataManager;
    	
    	try {
    		this.strategy = (Strategy)definition.instantiateStrategy(instrument);
    		this.strategy.setStrategyEngine(this);
    		// TODO: remove duplication between engine & strategy
    		this.strategy.setInstrument(instrument);
    	} catch( Exception e ) {
    		// TODO: pass this along to the UI?
    		logger.error("Could not instantiate strategy object.");
    	}

    	dataManager.addTickListener(strategy.getInstrument(), this);
    	orderManager.addExecutionListener(strategy, this);
    	
    	// do we need to set any additional properties onto the newly created strategy?
    	
        logger.info("Started monitoring " + strategy.getClass().getSimpleName());
    }
    
    public String toString() {
    	return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
    		.append("definition" + definition )
    		.append("instrument" + instrument )
    		.append("dataManager", dataManager)
    		.toString();
    }
    
    public void run() {
    	try {
    		strategy.init();
    		
    		// TODO: initialize positions
    		
    		strategy.onStartup();

    		executeStrategy();
    		
    	}catch( Throwable t) {
    		logger.error("Could not run strategy " + strategy, t);
    	}
    }
    
	public List<JOrder> getOutstandingOrders() {
		return orderManager.getOutstandingOrders(strategy);
	}

    
    protected abstract void executeStrategy();
    
    public synchronized BarSeries getSeries(TimeSpan span) {
//    	timeSpans.add(span);
//    	dataManager.addTickListener(getInstrument(), this);
    	return dataManager.getSeries(getInstrument(), span);
    }
    public synchronized void requestSeries(TimeSpan span) {
    	timeSpans.add(span);
    	dataManager.addTickListener(getInstrument(), this);
    	dataManager.requestSeries(getInstrument(), span);
    }
    public void onBarOpen(Bar bar) {
    	if( inTradingInterval(bar.getBeginTime())) {
	    	if( timeSpans.contains(bar.getTimeSpan()) ) {
	    		strategy.onBarOpen(bar);
	    	}
    	}
    }
    public void onBar(Bar bar) {
    	if( inTradingInterval(bar.getEndTime()) && timeSpans.contains(bar.getTimeSpan()) ) {
    		strategy.onBar(bar);
    	}
    }
	public void execution(JOrder order, JExecution execution) {
		// TODO: distinguish between the different order types
		strategy.onOrderFilled(order);
	}

    public void onTrade(Trade trade) {
    	if( inTradingInterval(trade.getDateTime())) {
    		strategy.onTrade(trade);
    	} else {
    		strategy.goFlat("End of trading day");
    	}
    }
    
	protected static boolean isAfter(Bar bar, int hour, int minute) {
		return isAfter(bar.getEndTime(), hour, minute);
	}
	protected static boolean isAfter(Date date, int hour, int minute) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		return ! cal.getTime().after(date);
	}
	
	protected boolean inTradingInterval(Date date) {
		if( startHour >= 0 ) {
			return( isAfter(date, startHour, startMin ) && !isAfter(date, endHour, endMin) );
		} else {
			return true;
		}
	}
	
	public void setTradingInterval(String startTime, String endTime) {
		try {
			StringTokenizer st = new StringTokenizer(startTime, ":");
			startHour = Integer.parseInt(st.nextToken());
			startMin = Integer.parseInt(st.nextToken());
			
			st = new StringTokenizer(endTime, ":");
			endHour = Integer.parseInt(st.nextToken());
			endMin = Integer.parseInt(st.nextToken());
		} catch( Exception e ) {
			logger.error("Could not parse start or end time", e);
			startHour = startMin = endHour = endMin = -1;
		}
	}


    
//    protected abstract void backfillBarSeries(BarSeries series);
    
    /**
     * method called just prior to running the strategy to allow strategy runners
     * to prepare for tick data
     *
     */
    protected abstract void initDataConnection();

	public void placeOrder(JOrder order) {
		orderManager.placeOrder(strategy, order);
	}
    
    public void cancelOrder(JOrder order) {
    	orderManager.cancelOrder(strategy, order);
    }

	protected Strategy getStrategy() {
		return strategy;
	}

	protected Instrument getInstrument() {
		return instrument;
	}

	protected OrderManager getOrderManager() {
		return orderManager;
	}

	protected StrategyDefinition getDefinition() {
		return definition;
	}
    
}

/**
 * for use only to locate the PriceBarSeries for a given strategy
 */
class PriceBarSeriesKey {
	private BarType type;
	private TimeSpan span;
	public PriceBarSeriesKey(BarType type, TimeSpan span ) {
		this.type = type;
		this.span = span;
	}
	public boolean equals(Object o) {
		if( ! ( o instanceof PriceBarSeriesKey )) {
			return false;
		}
		PriceBarSeriesKey that = (PriceBarSeriesKey)o;
		return( this.type == that.type && this.span == that.span );
	}
	public int hashCode( ) {
		return span.getSpanInSecs() * type.hashCode() * 2389;
	}
}
