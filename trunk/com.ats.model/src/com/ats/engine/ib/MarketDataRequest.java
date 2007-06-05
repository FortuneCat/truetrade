package com.ats.engine.ib;

import com.ats.platform.Instrument;

public class MarketDataRequest extends IBRequest {
	
	private Instrument instrument;
	private IBQuote quote;

	public MarketDataRequest(RequestListener listener, Instrument instrument) {
		super(listener);
		this.instrument = instrument;
		quote = new IBQuote();
	}

	@Override
	protected void doRequest(IBHelper helper) {
		helper.reqMktData(getId(), instrument);
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	public IBQuote getQuote() {
		return quote;
	}

	public void setQuote(IBQuote quote) {
		this.quote = quote;
	}
}
