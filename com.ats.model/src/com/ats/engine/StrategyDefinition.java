package com.ats.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ats.platform.Instrument;
import com.ats.platform.Strategy;
import com.ats.platform.TimeSpan;

/**
 * The necessary definition and parameters of a strategy, for
 * use by the engines.
 * 
 * @author Adrian
 *
 */
public class StrategyDefinition {
	private static final Logger logger = Logger.getLogger(StrategyDefinition.class);
	
	private List<Instrument> instruments = new ArrayList<Instrument>();
	
	private int id;
	
	private boolean isRuntime;
	
	/** the time span of loaded data used in backtesting */
	private TimeSpan backtestDataTimeSpan = TimeSpan.daily;
	
	/** the level of detail which will be simulated during backtest runs.
	 *  Should be smaller scale than the backtestDataTimeSpan */
	private TimeSpan backtestSimulatedTimeSpan = TimeSpan.daily;

	/**
	 * the params which will be applied to the strategy before execution
	 */
    private Map<String, Number> params = new HashMap<String, Number>();

	
	private Class strategyClass;
	
	public StrategyDefinition() {
		
	}
	
	public String toString() {
		return "StrategyDefinition: [class=" + strategyClass.getCanonicalName() 
			+ ", id=" + id
			+", instruments=" + instruments + "]";
	}
	
	public void addInstrument(Instrument instr) {
		instruments.add(instr);
	}
	
	public void removeInstrument(Instrument instrument) {
		instruments.remove(instrument);
	}
	
//	public Class getStrategyClass() {
//		return strategy.getClass();
//	}
	public Strategy getStrategy(Instrument instrument) {
		Strategy strategy = null;
		try {
			strategy = (Strategy)strategyClass.newInstance();
			strategy.setInstrument(instrument);
			for(String key : params.keySet()) {
				strategy.addParam(key, params.get(key));
			}
		} catch (Exception e) {
			logger.error("Could not instantiate strategy", e);
		}
		return strategy;
	}
	
	public Class getStrategyClass() {
		return strategyClass;
	}
	public String getStrategyClassName() {
		return strategyClass.getCanonicalName();
	}
	public void setStrategyClassName(String name) {
		if( name == null || name.length() <= 0 ) {
			logger.debug("Null strategy class name passed to StrategyDefinition");
			return;
		}
		try {
			strategyClass = Class.forName(name);
			Strategy strategy = (Strategy)strategyClass.newInstance();
			for(String key : strategy.getParamNames()) {
				params.put(key, strategy.getParam(key));
			}
		} catch (Exception e) {
			logger.error("Could not instantiate Strategy class[" + name + "]", e);
		}
	}
	
	public Set<String> getParameterNames() {
		return params.keySet();
	}
	public void setParameter(String paramName, Number value) {
		params.put(paramName, value);
	}
	public Number getParameter(String paramName) {
		return params.get(paramName);
	}

	public List<Instrument> getInstruments() {
		return instruments;
	}
	public void setInstruments(List<Instrument> instruments) {
		this.instruments = instruments;
	}


	public TimeSpan getBacktestDataTimeSpan() {
		return backtestDataTimeSpan;
	}

	public void setBacktestDataTimeSpan(TimeSpan backtestDataTimeSpan) {
		this.backtestDataTimeSpan = backtestDataTimeSpan;
	}

	public TimeSpan getBacktestSimulatedTimeSpan() {
		return backtestSimulatedTimeSpan;
	}

	public void setBacktestSimulatedTimeSpan(TimeSpan backtestSimulatedTimeSpan) {
		this.backtestSimulatedTimeSpan = backtestSimulatedTimeSpan;
	}
	
	public Integer getBacktestDataTimeSpanId() {
		if( backtestDataTimeSpan == null ) {
			return null;
		}
		return backtestDataTimeSpan.ordinal();
	}
	public void setBacktestDataTimeSpanId(int id) {
		backtestDataTimeSpan = TimeSpan.values()[id];
	}
	public Integer getBacktestSimulatedTimeSpanId() {
		if( backtestSimulatedTimeSpan == null ) {
			return null;
		}
		return backtestSimulatedTimeSpan.ordinal();
	}
	public void setBacktestSimulatedTimeSpanId(int id) {
		backtestSimulatedTimeSpan = TimeSpan.values()[id];
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isRuntime() {
		return isRuntime;
	}

	public void setRuntime(boolean isRuntime) {
		this.isRuntime = isRuntime;
	}
}
