package com.ats.utils;

public class TradeStats {
	public double grossProfit=0, netProfit = 0, grossLoss=0, netLoss = 0, grossUnrealized=0;
	public int numTrades=0, numShares=0, numWinners=0, numLosers=0;
	public double largestWinner=0, largestLoser=0;
	public int numConsecWinners=0, numConsecLosers=0, maxConsecWinners=0, maxConsecLosers=0;
	public double equityHigh = 0, maxDrawdown = 0, maxPerTradeLFT=0;
	public int maxShares = 0;
	public double commissions = 0.0;
	public double profitFactor;

	public double getTotalGross() {
		return grossProfit + grossLoss;
	}
	public double getTotalNet() {
		return netProfit + netLoss;
	}
	public double getAvgWinner() {
		return grossProfit / numWinners;
	}
	public double getAvgLoser() {
		return grossLoss / numLosers;
	}
	public double getAvgTrade() {
		return getTotalNet()/numTrades;
	}
	
	public double getReturnOnAccount() {
		final double initialCapital =  Utils.getPreferenceStore().getDouble(Utils.INITIAL_CAPITAL_VALUE);
		return getTotalNet() / initialCapital;
	}
}
