package com.ats.client.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ats.db.PlatformDAO;
import com.ats.platform.Bar;
import com.ats.platform.BarSeries;
import com.ats.platform.BarType;
import com.ats.platform.Instrument;
import com.ats.platform.TimeSpan;

public class ImportTemplate implements java.io.Serializable {
	private static final Logger logger = Logger.getLogger(ImportTemplate.class);
	
	private String delimiter;
	private int numHeaderLines;
	private TimeSpan timeSpan;
	private String dateFormatString;
	private String timeFormatString;
	private boolean isManualDate;
	private Date date;
	
	private int dateCol = -1;
	private int beginTimeCol = -1;
	private int endTimeCol = -1;
	private int highCol = -1;
	private int lowCol = -1;
	private int openCol = -1;
	private int closeCol = -1;
	private int volumeCol = -1;

	private boolean volAsHundreds;

	public int loadBarsFromFile(String fileName, Instrument instrument, IProgressMonitor monitor) {
		int barCount = 0;
		BarSeries series = PlatformDAO.getBarSeries(instrument, timeSpan);
		if( series == null ) {
			series = new BarSeries(instrument, BarType.time, timeSpan);
			PlatformDAO.insertBarSeries(series);
		}
		
		DateFormat dateFormat = null;
		if( timeFormatString != null && dateFormatString != null ) {
			dateFormat = new SimpleDateFormat(dateFormatString + " " + timeFormatString);
		}
		try {
			File file = new File(fileName);
			long length = file.length(); 
			BufferedReader bis = new BufferedReader( new FileReader(fileName) );
			int lineCount = 0;
			for( String line = bis.readLine(); line != null && line.length() > 0; line = bis.readLine() ) {
				if( lineCount < numHeaderLines ) {
					// eat the header lines
					lineCount++;
					monitor.worked(line.length()/10);
					continue;
				}
				Bar bar = new Bar(BarType.time, timeSpan);
				StringTokenizer st = new StringTokenizer(line, delimiter);
				String beginTime = null;
				String endTime = null;
				String date = null;
				for( int i = 0; st.hasMoreElements(); i++ ) {
					
					String currBlock = st.nextToken().trim();
					if( i == dateCol ) {
						date = currBlock;
					} else if( i == beginTimeCol ) {
						beginTime = currBlock;
					} else if( i == endTimeCol ) {
						endTime = currBlock;
					} else if( i == highCol ) {
						bar.setHigh(Double.parseDouble(currBlock));
					} else if( i == lowCol ) {
						bar.setLow(Double.parseDouble(currBlock));
					} else if( i == openCol ) {
						bar.setOpen(Double.parseDouble(currBlock));
					} else if( i == closeCol ) {
						bar.setClose(Double.parseDouble(currBlock));
					} else if( i == volumeCol ) {
						int vol = (int)Double.parseDouble(currBlock);
						if( volAsHundreds ) {
							vol *= 100;
						}
						bar.setVolume(vol);
					}
				}
				if( beginTime != null ) {
					bar.setBeginTime( dateFormat.parse(date + " " + beginTime ));
				} else {
					bar.setBeginTime( dateFormat.parse(date + " 00:00"));
				}
				if( endTime != null ) {
					bar.setEndTime(dateFormat.parse(date + " " + endTime));
				}
				// check for regular trading hours
//				if( bar.getBeginTime().getHours() >= 6 && bar.getBeginTime().getHours() < 13 ) {
					// TODO: PST only!
					PlatformDAO.insertBar(series, bar);
					// TODO: only increment if no errors
//				}
				barCount++;
				
				lineCount++;
				monitor.worked(line.length()/10);
			}
		} catch( Exception e) {
			logger.error("Error parsing template", e);
		}
		return barCount;
	}

	public int getBeginTimeCol() {
		return beginTimeCol;
	}

	public void setBeginTimeCol(int beginTimeCol) {
		this.beginTimeCol = beginTimeCol;
	}

	public int getCloseCol() {
		return closeCol;
	}

	public void setCloseCol(int closeCol) {
		this.closeCol = closeCol;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getDateCol() {
		return dateCol;
	}

	public void setDateCol(int dateCol) {
		this.dateCol = dateCol;
	}

	public String getDateFormatString() {
		return dateFormatString;
	}

	public void setDateFormatString(String dateFormatString) {
		this.dateFormatString = dateFormatString;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public int getEndTimeCol() {
		return endTimeCol;
	}

	public void setEndTimeCol(int endTimeCol) {
		this.endTimeCol = endTimeCol;
	}

	public int getHighCol() {
		return highCol;
	}

	public void setHighCol(int highCol) {
		this.highCol = highCol;
	}

	public boolean isManualDate() {
		return isManualDate;
	}

	public void setManualDate(boolean isManualDate) {
		this.isManualDate = isManualDate;
	}

	public int getLowCol() {
		return lowCol;
	}

	public void setLowCol(int lowCol) {
		this.lowCol = lowCol;
	}

	public int getNumHeaderLines() {
		return numHeaderLines;
	}

	public void setNumHeaderLines(int numHeaderLines) {
		this.numHeaderLines = numHeaderLines;
	}

	public int getOpenCol() {
		return openCol;
	}

	public void setOpenCol(int openCol) {
		this.openCol = openCol;
	}

	public String getTimeFormatString() {
		return timeFormatString;
	}

	public void setTimeFormatString(String timeFormatString) {
		this.timeFormatString = timeFormatString;
	}

	public TimeSpan getTimeSpan() {
		return timeSpan;
	}

	public void setTimeSpan(TimeSpan timeSpan) {
		this.timeSpan = timeSpan;
	}

	public int getVolumeCol() {
		return volumeCol;
	}

	public void setVolumeCol(int volumeCol) {
		this.volumeCol = volumeCol;
	}

	public void setVolAsHundreds(boolean selection) {
		this.volAsHundreds = selection;
	}
	public boolean getVolAsHundreds() {
		return this.volAsHundreds;
	}
}
