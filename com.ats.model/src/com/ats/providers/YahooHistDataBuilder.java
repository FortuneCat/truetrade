package com.ats.providers;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.ats.platform.Bar;
import com.ats.platform.BarSeries;
import com.ats.platform.Instrument;
import com.ats.platform.TimeSpan;
import com.ats.platform.Bar.BarType;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;

public class YahooHistDataBuilder {
	private static final Logger logger = Logger.getLogger(YahooHistDataBuilder.class);
	
	private static final YahooHistDataBuilder instance = new YahooHistDataBuilder();
	
	private static final NumberFormat monthForm = new DecimalFormat("00");
	
	private static final DateFormat yahooDateForm = new SimpleDateFormat("dd-MMM-yy");

	
	WebConversation wc;

	private YahooHistDataBuilder() {
		wc = new WebConversation();
	}
	
	public static final YahooHistDataBuilder getInstance() {
		return instance;
	}
	
	public synchronized BarSeries getEODData(Instrument stock, int numDays) {
		if( ! stock.isStock() ) {
			// cannot gather non-stock data
			return null;
		}
		BarSeries series = new BarSeries(stock,BarType.time, TimeSpan.daily);
		try {

			// build request URL
			Calendar endCal = new GregorianCalendar();
			Calendar startCal = (Calendar)endCal.clone();
			startCal.add(Calendar.DATE, -1 * numDays);
			
			
			String urlString = "http://finance.yahoo.com/q/hp?s="
				+ stock.getSymbol()
				+ "&a=" + monthForm.format(startCal.get(Calendar.MONTH))
				+ "&b=" + startCal.get(Calendar.DAY_OF_MONTH)
				+ "&c=" + startCal.get(Calendar.YEAR)
				+ "&d=" + monthForm.format(endCal.get(Calendar.MONTH))
				+ "&e=" + endCal.get(Calendar.DAY_OF_MONTH)
				+ "&f=" + endCal.get(Calendar.YEAR)
				+ "&g=d";
			
			WebRequest req = new GetMethodWebRequest( urlString );
			WebResponse resp = wc.getResponse(req);
			
			while(true) {
				// repeatedly scan through the list until there are no more pages.
				// use an internal break.
				
				WebTable table = resp.getTableStartingWith("Date");
				// start at 2nd row (data) and skip last row (disclaimer)
				for (int i = 1; i < table.getRowCount()-1; i++) {
					Bar hist = new Bar(BarType.time, TimeSpan.daily);
					hist.setBeginTime(yahooDateForm.parse(table.getCellAsText(i, 0)));
					hist.setEndTime(
							new Date(hist.getBeginTime().getTime() + TimeSpan.daily.getSpanInMillis() - 1));
					hist.setOpen(Double.parseDouble(table.getCellAsText(i, 1)));
					hist.setHigh(Double.parseDouble(table.getCellAsText(i, 2)));
					hist.setLow(Double.parseDouble(table.getCellAsText(i, 3)));
					hist.setClose(Double.parseDouble(table.getCellAsText(i, 4)));
					
					String vol = table.getCellAsText(i, 5);
					vol = vol.replaceAll(",", "");
					hist.setVolume(Integer.parseInt(vol));
					
					series.addHistory(hist);
				}
				
				WebLink link = resp.getLinkWith("Next");
				if( link == null ) {
					break;
				} else {
					resp = link.click();
				}
				
			}
		} catch( Exception e ) {
			logger.error("Could not build historical price list for " + stock, e);
		}
		logger.debug("Found: " + series);

		return series;
	}

}
