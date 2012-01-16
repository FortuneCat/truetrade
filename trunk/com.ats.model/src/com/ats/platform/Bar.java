package com.ats.platform;

import java.text.*;
import java.util.*;

import com.ats.utils.Utils;

/**
 * Encapsulates the price bar information.
 */
public class Bar implements Comparable {
	
	private BarType barType;
	
	private TimeSpan span;
	
	// duration only used by volume bars
	// Mar 9 2007 - not currently supported or tested
	private int duration;
	
	private int id;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss MM/dd/yy");

    private double open = -1;
    private double high = -1;
    private double low = -1;
    private double close = -1;
    private int volume = 0;
    private Date beginTime;  // first trade time
    private Date endTime;    // last trade time
    private Date segmentEnd;
    private Date segmentStart;
    
    private Calendar cannonicalStart = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"));;
    
//    static {
//    	 cannonicalStart = 
//    	 cannonicalStart.set(Calendar.YEAR, 1980);
//    	 cannonicalStart.set(Calendar.DAY_OF_YEAR, 1);
//    	 cannonicalStart.set(Calendar.HOUR_OF_DAY, 0);
//    	 cannonicalStart.set(Calendar.MINUTE, 0);
//    	 cannonicalStart.set(Calendar.SECOND, 0);
//    	 cannonicalStart.set(Calendar.MILLISECOND, 0);
//    }

    
    public Bar() {
    	
    }
    
    public Bar(BarType barType, TimeSpan span) {
    	this.barType = barType;
    	this.span = span;
    }
    
    private Date getSegmentStart(Date date, TimeSpan span) {
    	cannonicalStart.setTime(date);
    	long delta = cannonicalStart.getTimeInMillis() %  span.getSpanInMillis();
    	cannonicalStart.add(Calendar.MILLISECOND, (int)(delta * -1));
    	return cannonicalStart.getTime();
    }
    
    public Bar(BarType barType, TimeSpan span, Date startTime) {
    	this.barType = barType;
    	this.span = span;
    	this.beginTime = startTime;
    	this.endTime = beginTime;
    	segmentStart = getSegmentStart(startTime, span); 
    	this.segmentEnd = new Date(segmentStart.getTime() + span.getSpanInMillis() - 1);
    }
    
    public Bar(BarType barType, TimeSpan span, int duration, long date, double open, double high,
			double low, double close, int volume) {
    	this.barType = barType;
    	this.span = span;
    	this.beginTime = new Date(date);
    	this.endTime = beginTime;
    	this.segmentStart = getSegmentStart(beginTime, span); 
    	this.segmentEnd = new Date(segmentStart.getTime() + span.getSpanInMillis() - 1);
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public Object clone() {
    	Bar bar = new Bar(barType, span, beginTime);
    	bar.high = high;
    	bar.low = low;
    	bar.open = open;
    	bar.close = close;
    	bar.volume = volume;
    	bar.segmentEnd = segmentEnd;
    	bar.endTime = endTime;
    	return bar;
    }
    
    public boolean contains(Date time) {
    	long delta = time.getTime() - beginTime.getTime();
    	return delta >= 0 && delta < span.getSpanInMillis();
    }

    public boolean canAdd(Trade trade) {
    	boolean canAdd = false;
    	switch( barType ) {
    	case volume:
    		canAdd = volume < duration;
    		break;
    	case tick:
    		canAdd = false;
    		break;
    	case time:
    		canAdd = (trade.getDateTime().before(segmentEnd) && trade.getDateTime().after(segmentStart));
    		break;
    	}
    	return canAdd;
    }
    
    public boolean canAdd(Bar bar) {
    	if( barType == BarType.time ) {
    		if(bar.getEndTime().before(segmentEnd) || bar.getEndTime().equals(segmentEnd)) {
    			return true;
    		}
    	}
    	return false;
    }

    public void add(Bar bar) {
    	// could merge this with canAdd() and return a value if 
    	// an add was unsuccessful, but this seemed neater
    	high = Math.max(high, bar.getHigh());
    	low = low <= 0 ? bar.getLow() : Math.min(low, bar.getLow());
		close = bar.getClose();
		setEndTime(bar.getEndTime());
    	volume += bar.getVolume();
    	if( open <= 0 ) {
    		open = bar.getOpen();
    		setBeginTime(bar.getBeginTime());
    	}
    }
    

    public void add(Trade trade) {
    	// could merge this with canAdd() and return a value if 
    	// an add was unsuccessful, but this seemed neater
    	high = Math.max(high, trade.getPrice());
    	low = low <= 0 ? trade.getPrice() : Math.min(low, trade.getPrice());
    	if( open <= 0 ) {
    		open = trade.getPrice();
    		close = trade.getPrice();
    		setBeginTime(trade.getDateTime());
    		setEndTime(trade.getDateTime());
    	} else if( segmentEnd.after(trade.getDateTime()) ) {
    		endTime = trade.getDateTime();
    		close = trade.getPrice();
    	}
    	volume += trade.getSize();
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(" Bar: startDate: " + Utils.timeAndDateFormat.format(beginTime) );
        sb.append(" endDate: " + Utils.timeAndDateFormat.format(segmentEnd) );
        sb.append(" open: " + open);
        sb.append(" high: " + high);
        sb.append(" low: " + low);
        sb.append(" close: " + close);

        return sb.toString();
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getClose() {
        return close;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public void setBeginTimeLong(long date) {
    	setBeginTime(new Date(date));
    }
    
    public int getVolume() {
        return volume;
    }

/*
    public static String durationToIbString(int barSizeSetting) throws RuntimeException {
        String barSize;

        switch (barSizeSetting) {
            case BAR_1_MINUTE:
                barSize = "1 min";
                break;
            case BAR_2_MINUTE:
                barSize = "2 mins";
                break;
            case BAR_3_MINUTE:
                barSize = "3 mins";
                break;
            case BAR_5_MINUTE:
                barSize = "5 mins";
                break;
            case BAR_15_MINUTE:
                barSize = "15 mins";
                break;
            case BAR_30_MINUTE:
                barSize = "30 mins";
                break;
            default:
                throw new RuntimeException("Bar size " + barSizeSetting + " is not supported");

        }

        return barSize;
    }


    public static int barSizeToSeconds(int barSize) {
        int minutes = 0;

        switch (barSize) {
            case BAR_1_MINUTE:
                minutes = 1;
                break;
            case BAR_2_MINUTE:
                minutes = 2;
                break;
            case BAR_3_MINUTE:
                minutes = 3;
                break;
            case BAR_5_MINUTE:
                minutes = 5;
                break;
            case BAR_15_MINUTE:
                minutes = 15;
                break;
            case BAR_30_MINUTE:
                minutes = 30;
                break;
        }

        return minutes * 60;
    }
*/
	public int compareTo(Object arg0) {
		if( ! (arg0 instanceof Bar ) ) {
			return -1;
		}
		Bar that = (Bar)arg0;
		return this.getBeginTime().compareTo(that.getBeginTime());
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
    	this.segmentStart = getSegmentStart(beginTime, span); 
    	this.segmentEnd = new Date(segmentStart.getTime() + span.getSpanInMillis() - 1);
	}

	public Date getEndTime() {
		if( endTime == null ) {
			return segmentEnd;
		} else {
			return endTime;
		}
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public TimeSpan getTimeSpan() {
		return span;
	}
	
	public void setTimespanId(int id) {
		this.span = TimeSpan.values()[id];
	}
	public int getTimespanId() {
		return this.span.ordinal();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
