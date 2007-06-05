package com.ats.engine;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.ats.engine.Factory.RuntimeMode;
import com.ats.engine.ib.IBDataManager;
import com.ats.engine.ib.IBOrderManager;
import com.ats.engine.ib.IBStrategyEngine;
import com.ats.platform.Instrument;
import com.ats.platform.StrategyInstrumentSource;

public class IBFactory {
	private static final Logger logger = Logger.getLogger(IBFactory.class);

	public static void runIB(StrategyDefinition definition) {
		Factory.getInstance().setMode(RuntimeMode.live);
		IBOrderManager orderManager = (IBOrderManager)Factory.getInstance().getOrderManager();
		IBDataManager dataMgr = (IBDataManager)Factory.getInstance().getDataManager();
		PositionManager.getInstance().reset();
		if( definition.hasStrategyInstrumentSource() ) {
			StrategyInstrumentSource sis = definition.getStrategyInstrumentSource();
			sis.setDataManager(dataMgr);
			sis.setDefinition(definition);
			sis.setOrderManager(orderManager);
			new Thread(sis).start();
		} else {
			Collection<Instrument> instrs = definition.getInstruments();
			Iterator<Instrument> it = instrs.iterator();
			while( it.hasNext() ) {
				Instrument instr = it.next();
				try {
					IBStrategyEngine bse = new IBStrategyEngine(definition, instr, orderManager, dataMgr);
		//			dataMgr.addTickListener(instr, orderManager);
					Thread t = new Thread(bse);
					t.start();
				} catch( Exception e) {
					logger.error("Could not start strategy [" + definition + "] for [" + instr + "]: " + e, e);
				}
			}
		}
	}

}
