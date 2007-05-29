package com.ats.platform;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ats.engine.DataManager;
import com.ats.engine.IBStrategyEngine;
import com.ats.engine.OrderManager;
import com.ats.engine.StrategyDefinition;

/**
 * Supplies Instruments to strategies at runtime.
 * 
 * For example, the SIS may poll different web sites after market open to gather
 * a list of gappers.  The SIS must construct valid Instruments and then pass them
 * along to the TT framework for use by the different strategies.  The SIS will not be
 * consulted for backtesting but only at runtime.
 * 
 * It is meant to be subclassed.  The subclasses are responsible for pushing instruments
 * into the framework, supplying events rather than consuming them as happens in
 * Strategies.
 * 
 * @author Adrian
 *
 */
public abstract class StrategyInstrumentSource implements Runnable {
	private static final Logger logger = Logger.getLogger(StrategyInstrumentSource.class);
	
	private StrategyDefinition definition;
	private OrderManager orderManager;
	private DataManager dataManager;
	
	private List<Instrument> pastInstruments = new ArrayList<Instrument>();
	
	public StrategyInstrumentSource() {
	}
	
	
	/**
	 * Invoked by children to add an instrument to their controlling
	 * strategy.
	 * 
	 * @param instrument
	 */
	public synchronized final void addInstrument(Instrument instrument) {
		if( pastInstruments.contains(instrument)) {
			logger.info("Instrument already added, passing: " + instrument);
			return;
		}
		pastInstruments.add(instrument);
		try {
			IBStrategyEngine bse = new IBStrategyEngine(definition, instrument, orderManager, dataManager);
			Thread t = new Thread(bse);
			t.start();
		} catch( Exception e) {
			logger.error("Could not start strategy [" + definition + "] for [" + instrument + "]: " + e, e);
		}
	}
	
	/**
	 * Subclasses must extend this and add in any scanning or monitoring code
	 * necessary.  Whenever an event occurs and a new instrument is ready,
	 * call addInstrument()
	 */
	public abstract void run();


	public void setDataManager(DataManager dataManager) {
		this.dataManager = dataManager;
	}


	public void setDefinition(StrategyDefinition definition) {
		this.definition = definition;
	}


	public void setOrderManager(OrderManager orderManager) {
		this.orderManager = orderManager;
	}

}
