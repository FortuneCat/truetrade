package com.ats.engine.ib;

import java.text.SimpleDateFormat;

import com.ats.engine.DataManager;
import com.ats.engine.OrderManager;
import com.ats.engine.StrategyDefinition;
import com.ats.engine.StrategyEngine;
import com.ats.platform.BarSeries;
import com.ats.platform.Instrument;
import com.ats.platform.JOrder;

public class IBStrategyEngine extends StrategyEngine {
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    
    private IBOrderManager orderHelper;

    public IBStrategyEngine(StrategyDefinition definition, Instrument instrument, OrderManager orderManager, DataManager dataManager) {
		super(definition, instrument, orderManager, dataManager);
		this.orderHelper = (IBOrderManager)orderManager;
		dataManager.addTickListener(instrument, this);
	}

    
	@Override
	public void cancelOrder(JOrder order) {
		orderHelper.cancelOrder(order);
	}


	@Override
	public void placeOrder(JOrder order) {
		orderHelper.placeOrder(getStrategy(), order);
	}


	protected void backfillBarSeries(BarSeries series) {
		IBDataManager.getInstance().reqHistData(getInstrument(), series);
	}


	@Override
	protected void initDataConnection() {
		IBDataManager.getInstance().reqMktData(getInstrument(), this);
	}


	@Override
	protected void executeStrategy() {
		// TODO Auto-generated method stub
		
	}



	public IBOrderManager getOrderHelper() {
		return orderHelper;
	}



	public void setOrderHelper(IBOrderManager orderHelper) {
		this.orderHelper = orderHelper;
	}

}
