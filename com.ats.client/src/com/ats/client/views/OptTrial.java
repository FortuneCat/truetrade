package com.ats.client.views;

import java.util.HashMap;
import java.util.Map;

import com.ats.utils.TradeStats;

/**
 * the results of an optimization trial run
 * 
 * @author Adrian
 *
 */
public class OptTrial {
	public Map<String, Number> paramVals = new HashMap<String, Number>();
	public TradeStats stats;
}
