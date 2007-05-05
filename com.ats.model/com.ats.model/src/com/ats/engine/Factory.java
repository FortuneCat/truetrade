package com.ats.engine;

public class Factory {
	public enum RuntimeMode {
		backtest,
		simulation,
		live
	}
	
	private OrderManager orderManager;
	private DataManager dataManager;
	
	private RuntimeMode mode = RuntimeMode.backtest;
	
	private static final Factory instance = new Factory();
	
	private Factory() {
		
	}
	
	public static final Factory getInstance() {
		return instance;
	}
	
	public void setMode(RuntimeMode mode) {
		this.mode = mode;
	}

	public synchronized OrderManager getOrderManager() {
		if( orderManager == null ) {
			switch ( mode ) {
			case backtest:
				orderManager = new BacktestOrderManager();
				break;
			case simulation:
				break;
			case live:
				orderManager = new IBOrderManager();
				break;
			}
		}
		return orderManager;
	}

	public synchronized DataManager getDataManager() {
		if( dataManager == null ) {
			switch ( mode ) {
			case backtest:
				dataManager = new BacktestDataManager();
				break;
			case simulation:
				break;
			case live:
				dataManager = IBDataManager.getInstance();
				break;
			}
		}
		return dataManager;
	}

}
