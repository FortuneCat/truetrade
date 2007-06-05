package com.ats.engine;

import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

import com.ats.engine.ib.IBDataManager;
import com.ats.platform.BarSeries;
import com.ats.platform.BarType;
import com.ats.platform.Instrument;
import com.ats.platform.TimeSpan;
import com.ats.providers.OpenTickProvider;
import com.ats.providers.YahooHistDataBuilder;


/**
 * A centralized facade for managing current and historical data.
 * 
 * This should be the go-to class for the GUI and tools that wish to
 * gather, parse, manipulate and view data.  Its primary responsibility
 * is to manage the different means of accessing and storing data
 * and so may delegate many of its tasks to other, specialized
 * classes.
 * 
 * @author Adrian
 *
 */
public class ImportDataManager {
	private static final Logger logger = Logger.getLogger(ImportDataManager.class);
	
	private static final long MILLIS_PER_DAY = 1000*60*60*24;
	
	public enum HistDataProvider {
		Yahoo,
		InteractiveBrokers,
		OpenTick
	};
	
//	private Map<Instrument, List<BarSeries>> cachedData = new HashMap<Instrument, List<BarSeries>>();

	
	
	private static final ImportDataManager instance = new ImportDataManager();
	
	private ImportDataManager() {
		
	}
	public static final ImportDataManager getInstance() {
		return instance;
	}
	
	public BarSeries downloadHistData(HistDataProvider provider, Instrument instrument, TimeSpan timeSpan, Date startDate, Date endDate) {
		BarSeries downSeries = null;
		switch( provider ) {
		case Yahoo:
			if( timeSpan == TimeSpan.daily ) {
				// only format supported by Yahoo
				GregorianCalendar calEnd = new GregorianCalendar();
				calEnd.setTime(endDate);
				
				GregorianCalendar calStart = new GregorianCalendar();
				calStart.setTime(startDate);

				int numDays = (int)( (endDate.getTime() - startDate.getTime()) / MILLIS_PER_DAY);
				
				downSeries = YahooHistDataBuilder.getInstance().getEODData(instrument, numDays );
			}
			break;
		case InteractiveBrokers:
			downSeries = new BarSeries(instrument, BarType.time, timeSpan);
			IBDataManager.getInstance().reqHistData(instrument, downSeries, startDate, endDate);
			break;
		case OpenTick:
			downSeries = OpenTickProvider.getInstance().getHistoricalData(instrument, timeSpan, startDate, endDate);
			break;
		}
		return downSeries;
	}
	

}
