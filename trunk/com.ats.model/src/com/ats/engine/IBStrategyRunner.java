package com.ats.engine;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.ats.platform.BarSeries;
import com.ats.platform.JOrder;
import com.ats.platform.JSystemTraderException;
import com.ats.platform.Strategy;
import com.ib.client.Contract;

public class IBStrategyRunner extends StrategyEngine {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    
    private static final IBOrderManager orderHelper = IBOrderManager.getInstance();
    
    private static final int nextHistDataId = 1;

	public IBStrategyRunner(Strategy strategy) throws JSystemTraderException {
		super(strategy);
	}


    private void getMarketData() {
    	IBDataManager.getInstance().reqMktData(strategy.getContract(), this);
    }
    
    

	@Override
	public void cancelOrder(JOrder order) {
		orderHelper.cancelOrder(order);
	}


	@Override
	public void placeOrder(JOrder order) {
		orderHelper.placeOrder(order);
	}


	protected void backfillBarSeries(BarSeries series) {
		IBDataManager.getInstance().reqHistData(strategy.getContract(), series, this);
	}


	@Override
	protected void initDataConnection() {
		IBDataManager.getInstance().reqMktData(strategy.getContract(), this);
	}

}
