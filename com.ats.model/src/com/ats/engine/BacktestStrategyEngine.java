package com.ats.engine;

import org.apache.log4j.Logger;

import com.ats.platform.Instrument;

public class BacktestStrategyEngine extends StrategyEngine {
	private static final Logger logger = Logger.getLogger(BacktestStrategyEngine.class);

	
	public BacktestStrategyEngine(StrategyDefinition definition, Instrument instrument, OrderManager orderManager, DataManager dataManager) {
		super(definition, instrument, orderManager, dataManager);
	}
	

	@Override
	protected void executeStrategy() {
	}

	@Override
	protected void initDataConnection() {
		// TODO Auto-generated method stub
		
	}
	
	
}
