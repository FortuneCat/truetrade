package com.ats.engine;

import java.util.ArrayList;
import java.util.List;

import com.ats.platform.JOrder;
import com.ats.platform.Position;

/**
 * A repository for the details of a currently executing strategy.
 * 
 * It isn't really a stand-alone class with any responsibilities, rather
 * a struct to make it convenient for organizing data.
 * 
 * Fields are not threadsafe as a strategy is expected to be running in
 * only one thread, and only one strategy will be accessing this class at any
 * given time.
 * 
 * @author Adrian
 *
 */
public class StrategyDetails {
	
	public List<JOrder> outstandingOrders = new ArrayList<JOrder>();
	
	public List<JExecution> executions = new ArrayList<JExecution>();
	
	public Position position;

}
