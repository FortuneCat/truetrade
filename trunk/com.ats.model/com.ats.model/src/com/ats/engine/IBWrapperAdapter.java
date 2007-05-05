package com.ats.engine;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ats.platform.MessageListener;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.Execution;
import com.ib.client.Order;

/**
 * This class acts as a "wrapper" in IB's API terminology.
 */
public class IBWrapperAdapter extends EWrapperAdapter {
	
	private static IBOrderManager ibOrderManager;

	private static final Logger logger = Logger.getLogger(IBWrapperAdapter.class);
	
    //static volatile boolean isPendingHistRequest;
    //private final TraderAssistant traderAssitant;
    
    private static final IBWrapperAdapter wrapper = new IBWrapperAdapter();
    
    private final List<MessageListener> errorListeners = new ArrayList<MessageListener> ();
    
    private IBWrapperAdapter() {
//        traderAssitant = new TraderAssistant(this);
//        traderAssitant.connect();
//        traderAssitant.realAccountCheck();
    }
    
    public static final IBWrapperAdapter getWrapper() {
    	return wrapper;
    }

    public void addMessageListener(MessageListener errorListener) {
        if (!errorListeners.contains(errorListener)) {
            errorListeners.add(errorListener);
        }
    }

    public synchronized void removeErrorListener(MessageListener errorListener) {
        errorListeners.remove(errorListener);
    }


    public void fireError(int id, int errorCode, String errorMsg) {
        for (MessageListener listener : errorListeners) {
            listener.error(id, errorCode, errorMsg);
        }
    }
    
    public void fireNewsBulletin(int msgId, int msgType, String message, String origExchange) {
        for (MessageListener listener : errorListeners) {
            listener.updateNewsBulletin(msgId, msgType, message, origExchange);
        }
    }


    public void updateAccountValue(String key, String value, String currency, String accountName) {
        try {
            if (key.equalsIgnoreCase("AccountCode")) {
                IBHelper.getInstance().setAccountCode(value);
                synchronized (this) {
                    notifyAll();
                }
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            logger.error("Error", t);
        }
    }

    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
        String newsBulletin = "Msg ID: " + msgId + " Msg Type: " + msgType + " Msg: " + message + " Exchange: " +
                              origExchange;
        logger.info(newsBulletin);
        fireNewsBulletin(msgId, msgType, message, origExchange);
    }

    public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume,
            int count, double WAP, boolean hasGaps) {
    	IBDataManager.getInstance().historicalData(reqId, date, open, high, low, close, volume, WAP, hasGaps);
    }

    @Override
    public void execDetails(int orderId, Contract contract, Execution execution) {
    	if( ibOrderManager == null ) {
    		return;
    	}
        try {
        	ibOrderManager.execDetails(orderId, contract, execution);
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            logger.error("Error getting execution details", t);
        }
    }
    
    @Override
	public void tickSize(int tickerId, int field, int size) {
    	IBDataManager.getInstance().tickSize(tickerId, field, size);
    }

	public void tickPrice(int reqId, int tickType, double price, int canAutoExecute) {
    	IBDataManager.getInstance().tickPrice(reqId, tickType, price, canAutoExecute);
    }

    public void error(Exception ex) {
    	ex.printStackTrace();
        if (!ex.getMessage().contains("socket closed")) {
            logger.error("Socket error", ex);
        }
    }

    public void error(int id, int errorCode, String errorMsg) {
        try {
            String msg = id + " | " + errorCode + ": " + errorMsg;
            logger.info("Error: " + msg);

            // check for a code which renders an order null
            if( errorCode >= 103 && errorCode <= 161  ) {
            	// a problem occured with the order
            	if( ibOrderManager != null ) {
            		ibOrderManager.orderError(id, errorCode);
            	}
            	return;
            }
            if( errorCode >= 162 && errorCode <= 200 ) {
            	// historical data problem
            	return;
            }
            
            

            // Error 1101 is fired when connection between IB and TWS is restored
            // after a temporary connection loss and the data is "lost". In this case,
            // we need to resubscribe to market data.
            if (errorCode == 1101) {
                IBHelper.getInstance().resubscribeToMarketData();
            }

            // If either error 1101 or 1102 was fired, it means TWS was disconnected
            // from the IB server, so some orders could have been executed during
            // that time. In this case, we need to request executions.
            if (errorCode == 1101 || errorCode == 1102) {
            	// TODO: reset our positions
                //traderAssitant.resetOrders();
            	IBHelper.getInstance().requestExecutions();
            }

            fireError(id, errorCode, errorMsg);

        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            logger.error("Unexpected error", t);
        }
    }
    
	@Override
	public void error(String str) {
		fireError(0, 0, str);
	}

    
    @Override
	public void orderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId) {
    	if( ibOrderManager == null ) {
    		return;
    	}
    	ibOrderManager.orderStatus(orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId);
	}
    
	@Override
	public void openOrder(int orderId, Contract contract, Order order) {
		logger.debug("open order: " + contract + ", order=" + order);
	}




    public void nextValidId(int orderID) {
        //traderAssitant.setOrderID(orderID);
    	IBOrderManager.setOrderID(orderID);
    }

	@Override
	public void connectionClosed() {
		// TODO Auto-generated method stub
		super.connectionClosed();
	}

	@Override
	public void contractDetails(ContractDetails contractDetails) {
		// TODO Auto-generated method stub
		super.contractDetails(contractDetails);
	}

	@Override
	public void tickString(int tickerId, int tickType, String value) {
	}

	@Override
	public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
		// TODO Auto-generated method stub
		super.updateMktDepth(tickerId, position, operation, side, price, size);
	}

	@Override
	public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price, int size) {
		// TODO Auto-generated method stub
		super.updateMktDepthL2(tickerId, position, marketMaker, operation, side, price,
				size);
	}

	@Override
	public void updatePortfolio(Contract contract, int position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
		// TODO Auto-generated method stub
		super.updatePortfolio(contract, position, marketPrice, marketValue,
				averageCost, unrealizedPNL, realizedPNL, accountName);
	}

	public static void setIBOrderManager(IBOrderManager manager) {
		ibOrderManager = manager;
	}
}
