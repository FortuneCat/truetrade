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

/**
 * Base class to manage orders.  Subclass this to handle simulated orders
 * as well as live orders
 * 
 * @author Adrian
 *
 */
public class IBOrderManager extends OrderManager {
	private static final Logger logger = Logger.getLogger(IBOrderManager.class);
	
    protected final Map<Integer, JOrder> orders = new HashMap<Integer, JOrder>();

    private static int orderID = 0;
    
    public IBOrderManager() {
    	IBWrapperAdapter.setIBOrderManager(this);
    }
    
    public void placeOrder(Strategy strategy, JOrder order) {
    	super.placeOrder(strategy, order);
    	orderID++;
    	orders.put(orderID, order);
    	order.setState(OrderState.pendingSubmit);
    	IBHelper.getInstance().placeOrder(orderID, order.getInstrument().getContract(), order.buildIBOrder());
    	String msg = order.getStrategy() + ": Placed order " + orderID;
    	logger.info(msg);
	}
    
    public synchronized void cancelOrder(JOrder order) {
    	IBHelper.getInstance().cancelOrder(order.getOrderId());
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
		JExecution jexec = new JExecution(execution);
		jexec.setInstrument(order.getInstrument());
		PositionManager.getInstance().execution(order, jexec);

		if( order.isFilled() ) {
			double avgFillPrice = order.getAvgPrice();

			String msg = order.getStrategy() + ": " + "Order ID: " + orderId2
					+ " is filled.  Avg Fill Price: " + avgFillPrice;
			if( order.getText() != null && order.getText().length() > 0 ) {
				msg += ".  Msg: [" + order.getText() + "]";
			}
			completeOrder(order);
			logger.info(msg);
		}
	}


	public void orderStatus(int orderId2, String status, int filled, int remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId2) {
		JOrder order = orders.get(orderId2);
		
		if( order == null ) {
			logger.info("External order status change: " + orderId2 + " " + status + " clientid = " + clientId2);
			return;
		}
		logger.info("Order id " + orderId2 + " status changed to " + status);
		if( "Submitted".equals(status) ) {
			order.setSubmitted();
		} else if( "Cancelled".equals(status) ) {
			order.setCancelled();
		} else if( "PreSubmitted".equals(status) ) {
			order.setState(OrderState.pendingSubmit);
		} 
	}

	public synchronized void orderError(int orderId2, int code) {
		JOrder order = orders.get(orderId2);
		
		if( order == null ) {
			logger.info("External order error: " + orderId2 + " " + code);
			return;
		}
		logger.info("Order id " + orderId2 + " error with code " + code);
		// TODO: cancelled or a full error state?
		order.setCancelled();
		
		// remove it from all outstanding lists
		orders.remove(orderId2);
		completeOrder(order);
	}


}
