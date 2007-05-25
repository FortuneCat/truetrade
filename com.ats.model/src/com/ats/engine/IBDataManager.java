package com.ats.engine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.ats.platform.Bar;
import com.ats.platform.BarSeries;
import com.ats.platform.Instrument;
import com.ats.platform.MessageListener;
import com.ats.platform.Trade;
public class IBDataManager extends DataManager implements MessageListener {
	private static final Logger logger = Logger.getLogger(IBDataManager.class);
	
	public static final String SHOW_TRADES = "TRADES";
	public static final String SHOW_MIDPOINT = "MIDPOINT";
	public static final String SHOW_BID = "BID";
	public static final String SHOW_ASK = "ASK";
	public static final String SHOW_BID_ASK = "BID_ASK";
	
	public static final int TICK_TYPE_BID_SIZE = 0;
	public static final int TICK_TYPE_BID = 1;
	public static final int TICK_TYPE_ASK = 2;
	public static final int TICK_TYPE_ASK_SIZE = 3;
	public static final int TICK_TYPE_LAST = 4;
	public static final int TICK_TYPE_LAST_SIZE = 5;
	public static final int TICK_TYPE_HIGH = 6;
	public static final int TICK_TYPE_LOW = 7;
	public static final int TICK_TYPE_VOLUME = 8;
	public static final int TICK_TYPE_CLOSE = 9;
	
	
	public static final double NUM_MILLISECS_IN_DAY = 24*60*60*1000;

	private final List<BarSeries> requestData = new ArrayList<BarSeries>();
	private final List<Boolean> requestIsDone = new ArrayList<Boolean>();
	

	// store all data & maps to handle trade & quote data
	private final List<Instrument> tickInstruments = new ArrayList<Instrument>();
	private final Map<Instrument, IBQuote> instrumentQuotes = new HashMap<Instrument, IBQuote>();
	private final Map<Instrument, List<TickListener>> instrumentStrategies = new HashMap<Instrument, List<TickListener>>();

	
	// can have 2 simultaneous requests, but this allows us to block
	private boolean isRequestActive = false;
	
	private static final IBDataManager instance = new IBDataManager();

	private static final long TIMEOUT_MILLIS = 12*1000;
	
	private static final DateFormat ibDateFormat = new SimpleDateFormat("yyyyMMdd kk:mm:ss");

	private static final IBWrapperAdapter wrapper = IBWrapperAdapter.getWrapper();
	
	/**
	 * singleton constructor
	 *
	 */
	private IBDataManager() {
//		Runnable r = new Runnable() {
//			public void run() {
//				while( true ) {
//					try {
//						Thread.sleep(TIMEOUT_MILLIS);
//					} catch( Exception e ) {}
//					scanForTimeout();
//				}
//			}
//		};
//		
//		Thread t = new Thread(r);
//		t.setDaemon(true);
//		t.start();
		IBWrapperAdapter.getWrapper().addMessageListener(this);
	}
	
	public static final IBDataManager getInstance() {
		return instance;
	}
	
	@Override
	protected void reqMktData(Instrument instrument) {
		reqMktData(instrument, null);
	}

	public synchronized void reqMktData(Instrument instrument, TickListener runner) {
		int id = tickInstruments.indexOf(instrument);
		if( id < 0 ) {
			tickInstruments.add(instrument);
			id = tickInstruments.size() - 1;
			instrumentQuotes.put(instrument, new IBQuote());
			instrumentStrategies.put(instrument, new ArrayList<TickListener>());
		}
		if( runner != null ) {
			instrumentStrategies.get(instrument).add(runner);
		}
		IBHelper.getInstance().reqMktData(id, instrument);
	}

