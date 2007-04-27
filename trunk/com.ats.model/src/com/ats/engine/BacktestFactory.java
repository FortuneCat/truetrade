package com.ats.engine;

import java.util.Collection;
import java.util.Iterator;

import com.ats.platform.Instrument;

/**
 * Sort of a utility class to help start and run backtesting.
 * 
 * It feels like it's a collection of functions without a home and I
 * hope to eventually move these to someplace more stable, once I work
 * out what all of the requirements are.
 * 
 * @author Adrian
 *
 */
public class BacktestFactory {
	
	public static void runBacktest(StrategyDefinition definition) {
		BacktestOrderManager orderManager = (BacktestOrderManager)Factory.getInstance().getOrderManager();
		orderManager.reset();
		BacktestDataManager dataMgr = new BacktestDataManager();
		dataMgr.setSpan(definition.getBacktestDataTimeSpan());
		PositionManager.getInstance().reset();
		Collection<Instrument> instrs = definition.getInstruments();
		Iterator<Instrument> it = instrs.iterator();
		while( it.hasNext() ) {
			Instrument instr = it.next();
			// need to add the backtest order manager as a tick listener, but
			// where best to do this?
			dataMgr.addTickListener(instr, orderManager);
			BacktestStrategyEngine bse = new BacktestStrategyEngine(definition, instr, orderManager, dataMgr);
			Thread t = new Thread(bse);
			t.start();
		}
		
		// TODO: if the order manager is persistent, we need to make sure that we call
		// removeListener methods so that the other managers get garbage collected
		// 
	}

}
