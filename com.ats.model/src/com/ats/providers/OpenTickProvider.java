package com.ats.providers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.ats.platform.Bar;
import com.ats.platform.BarSeries;
import com.ats.platform.Instrument;
import com.ats.platform.TimeSpan;
import com.ats.platform.Bar.BarType;
import com.ats.platform.Instrument.InstrumentType;
import com.ats.utils.Utils;
import com.opentick.OTClient;
import com.opentick.OTConstants;
import com.opentick.OTDataEntity;
import com.opentick.OTError;
import com.opentick.OTLoginException;
import com.opentick.OTMessage;
import com.opentick.OTOHLC;

public class OpenTickProvider extends OTClient {
	private static final Logger logger = Logger.getLogger(OpenTickProvider.class);
	
	private static final String EXPIRATION_CODE[] = new String[]{
		"F", "G", "H", 
		"J", "K", "M", 
		"N", "Q", "U", 
		"V", "X", "Z"
	};
	
	private static final OpenTickProvider instance = new OpenTickProvider();
	
	private boolean isRequestActive;
	
	private BarSeries requestData;
	
	private final List<Integer> currentRequestIds = new ArrayList<Integer>();
	

	// store all data & maps to handle trade & quote data
	private final List<Instrument> tickInstruments = new ArrayList<Instrument>();
//	private final Map<Instrument, IBQuote> instrumentQuotes = new HashMap<Instrument, IBQuote>();
//	private final Map<Instrument, List<TickListener>> instrumentStrategies = new HashMap<Instrument, List<TickListener>>();

	
	private OpenTickProvider() {
		// addHost("feed1.opentick.com", 10015); //delayed data
		// addHost("feed1.opentick.com", 10010); //real-time data
		addHost("feed2.opentick.com", 10010); // real-time data
	}
	
	private synchronized void init() {
		if (!isLoggedIn()) {
	        String user = Utils.getPreferenceStore().getString(Utils.OPENTICK_USER);
	        String password = Utils.getPreferenceStore().getString(Utils.OPENTICK_PASSWORD);
//	        System.out.println("Logging in with " + user + ", " + password);
			try {
				login(user, password);
				
				while( !isLoggedIn() ) {
					try {
						wait();
					} catch( Exception e) {
					}
				}
			} catch (OTLoginException e) {
				logger.error("Could not log in to OpenTick", e);
			}
		}
	}
	
