package com.ats.strategy;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.ats.platform.Instrument;
import com.ats.platform.StrategyInstrumentSource;
import com.ats.platform.Instrument.InstrumentType;
import com.ats.utils.Utils;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;

public class GapInstrumentSource extends StrategyInstrumentSource {
	private static final int MIN_INITIAL_VOLUME = 150000;
	private static final int MIN_PRICE = 10;
	private static final Logger logger = Logger.getLogger(GapInstrumentSource.class);

	public GapInstrumentSource() {
		super();
	}

	@Override
	public void run() {
		// Check the gap list 15 minutes after the market opens
		Calendar cal = new GregorianCalendar(Utils.nyseTimeZone);
		cal.set(Calendar.HOUR_OF_DAY, 9);
		cal.set(Calendar.MINUTE, 45);
		Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {
			public void run() {
				runGapScan();
			}
		}, cal.getTime());

	}
	
	private void runGapScan() {
		HttpUnitOptions.setScriptingEnabled( false );
		runQuoteScan("http://quote.com/qc/research/hotlist.aspx?hotlist='NASDAQ+Stocks+Unfilled+Gaps'&symbols=");
		runQuoteScan("http://quote.com/qc/research/hotlist.aspx?hotlist='NYSE+Stocks+Unfilled+Gaps'");
//		runQuoteScan("http://quote.com/qc/research/hotlist.aspx?hotlist='NASDAQ+Stocks+Unusual+Volume'&symbols='");
	}

	/**
	 * Scan quote.com for gaps or unusual volume
	 * 
	 * @param urlString
	 */
	private void runQuoteScan(String urlString) {
		WebConversation wc = new WebConversation();
		
		try {
			WebResponse resp = wc
					.getResponse(new GetMethodWebRequest(urlString));

			WebTable table = resp.getTableStartingWith("Company");
			if( table == null || table.getRowCount() <= 1 ) {
				logger.warn("Could not get gap table");
				return;
			}
			for( int i = 1; i < table.getRowCount(); i++ ) {
				double last = Double.parseDouble(table.getCellAsText(i, 2));
				String volString = table.getCellAsText(i, 4);
				volString = volString.replaceAll(",", "");
				int volume = Integer.parseInt(volString);
				if( last < MIN_PRICE || volume < MIN_INITIAL_VOLUME ) {
					// didn't meet minimum criteria, move on
					continue;
				}
				// meets criteria, so build an instrument and add
				Instrument instr = new Instrument();
				String symbol = table.getCellAsText(i, 1);
				instr.setSymbol(symbol);
				instr.setCurrency("USD");
				instr.setExchange("SMART");
				instr.setInstrumentType(InstrumentType.stock);
				addInstrument(instr);
			}
		} catch (Exception e) {
			logger.error("Could not run gap scan", e);
		}
	}
	
}
