package com.ats.engine;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.ats.platform.BarSeries;
import com.ats.platform.Instrument;
import com.ats.platform.JOrder;
import com.ats.platform.BaseSystemException;
import com.ats.platform.Strategy;
import com.ib.client.Contract;

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
