package com.ats.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;

import com.ats.platform.Instrument;
import com.ats.platform.JOrder;
import com.ats.platform.JOrder.OrderState;
import com.ats.platform.Strategy;

/**
 * Manages orders for the strategies.
 * 
 * Designed to be subclassed to handle different modes or backends.
 * To support orders like flatten() (proposed), OrderManager
 * needs to have access to position information as well.
 * 
 * @author Adrian
 *
 */
public abstract class OrderManager {
	private static final Logger logger = Logger.getLogger(OrderManager.class);

	
	private Map<Strategy, StrategyDetails> stratDetails = new HashMap<Strategy, StrategyDetails>();
	
	private Map<Strategy, List<ExecutionListener>> strategyExecutionListeners = new HashMap<Strategy, List<ExecutionListener>>();
	private List<ExecutionListener> executionListeners = new ArrayList<ExecutionListener>();
	
	// TODO: should make this immediately persistent so we can recover from crashes?
	// JOrder has an 'instrument', but this allows much faster access 
	private Map<Instrument, List<JOrder>> outstandingOrders = new HashMap<Instrument, List<JOrder>>();
	
	// redundant data, for ease of access
	private Map<Strategy, List<JOrder>> outstandingStrategyOrders = new HashMap<Strategy, List<JOrder>>();
	
	// could make a map, but this is simple
	private List<JOrder> completedOrders = new ArrayList<JOrder>();
	
	/**
	 * should only be run by the OrderManagerFactory
	 *
	 */
	protected OrderManager() {
		addExecutionListener(PositionManager.getInstance());
	}
	
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
			.append("outstandingOrders", outstandingOrders)
			.append("outstandingStrategyOrders", outstandingStrategyOrders)
			.append("completedOrders", completedOrders)
			.toString();
	}
	
	public void reset() {
		stratDetails = new HashMap<Strategy, StrategyDetails>();
		
		strategyExecutionListeners = new HashMap<Strategy, List<ExecutionListener>>();
		executionListeners = new ArrayList<ExecutionListener>();
		outstandingOrders = new HashMap<Instrument, List<JOrder>>();
		outstandingStrategyOrders = new HashMap<Strategy, List<JOrder>>();
		completedOrders = new ArrayList<JOrder>();
		addExecutionListener(PositionManager.getInstance());
	}
	
	public synchronized List<JOrder> getOutstandingOrders(Instrument instrument) {
		List<JOrder> oo = outstandingOrders.get(instrument);
		if( oo == null ) {
			oo = new ArrayList<JOrder>();
			outstandingOrders.put(instrument, oo);
		}
		// return a copy
		List<JOrder> ret = new ArrayList<JOrder>(oo.size());
		ret.addAll(oo);
		return ret;
	}
	
	public synchronized List<JOrder> getOutstandingOrders(Strategy strategy) {
		List<JOrder> oo = outstandingStrategyOrders.get(strategy);
		if( oo == null ) {
			oo = new ArrayList<JOrder>();
			outstandingStrategyOrders.put(strategy, oo);
		}
		
		// return a copy
		List<JOrder> ret = new ArrayList<JOrder>(oo.size());
		ret.addAll(oo);
		return ret;
	}
	
	
	protected synchronized void completeOrder(JOrder order) {
		// TODO: set the order state?
		completedOrders.add(order);
		outstandingOrders.get(order.getInstrument()).remove(order);
		outstandingStrategyOrders.get(order.getStrategy()).remove(order);
		
		// issue stop order
		// TODO: handle partial fills. Only significant for trailing stop orders
		for( JOrder trigOrder : order.getTriggerOrders() ) {
			placeOrder(order.getStrategy(), trigOrder);
		}
	}
	
	public synchronized final void addExecutionListener(ExecutionListener listener) {
		executionListeners.add(listener);
	}
	
	public synchronized final void removeExecutionListener(ExecutionListener listener) {
		executionListeners.remove(listener);
	}
	
	
	public synchronized final void addExecutionListener(Strategy strategy, ExecutionListener listener ) {
		List<ExecutionListener> lists = strategyExecutionListeners.get(strategy);
		if( lists == null ) {
			lists = new ArrayList<ExecutionListener>();
			strategyExecutionListeners.put(strategy, lists);
		}
		lists.add(listener);
	}
	
	public synchronized final void removeExecutionListener(Strategy strategy, ExecutionListener listener ) {
		List<ExecutionListener> lists = strategyExecutionListeners.get(strategy);
		if( lists != null ) {
			lists.remove(listener);
		}
	}
	
	public synchronized final void fireExecution(JOrder order, JExecution execution) {
		// need to send messages to the PositionManager first
		for( ExecutionListener listen : executionListeners ) {
			listen.execution(order, execution);
		}
		
		List<ExecutionListener> listeners = strategyExecutionListeners.get(order.getStrategy());
		if( listeners != null ) {
			for( ExecutionListener listen : listeners ) {
				listen.execution(order, execution);
			}
		}
	}

	protected synchronized final StrategyDetails getStrategyDetails(Strategy strategy) {
		StrategyDetails det = stratDetails.get(strategy);
		if( det == null ) {
			det = new StrategyDetails();
			stratDetails.put(strategy, det);
		}
		return det;
	}
	
    public synchronized void placeOrder(Strategy strategy, JOrder order) {
    	if( order.getQuantity() <= 0 ) {
    		// reject
    		logger.error("Invalid quantity size: " + order.getQuantity());
    		return;
    	}
    	StrategyDetails det = getStrategyDetails(strategy);
    	det.outstandingOrders.add(order);
    	
    	order.setState(OrderState.pendingSubmit);
    	
    	List<JOrder> oo = outstandingOrders.get(order.getInstrument());
    	if( oo == null ) {
    		oo = new ArrayList<JOrder>();
    		outstandingOrders.put(order.getInstrument(), oo);
    	}
    	oo.add(order);

    	oo = outstandingStrategyOrders.get(strategy);
    	if( oo == null ) {
    		oo = new ArrayList<JOrder>();
    		outstandingStrategyOrders.put(strategy, oo);
    	}
    	oo.add(order);
	}
    
    public void cancelOrder(Strategy strategy, JOrder order) {
	}
    
    protected synchronized void cancelledOrder(Strategy strategy, JOrder order) {
    	order.setCancelled();
    	outstandingOrders.get(order.getInstrument()).remove(order);
    	outstandingStrategyOrders.get(strategy).remove(order);
    }
}

class StratOrderPr {
	public JOrder order;
	public Strategy strategy;
}
