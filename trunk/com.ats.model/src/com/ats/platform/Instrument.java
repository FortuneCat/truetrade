package com.ats.platform;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ib.client.Contract;

public class Instrument {
	private static final Logger logger = Logger.getLogger(Instrument.class);
	
	public static final String EXCHANGES[] = new String[]{"SMART", 
		"GLOBEX", "ECBOT", "CBOE", "IDEALPRO", "IDEAL",
		"NYSE", "NASDAQ", "AMEX",
		"NYMEX", "LIFFE"};

	public enum InstrumentType {
		stock("STK"),
		option("OPT"),
		future("FUT"),
		futureOption("FOP"),
		index("IND"),
		forex("CASH"),
		bond("BAG")
		;
		
		private String ibType;
		private InstrumentType(String ibType) {
			this.ibType = ibType;
			types.put(ibType, this);
		}
		public String getIbType() {
			return ibType;
		}
		public static InstrumentType getType(String code) {
			return types.get(code);
		}
	}
	private static final Map<String, InstrumentType> types = new HashMap<String, InstrumentType>();
	
	private Contract contract;
	
	private int id;
	private double tickSize = 0.01;
	private int multiplier = 1;
	
	public Instrument() {
		contract = new Contract();
	}
	public Instrument(Contract contract) {
		this.contract = contract;
	}
	
	@Override
	public boolean equals(Object obj) {
		if( obj == null || !(obj instanceof Instrument)) {
			return false;
		}
		Instrument that = (Instrument)obj;
		return this.getSymbol().equals(that.getSymbol())
			&& this.getExchange().equals(that.getExchange())
			&& this.getCurrency().equals(that.getCurrency());
	}
	
	@Override
	public int hashCode() {
		return this.getSymbol().hashCode() * this.getExchange().hashCode()
				* this.getCurrency().hashCode() * 19;
	}
	
	
	public String toString() {
		return "Instrument[symbol="+getSymbol()+",type="+getInstrumentType()+"]";
	}
	
	public boolean isStock() {
		return getInstrumentType().equals(InstrumentType.stock);
	}
	public boolean isFuture() {
		return getInstrumentType().equals(InstrumentType.future);
	}
	public boolean isForex() {
		return getInstrumentType().equals(InstrumentType.forex);
	}

	
	public String getSymbol() {
		return contract.m_symbol;
	}
	public void setSymbol(String s) {
		contract.m_symbol = s.toUpperCase();
	}

	public String getExchange() {
		return contract.m_exchange;
	}
	public void setExchange(String s) {
		contract.m_exchange = s;
	}
	
	public String getCurrency() {
		return contract.m_currency;  
	}
	public void setCurrency(String s) {
		contract.m_currency = s;
	}
	
	public String getPrimaryExchange() {
		return contract.m_primaryExch;
	}
	public void setPrimaryExchange(String s) {
		contract.m_primaryExch = s;
	}
	
	public InstrumentType getInstrumentType() {
		return InstrumentType.getType(contract.m_secType);
	}
	public void setInstrumentType(InstrumentType it) {
		contract.m_secType = it.getIbType();
	}
	/**
	 * supplied only for use of database persistence
	 * @param type
	 */
	@Deprecated
	public void setInstrumentTypeString(String type) {
		contract.m_secType = type;
	}
	
	@Deprecated
	public Contract getContract() {
		return contract;
	}
	@Deprecated
	public int getId() {
		return id;
	}
	@Deprecated
	public void setId(int id) {
		this.id = id;
	}
	public int getMultiplier() {
		return multiplier;
	}
	public void setMultiplier(int multiplier) {
		this.multiplier = multiplier;
	}
	public double getTickSize() {
		return tickSize;
	}
	public void setTickSize(double tickSize) {
		this.tickSize = tickSize;
	}
}
