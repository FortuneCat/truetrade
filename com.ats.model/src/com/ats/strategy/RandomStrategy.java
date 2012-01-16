package com.ats.strategy;

import java.util.Random;

import org.apache.log4j.Logger;

import com.ats.platform.Bar;
import com.ats.platform.Position.PositionSide;
import com.ats.platform.Strategy;
import com.ats.platform.TimeSpan;
import com.ats.platform.Trade;

/**
 * Just for testing, takes entries & exits on a random basis.
 * 
 * @author Adrian
 *
 */
public class RandomStrategy extends Strategy {
	private static final Logger logger = Logger.getLogger(RandomStrategy.class);

	static final Random rand = new Random(); 
	private GapInstrumentSource gis;

	public RandomStrategy() {
		super();
		gis = new GapInstrumentSource();
	}
	
//	@Override
//    public StrategyInstrumentSource getInstrumentSource() {
//    	return gis;
//    }
	
	@Override
	public void init() {
		requestTimeSeries(TimeSpan.min1);
		
		// TODO: allow default size calculation to come from the strategy
		defaultSize = 100;
	}
	
	@Override
	public void onTrade(Trade trade) {
		logger.debug("Random trade: " + trade);
	}
	
	@Override
	public void onBar(Bar bar) {
		if( getOutstandingOrders().size() > 0 ) {
			// TODO: why do we need to do this check?  Shouldn't market orders
			// execute immediately on simulator?  Esp. if I'm using onBar()?
			
			// haven't been fully filled yet, so return
			return;
		}
		//if( rand.nextBoolean() ) {
		if( true ) {
			// trade!
			if( !hasPosition() ) {
				// start long, why the hell not?
				buy();
			} else {
				if(getPosition().getSide() == PositionSide.LONG ) {
					// flatten then flip
					sell();
					sell();
				} else {
					// short, so reverse
					buy();
					buy();
				}
			}
		}
	}
	
	
}
