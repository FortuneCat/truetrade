package com.ats.engine.backtesting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.ats.engine.Factory;
import com.ats.engine.PositionManager;
import com.ats.engine.StrategyDefinition;
import com.ats.platform.Instrument;

/**
 * Sort of a utility class to help start and run backtesting.
 * 
 * It feels like it's a collection of functions without a home and I
 * hope to eventually move these to someplace more stable, once I work
 * out what all of the requirements are.
 * 
 * @author Adrian
 * @author Krzysztof Kazmierczyk - fix Issue 54 (onOrderFilled is not executed when running optimization)
 *
 */
public class BacktestFactory {
	
	public BacktestFactory() {
		
	}
	
	public void runBacktest(StrategyDefinition definition) {
		runBacktest(definition, null);
	}
	
	public void runBacktest(StrategyDefinition definition, final BacktestListener listener) {
		final BacktestOrderManager orderManager = (BacktestOrderManager)Factory.getInstance().getOrderManager();
		orderManager.reset();
		PositionManager.getInstance().reset();
		Collection<Instrument> instrs = definition.getInstruments();
		final List<BacktestDataManager> mgrs = new ArrayList<BacktestDataManager>();
		Iterator<Instrument> it = instrs.iterator();
		while( it.hasNext() ) {
			Instrument instr = it.next();
			// need to add the backtest order manager as a tick listener, but
			// where best to do this?
			BacktestDataManager dataMgr = new BacktestDataManager();
			mgrs.add(dataMgr);
			dataMgr.setSpan(definition.getBacktestDataTimeSpan());
			dataMgr.addTickListener(instr, orderManager);
			BacktestStrategyEngine bse = new BacktestStrategyEngine(definition, instr, orderManager, dataMgr);
			Thread t = new Thread(bse);
			t.start();
		}
		
		if( listener != null ) {
			new Thread(new Runnable() {
				public void run() {
					while( true ) {
						try {
							Thread.sleep(300);
						} catch( Exception e) {}
						boolean isDone = true;
						for(BacktestDataManager mgr : mgrs ) {
							if( ! mgr.isComplete() ) {
								isDone = false;
								break;
							}
						}
						if( isDone ) {
							orderManager.reset();
							listener.testComplete();
							
							break;
						}
					}
				}
			}).start();
		}
		
		
		// TODO: if the order manager is persistent, we need to make sure that we call
		// removeListener methods so that the other managers get garbage collected
		// 
	}

}
