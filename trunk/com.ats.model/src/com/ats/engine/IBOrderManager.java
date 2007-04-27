package com.ats.engine;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ats.platform.JOrder;
import com.ats.platform.Strategy;
import com.ats.platform.JOrder.OrderState;
import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.Execution;
import com.ib.client.Order;

/**
 * Base class to manage orders.  Subclass this to handle simulated orders
 * as well as live orders
 * 
 * @author Adrian
 *
 */
public class IBOrderManager extends OrderManager {
	private static final Logger logger = Logger.getLogger(IBOrderManager.class);
	
    private final String host = "";
    private final int port = 7496;
    private final int clientID = 0;

    private EClientSocket socket;
    protected final Map<Integer, JOrder> orders = new HashMap<Integer, JOrder>();

    private String accountCode; // used to determine if TWS is running against real or paper trading account
    private int serverVersion;
    
    private static int orderID = 0;
    

    
    public void placeOrder(Strategy strategy, JOrder order) {
    	super.placeOrder(strategy, order);
    	orderID++;
    	order.setState(OrderState.pendingSubmit);
    	socket.placeOrder(orderID, order.getContract(), order.buildIBOrder());
    	String msg = order.getStrategy() + ": Placed order " + orderID;
    	logger.info(msg);
	}
    
    public synchronized void cancelOrder(JOrder order) {
    	socket.cancelOrder(order.getOrderId());
    	logger.info(order.getStrategy() + ": Submitted cancel order " + orderID);
	}


    public static void setOrderID(int id) {
        orderID = id;
    }


	public void execDetails(int orderId2, Contract contract, Execution execution) {
		JOrder order = orders.get(orderId2);
		
		if( order == null ) {
			// must have come from outside of the system
			return;
		}
		
		order.addExecution(execution);
		order.getStrategy().getPositionManager().addExecution(execution);

		if( order.isFilled() ) {
			double avgFillPrice = order.getAvgPrice();

			String msg = order.getStrategy() + ": " + "Order ID: " + orderId2
					+ " is filled.  Avg Fill Price: " + avgFillPrice;
			if( order.getText() != null && order.getText().length() > 0 ) {
				msg += ".  Msg: [" + order.getText() + "]";
			}
			orders.remove(orderId2);
			logger.info(msg);

			// TODO: update position manager
			PositionManager positionManager = order.getStrategy().getPositionManager();
			synchronized (positionManager) {
				positionManager.setAvgFillPrice(avgFillPrice);
				positionManager.notifyAll();
			}
		}
	}


	public void orderStatus(int orderId2, String status, int filled, int remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId2) {
		JOrder order = orders.get(orderId2);
		
		if( order == null ) {
			// must have come from outside of the system
			return;
		}
		if( "Submitted".equals(status) ) {
			order.setSubmitted();
		} else if( "Cancelled".equals(status) ) {
			order.setCancelled();
		}
	}


}
