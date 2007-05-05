package com.ats.client.tools;

import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.OHLCDataset;

import com.ats.platform.BarSeries;

public class BarSeriesDataset extends AbstractXYDataset implements OHLCDataset {
	
	private BarSeries data;
	
	public BarSeriesDataset(BarSeries series) {
		this.data = series;
	}
	public Number getClose(int series, int item) {
		return this.data.itemAt(item).getClose();
	}

	public double getCloseValue(int series, int item) {
	    double result = Double.NaN;
        Number n = getClose(series, item);
        if (n != null) {
            result = n.doubleValue();   
        }
        return result;   
	}

	public Number getHigh(int series, int item) {
		return this.data.itemAt(item).getHigh();
	}

	public double getHighValue(int series, int item) {
	    double result = Double.NaN;
        Number high = getHigh(series, item);
        if (high != null) {
            result = high.doubleValue();   
        }
        return result;   
	}

	public Number getLow(int series, int item) {
        return this.data.itemAt(item).getLow();
	}

	public double getLowValue(int series, int item) {
		double result = Double.NaN;
		Number low = getLow(series, item);
		if (low != null) {
			result = low.doubleValue();
		}
		return result;
	}

	public Number getOpen(int series, int item) {
		return this.data.itemAt(item).getOpen();
	}

	public double getOpenValue(int series, int item) {
		double result = Double.NaN;
		Number open = getOpen(series, item);
		if (open != null) {
			result = open.doubleValue();
		}
		return result;
	}

	public Number getVolume(int series, int item) {
		return this.data.itemAt(item).getVolume();
	}

	public double getVolumeValue(int series, int item) {
	    double result = Double.NaN;
        Number n = getVolume(series, item);
        if (n != null) {
            result = n.doubleValue();   
        }
        return result;   
	}

	public int getItemCount(int series) {
		return this.data.size();
	}

	public Number getX(int series, int item) {
		return new Long(data.itemAt(item).getBeginTime().getTime());
	}
	
	public Number getY(int series, int item) {
		return getClose(series, item);
	}
	public int getSeriesCount() {
		return 1;
	}

	public Comparable getSeriesKey(int series) {
		return "my key";
//		Comparable c = new Comparable() {
//			public int compareTo(Object o) {
//				logger.debug("compareTo called with " + o.getClass() + ": " + o );
//				return -1;
//			}
//		};
//		return c;
	}

}
