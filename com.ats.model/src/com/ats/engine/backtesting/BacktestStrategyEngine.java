package com.ats.engine.backtesting;

import org.apache.log4j.Logger;

import com.ats.engine.DataManager;
import com.ats.engine.OrderManager;
import com.ats.engine.StrategyDefinition;
import com.ats.engine.StrategyEngine;
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