	void tickSize(int tickerId, int field, int size) {
		Instrument instr = tickInstruments.get(tickerId);
		IBQuote quote = instrumentQuotes.get(instr);
		
		boolean isDirty = false;
		switch( field ) {
		case TICK_TYPE_VOLUME:
			break;
		case TICK_TYPE_LAST_SIZE:
			quote.setLastSize(size);
			quote.setDateTime(new Date());
			isDirty = true;
			break;
		case TICK_TYPE_BID_SIZE:
			quote.setBidSize(size);
			break;
		case TICK_TYPE_ASK_SIZE:
			quote.setAskSize(size);
			break;
		}
		if( isDirty ) {
			Trade trade = new Trade(instr, quote.getDateTime(), quote.getLast(), quote.getLastSize());
			fireTrade(instr, trade);
		}
	}
	void tickPrice(int tickerId, int field, double price, int canAutoExecute ) {
		Instrument c = tickInstruments.get(tickerId);
		IBQuote quote = instrumentQuotes.get(c);
		switch(field) {
		case TICK_TYPE_CLOSE:
			if( quote.getLast() <= 0 ) {
				quote.setLast(price);
			}
			break;
		case TICK_TYPE_LAST:
			quote.setLast(price);
			break;
		case TICK_TYPE_BID:
			quote.setBid(price);
			break;
		case TICK_TYPE_ASK:
			quote.setAsk(price);
			break;
		}
	}

	public void reqHistData(Instrument instrument, BarSeries downSeries, Date startDate, Date endDate) {
		// TODO obey the start and end dates
		reqHistData(instrument, downSeries);
		
	}

	
	/**
	 * blocking request for hist data
	 * 
	 * @param instrument
	 * @param span
	 * @param runner
	 * @return
	 */
	public synchronized BarSeries reqHistData(Instrument instrument, BarSeries series) {
		// TODO: add timeout in the case of errors
		
		while( isRequestActive ) {
			// wait our turn
			try {
				wait();
			} catch( Exception e ) {
			}
		}
		// we have the conch!
		isRequestActive = true;

		requestData.add(series);
		requestIsDone.add(Boolean.FALSE);
		final int id = requestData.size() - 1;
		
		// TODO: generalize
		instrument.getContract().m_expiry = "200706";

		// build and place request
		Calendar nyCal = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"));
		String endDateString = ibDateFormat.format(nyCal.getTime());
		String priceType = instrument.isForex() ? SHOW_MIDPOINT : SHOW_TRADES;
		logger.info("Requesting historical data for " + instrument + ", id=" + id);
		try {
			IBHelper.getInstance().reqHistoricalData(id,
					instrument,
					endDateString,
					series.getTimeSpan().getIbDuration(),
					series.getTimeSpan().getIbBarParam(),
					priceType,
					1,
					2);
		} catch( Exception e ) {
			// typically a connection exception or other error
			logger.error("Could not request historical data: " + e);
			return null;
		}
		
		// wait for the request to complete
		// TODO: also stop when errors occur
		// TODO: add timeouts
		while( ! requestIsDone.get(id) ) {
			try {
				wait();
			} catch( Exception e ) {
				// do nothing
			}
		}
		
		// the price series has been built.  Prepare for next req and return
		isRequestActive = false;
		notifyAll();
		return series;
	}

            
	
	
	

	/**
	 * not for use by clients
	 */
	public synchronized void historicalData(int reqId, String date, double open, double high, double low,
			double close, int volume, double WAP, boolean hasGaps) {
		
		BarSeries series = requestData.get(reqId);
		//logger.debug("historical data for " + series.getInstrument());
		if( date.startsWith("finished")) {
//		if( high <= 0 ) {
			// completed this series
			logger.info("Completed historical data request for " + series.getInstrument() + ", id=" + reqId);
			requestIsDone.set(reqId, Boolean.TRUE);
			notifyAll();
			return;
		}

		// add a new PriceBar
		// TODO: IB data for equities "starts" at the end of the prev day's trading.  Readjust
		// to the start of the next day?
		Date start = new Date(Long.parseLong(date)*1000);
		Bar bar = new Bar(series.getType(), series.getTimeSpan(), start );
		bar.setOpen(open);
		bar.setHigh(high);
		bar.setLow(low);
		bar.setClose(close);
		bar.setVolume(volume);
		
		series.addHistory(bar);
	}

	public synchronized void error(int id, int errorCode, String errorMsg) {
		if( errorCode == IBWrapperAdapter.ERCODE_NOT_CONNECTED  ) {
			for( int i = 0; i < requestIsDone.size(); i++ ) {
				requestIsDone.set(i, Boolean.TRUE);
			}
			notifyAll();
		} else if( errorCode == IBWrapperAdapter.ERCODE_NO_SECURITY ) {
			// TODO: need to be assured that the ID corresponds to our request,
			// not some other subsystem!
		}
	}

	public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
		// TODO Auto-generated method stub
		
	}

}
