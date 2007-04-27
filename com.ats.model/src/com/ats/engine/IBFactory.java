package com.ats.engine;

import java.util.Collection;
import java.util.Iterator;

import com.ats.platform.Instrument;

public class IBFactory {

	public static void runBacktest(StrategyDefinition definition) {
		IBOrderManager orderManager = (IBOrderManager)Factory.getInstance().getOrderManager();
		IBDataManager dataMgr = (IBDataManager)Factory.getInstance().getDataManager();
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

}