	public static final OpenTickProvider getInstance() {
		return instance;
	}
	
	
	public synchronized BarSeries getHistoricalData(Instrument instrument, TimeSpan span, Date startDate, Date endDate) {
		init();
		// TODO: add timeout in the case of errors
		
		requestData = new BarSeries(instrument, BarType.time, span);
		
		while( isRequestActive ) {
			// wait our turn
			try {
				wait();
			} catch( Exception e ) {
			}
		}
		// we have the conch!
		isRequestActive = true;

		// build and place request
		Calendar nyCal = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"));
		
		Calendar startCal = (Calendar)nyCal.clone();
		startCal.setTime(startDate);
		startCal.set(Calendar.HOUR_OF_DAY, 0);
		startCal.set(Calendar.MINUTE, 0);
		startCal.set(Calendar.SECOND, 0);
		startCal.set(Calendar.MILLISECOND, 0);
		
		Calendar endCal = (Calendar)nyCal.clone();
		endCal.setTime(endDate);
		endCal.set(Calendar.HOUR_OF_DAY, 23);
		endCal.set(Calendar.MINUTE, 0);
		endCal.set(Calendar.SECOND, 0);
		endCal.set(Calendar.MILLISECOND, 0);

		int start_int = (int) (startCal.getTime().getTime() / 1000);
		int end_int = (int) (endCal.getTime().getTime() / 1000);
		
		int timeFrame = getTimeFrame(span);
		int duration = getDuration(span);

		String exchange = getExchange(instrument);
		if( instrument.isStock() ) {
			try {
				int id = requestHistData(new OTDataEntity(exchange, instrument.getSymbol()), start_int, end_int, timeFrame, duration);
				currentRequestIds.add(id);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if( instrument.isFuture() ) {
			List<String> expiryCodes = getExpiryCodes(instrument, startCal, endCal);
			// ES is on the "EM", CME-MINI
			// YM is on the "EC", CBOT
			for( String currCode : expiryCodes ) {
				try {
					String symbol = "/" + instrument.getSymbol() + currCode;
					int id = requestHistData(new OTDataEntity(exchange, symbol),
							start_int, end_int, timeFrame, duration);
					System.out.println("requesting " + symbol + " with id = " + id);
					currentRequestIds.add(id);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		
		
		// wait for the request to complete
		while( currentRequestIds.size() > 0 ) {
			try {
				wait(1000l);
			} catch( Exception e ) {
				// do nothing
			}
		}
		
		// the price series has been built.  Prepare for next req and return
		isRequestActive = false;
		notifyAll();
		return requestData;
	}
	
	@Override
	public void onHistOHLC(OTOHLC ohlc) {
//		System.out.println("onHistOHLC: " + ohlc);

		// add a new PriceBar
		// TODO: IB data for equities "starts" at the end of the prev day's trading.  Readjust
		// to the start of the next day?
		Date start = new Date(ohlc.getTimestamp()*1000l);
		Bar bar = new Bar(requestData.getType(), requestData.getTimeSpan(), start );
		bar.setOpen(ohlc.getOpenPrice());
		bar.setHigh(ohlc.getHighPrice());
		bar.setLow(ohlc.getLowPrice());
		bar.setClose(ohlc.getClosePrice());
		bar.setVolume((int)ohlc.getVolume());
		
		requestData.addHistory(bar);
	}
	
	@Override
	public synchronized void onMessage(OTMessage message) {
		System.out.println("Message: " + message + " for id " + message.getRequestID());
		if( message.getCode() == 10 ) {
			// completed this series
			if( currentRequestIds.remove((Integer)message.getRequestID()) ) {
				notifyAll();
			}
		}
	}

	
	private int getDuration(TimeSpan span) {
		int duration = 0;
		switch(span) {
		case min1:
			duration = 1;
			break;
		case min2:
			duration = 2;
			break;
		case min5:
			duration = 5;
			break;
		case min15:
			duration = 15;
			break;
		case min30:
			duration = 30;
			break;
		case hourly:
			duration = 1;
			break;
		case daily:
			duration = 1;
			break;
		}
		return duration;
	}

	private int getTimeFrame(TimeSpan span) {
		int timeFrame = OTConstants.OT_HIST_OHLC_MINUTELY;
		if( span == TimeSpan.hourly ) {
			timeFrame = OTConstants.OT_HIST_OHLC_HOURLY;
		} else if( span == TimeSpan.daily ) {
			timeFrame = OTConstants.OT_HIST_OHLC_DAILY;
		}
		return timeFrame;
	}

	private List<String> getExpiryCodes(Instrument instrument, Calendar startCal, Calendar endCal ) {
		// TODO: this is another hack, assuming 4-month expiration schedules
		List<String> ret = new ArrayList<String>();
		
		Calendar curr = (Calendar)startCal.clone();
		while( curr.before(endCal)) {
			int month = curr.get(Calendar.MONTH)+1;
			int expIdx = (int)Math.ceil((double)month/3.0) * 3;  // round up
			ret.add(EXPIRATION_CODE[expIdx-1] + (curr.get(Calendar.YEAR)%10));
			if( month % 3 == 0 ) {
				// in an expiry month, so add the next quarter
				curr.add(Calendar.MONTH, 3);
				month = curr.get(Calendar.MONTH)+1;
				expIdx = (int)Math.ceil((double)month/3.0) * 3;  // round up
				ret.add(EXPIRATION_CODE[expIdx-1] + (curr.get(Calendar.YEAR)%10));
			}
			// move to next quarter
			curr.add(Calendar.MONTH, 3);
		}
		return ret;
	}
	
	private String getExchange(Instrument instrument) {
		// TODO: use IB's ContractDetails to get the exact exchange list
		String exchange = "";
		if( instrument.isStock() ) {
			if( instrument.getSymbol().length() <= 3 ) {
				return "N";  // NYSE
			} else {
				return "Q";  // NASDAQ
			}
		} else if( instrument.isFuture() ) {
			// TODO: ugh, really need a better way of doing this
			if( "YM".equals(instrument.getSymbol())) {
				return "EC";
			} else {
				return "EM";
			}
		}
		
		return exchange;
	}

	public void onError(OTError error) {
		System.out.println("Error: " + error);
	}

	public synchronized void onLogin() {
		System.out.println("Logged in to the server");
		notifyAll();
	}

	
	public static void main(String args[]) {
		Instrument instrument = new Instrument();
		instrument.setInstrumentType(InstrumentType.future);
		instrument.setSymbol("ES");
	      Calendar start = new GregorianCalendar();
	      start.add(Calendar.DATE, -2);
	      start.set(Calendar.MINUTE, 0);
	      start.set(Calendar.SECOND, 0);
	      start.set(Calendar.MILLISECOND, 0);
	      Calendar end = new GregorianCalendar();
		
		BarSeries series = OpenTickProvider.getInstance().getHistoricalData(instrument, TimeSpan.min5, start.getTime(), end.getTime());
		System.out.println("Series = " + series);
	}

}
