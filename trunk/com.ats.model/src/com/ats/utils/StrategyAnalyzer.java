package com.ats.utils;

import static com.ats.utils.Utils.currencyForm;
import static com.ats.utils.Utils.doubleDecForm;

import java.util.Collection;

import com.ats.engine.TradeSummary;

public class StrategyAnalyzer {

	public static TradeStats calculateTradeStats(Collection <TradeSummary> trades) {
		if( trades == null || trades.size() <= 0) {
			return null;
		}
		TradeStats stats = new TradeStats();
		
		for(TradeSummary ts : trades) {
			stats.maxShares = Math.max(ts.getTotalBuyQty(), stats.maxShares);
			stats.maxShares = Math.max(ts.getTotalSellQty(), stats.maxShares);
			stats.maxDrawdown = Math.max(stats.maxDrawdown, ts.getMaxDrawdown());
			stats.numTrades++;
			stats.numShares += (ts.getTotalBuyQty() + ts.getTotalSellQty());
			stats.grossUnrealized += ts.getUnrealizedProfit();
			if( ts.getRealizedPnL() > 0 ) {
				stats.grossProfit += ts.getRealizedPnL();
				stats.netProfit += ts.getRealizedPnL();
				stats.numWinners++;
				stats.largestWinner = Math.max(ts.getRealizedNetPnL(), stats.largestWinner);
				stats.numConsecLosers = 0;
				stats.numConsecWinners++;
				stats.maxConsecWinners = Math.max(stats.maxConsecWinners, stats.numConsecWinners);
			} else {
				stats.grossLoss += ts.getRealizedPnL();
				stats.netLoss += ts.getRealizedNetPnL();
				stats.numLosers++;
				stats.largestLoser = Math.min(ts.getRealizedNetPnL(), stats.largestLoser);
				stats.numConsecWinners = 0;
				stats.numConsecLosers++;
				stats.maxConsecLosers = Math.max(stats.maxConsecLosers, stats.numConsecLosers);
			}
		}
		// calc comission
//		double comission = 0.0;
//		if( Utils.getPreferenceStore().getBoolean(Utils.COMMISSION_SHARE) ) {
//			comission = numShares * Utils.getPreferenceStore().getDouble(Utils.COMMISSION_SHARE_VALUE);
//		} else if( Utils.getPreferenceStore().getBoolean(Utils.COMMISSION_ORDER) ) {
//			comission = numTrades * Utils.getPreferenceStore().getDouble(Utils.COMMISSION_ORDER_VALUE);
//		}
		
		return stats;
		
	}

}
