package com.ats.engine.ib;

import com.ats.platform.BarSeries;
import com.ats.platform.Instrument;

public class HistoricalDataRequest extends IBRequest {

	private Instrument instrument;
	private String endDateTime;
	private String durationStr;
	private int barSizeSetting;
	private String whatToShow;
	private int useRTH;
	private int formatDate;
	
	private BarSeries barSeries;
	
	public HistoricalDataRequest(RequestListener listener, BarSeries series, Instrument instrument, String endDateTime, String durationStr, int barSizeSetting, String whatToShow, int useRTH, int formatDate) {
		super(listener);
		this.barSeries = series;
		this.instrument = instrument;
		this.endDateTime = endDateTime;
		this.durationStr = durationStr;
		this.barSizeSetting = barSizeSetting;
		this.whatToShow = whatToShow;
		this.useRTH = useRTH;
		this.formatDate = formatDate;
	}

	@Override
	protected void doRequest(IBHelper helper) {
		helper.reqHistoricalData(getId(), instrument, endDateTime, durationStr,
				barSizeSetting, whatToShow, useRTH, formatDate);
	}

	public void setBarSizeSetting(int barSizeSetting) {
		this.barSizeSetting = barSizeSetting;
	}

	public void setDurationStr(String durationStr) {
		this.durationStr = durationStr;
	}

	public void setEndDateTime(String endDateTime) {
		this.endDateTime = endDateTime;
	}

	public void setFormatDate(int formatDate) {
		this.formatDate = formatDate;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	public void setUseRTH(int useRTH) {
		this.useRTH = useRTH;
	}

	public void setWhatToShow(String whatToShow) {
		this.whatToShow = whatToShow;
	}

	public BarSeries getBarSeries() {
		return barSeries;
	}

	public void setBarSeries(BarSeries barSeries) {
		this.barSeries = barSeries;
	}

}
