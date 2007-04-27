package com.ats.engine;

import java.util.Date;

import org.apache.log4j.Logger;

import com.ats.platform.Bar;
import com.ats.platform.BarSeries;
import com.ats.platform.Instrument;
import com.ats.platform.TimeSpan;
import com.ats.platform.Trade;
import com.ats.platform.Bar.BarType;

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
