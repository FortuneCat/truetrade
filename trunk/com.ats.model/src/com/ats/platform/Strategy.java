package com.ats.platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;

import com.ats.engine.PositionManager;
import com.ats.engine.StrategyEngine;
import com.ats.engine.TickListener;
import com.ats.engine.TradeListener;
import com.ats.platform.Position.PositionSide;


/**
 * Base class for all classes that implement trading strategies.
 */
public abstract class Strategy implements TickListener, TradeListener {
	private static final Logger logger = Logger.getLogger(Strategy.class);
	
	/** default volume to buy/sell */
    protected int defaultSize;

    private boolean onlyRTHPriceBars;
    private Instrument instrument;
//    private final List<IndicatorHistory> indicators;
    private StrategyEngine strategyEngine;

    private Map<String, Number> params = new HashMap<String, Number>();

    public void updateIndicators(boolean isOptimized) {
    }

    public Strategy() {
//        tradingSchedule = new TradingSchedule(this);
//        indicators = new ArrayList<IndicatorHistory> ();
    }
    
    public String toString() {
    	return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
    		.append("Instrument", instrument)
    		.append("strategyEngine", strategyEngine)
    		.append("params", params).toString();
//    	return "Strategy: " + getClass().getSimpleName() + ", instrument=" + instrument;
    }
    
    /**
     * Subclasses can add parameters which may be set at build time.  This is most
     * useful for backtesting, when many different parameters need to be tried.
     * 
     * @param paramName
     * @param defValue
     */
    public final void addParam(String paramName, Number defValue) {
    	params.put(paramName, defValue);
    }
    
    /**
     * Subclasses may override to provide a custom instrument source.  By default,
     * runtime instruments will be drawn from the database definition, but if there
     * is an SIS, then the DB instruments will be ignored and the SIS will provide all
     * instruments instead.  Backtesting will still use the DB Instruments.
     * 
     * @return
     */
    public StrategyInstrumentSource getInstrumentSource() {
    	return null;
    }
    
    public final Number getParam(String paramName) {
    	return params.get(paramName);
    }
    public final Set<String> getParamNames() {
    	return params.keySet();
    }

    /**
     * Allow subclasses to contribute to the chart.  Each contribution must end at
     * the end of the series and must not skip any bars in the series.  It does not
     * need to start at the same place as the series.
     * 
     * @param series
     * @return
     */
    public List<ChartContribution> getContributions(BarSeries series) {
    	return Collections.emptyList();
    }


    /**
     * sets the trading interval in local time, using "HH:mm" format.  Must
     * be called from init() and not from the constructor.
     */
    public final void setTradingInterval( String startTime, String endTime ) {
    	strategyEngine.setTradingInterval(startTime, endTime);
    }

    /**
     * Called after the strategy has had all of its parameters set so that it may
     * do any initialization.  All code to initialize the strategy should be place
     * here and not in the constructor.
     * 
     * @throws IOException
     */
    public void init()  {
    }
    
    /**
     * Called after all initialization is complete and any current positions have been located.
     * This is intended to allow long-running strategies to perform any state initialization
     * after the ATS has been shutdown.
     *
     */
    public void onStartup() {
    	
    }
    


    public final boolean getOnlyRTHPriceBars() {
        return onlyRTHPriceBars;
    }

    /**
     * returns a price bar series with specified size
     * @return
     */
    public final BarSeries getSeries(TimeSpan span){
    	return strategyEngine.getSeries(span); 
    }
    
    /**
     * onBar() events will now be triggered with the specified size bars
     * @param barSizeInSecs
     */
    public final void requestTimeSeries(TimeSpan span) {
    	// as a side-effect, getSeries will update the given series
    	strategyEngine.requestSeries(span);
    }

    /**
     * Subclasses override to get events whenever a trade takes place
     */
    public void onTrade(Trade trade) {
    }

    /**
     * called when a new bar is created
     * @param bar
     */
    public void onBarOpen(Bar bar) {
    }
    
    /**
     * called when a bar has completed
     */
    public void onBar(Bar bar) {
    }
    
    public void onNewOrder(JOrder order) { }
    
    public void onOrderCancelled(JOrder order) {}
    
    public void onOrderDone(JOrder order) {}
    
    public void onOrderFilled(JOrder order){}
    
    public void onOrderRejected(JOrder order){}
    
    public void onOrderStatusChanged(JOrder order){ }
    
    public void onPositionChanged() { }
    
    public void onPositionClosed() { }
    
    public void onPositionOpened() { }
    
    public void onPositionValueChanged() { }
    
    public void onStrategyStart() { }
    
    public void onStrategyStop() { }
    
    
    public final boolean hasPosition() {
    	Position pos = getPosition();
    	return ! PositionSide.FLAT.equals(pos.getSide());
    }
    public final boolean isLong() {
    	Position pos = getPosition();
    	return PositionSide.LONG.equals(pos.getSide());
    }
    public final boolean isShort() {
    	Position pos = getPosition();
    	return PositionSide.SHORT.equals(pos.getSide());
    }
    
    /**
     * 
     * @return
     */
    public final Position getPosition() {
    	return PositionManager.getInstance().getPosition(this);
    }
    

    /*  helper methods to manage orders manually */
    
    public void goFlat(String message) {
    	cancelAllOrders();
    	Position pos = getPosition();
    	if( PositionSide.LONG.equals(pos.getSide() ) ) {
    		sell(pos.getQuantity(), message);
    	} else if( PositionSide.SHORT.equals(pos.getSide() ) ) {
    		buy(pos.getQuantity(), message);
    	}
    }
    
    public void goFlat() {
    	goFlat("");
    }
    
    public void goLong(int minSize) {
    	for(JOrder order : getOutstandingOrders()) {
    		if(OrderSide.BUY.equals(order.getSide())) {
    			// already a buy order present, let it complete
    			return;
    		}
    	}
    	Position pos = getPosition();
    	if( PositionSide.SHORT.equals(pos.getSide())) {
    		buy(pos.getQuantity() + minSize);
    	} else {
    		int delta = minSize - pos.getQuantity();
    		if( delta > 0 ) {
    			buy(delta);
    		}
    	}
    }
    public void goShort(int minSize) {
    	for(JOrder order : getOutstandingOrders()) {
    		if(OrderSide.SELL.equals(order.getSide())) {
    			// already a buy order present, let it complete
    			return;
    		}
    	}
    	Position pos = getPosition();
    	if( PositionSide.LONG.equals(pos.getSide())) {
    		sell(pos.getQuantity() + minSize);
    	} else {
    		int delta = minSize - pos.getQuantity();
    		if( delta > 0 ) {
    			sell(delta);
    		}
    	}
    }
    
    public void cancelBuyOrders() {
    	cancelOrders(OrderSide.BUY);
    }

    public void cancelSellOrders() {
    	cancelOrders(OrderSide.SELL);
    }

	private void cancelOrders(OrderSide orderSide) {
		List<JOrder> prevOutstanding = new ArrayList<JOrder>();
		prevOutstanding.addAll(getOutstandingOrders());
		for(JOrder order : prevOutstanding ) {
			if( orderSide.equals(order.getSide()) ) {
    			strategyEngine.cancelOrder(order);
    		}
    	}
	}
    
	/**
	 * Cancels all orders in pending state
	 */
    public void cancelAllOrders() {
//    	logger.debug("cancelAllOrders for strategy[" + this + "], outstandingOrders=" + getOutstandingOrders());
    	for(JOrder order : getOutstandingOrders() ) {
    		strategyEngine.cancelOrder(order);
//    		logger.debug("After cancelling order[" + order + "], strategyEngine=" + strategyEngine);
    	}
    }
    
    public void cancelOrder(JOrder order) {
    	strategyEngine.cancelOrder(order);
    }
	
	public void sendOrder(JOrder order) {
		strategyEngine.placeOrder(order);
	}
	
	public List<JOrder> getOutstandingOrders() {
		return strategyEngine.getOutstandingOrders();
	}
    
    /**
     * issue a market order with the default number of shares
     */
    public JOrder buyOrder() {
    	return buyOrder(defaultSize);
    }
	public void buy() {
		logger.debug("buy " + defaultSize);
		sendOrder(buyOrder());
	}
    
    /**
     * issue a market order with specified number of shares
     */
    public JOrder buyOrder(int quantity, String message) {
    	return new JOrder(this, getInstrument(), OrderSide.BUY, OrderType.market, quantity, 0.0, 0.0, message);
    }
    public JOrder buyOrder(int quantity) {
    	return buyOrder(quantity, "");
    }
    public void buy(int quantity) {
    	sendOrder(buyOrder(quantity));
    }
    public void buy(int quantity, String message) {
    	sendOrder(buyOrder(quantity, message));
    }
    /**
     * issue a market order with the default number of shares
     */
    public JOrder sellOrder() {
    	return sellOrder(defaultSize);
    }
    public void sell() {
		logger.debug("sell " + defaultSize);
    	sendOrder(sellOrder());
    }
    /**
     * issue a market order with specified number of shares
     */
    public JOrder sellOrder(int quantity, String message) {
    	return new JOrder(this, getInstrument(), OrderSide.SELL, OrderType.market, quantity, 0.0, 0.0, message);
    }
    public JOrder sellOrder(int quantity) {
    	return sellOrder(quantity, "");
    }
    public void sell(int quantity) {
    	sendOrder(sellOrder(quantity));
    }
    public void sell(int quantity, String message) {
    	sendOrder(sellOrder(quantity, message));
    }
    
    public JOrder buyLimitOrder(int quantity, double price ) {
    	return buyLimitOrder(quantity, price, "");
    }
    public void buyLimit(int quantity, double price) {
    	sendOrder(buyLimitOrder(quantity, price));
    }
    public JOrder buyLimitOrder(int quantity, double price, String text ) {
    	return limitOrder(OrderSide.BUY, quantity, price, text);
    }
    public JOrder sellLimitOrder(int quantity, double price ) {
    	return sellLimitOrder(quantity, price, "");
    }
    public void sellLimit(int quantity, double price) {
    	sendOrder(sellLimitOrder(quantity, price));
    }
    public JOrder sellLimitOrder(int quantity, double price, String text ) {
    	return limitOrder(OrderSide.SELL, quantity, price, text);
    }
    private JOrder limitOrder(OrderSide side, int quantity, double price, String text) {
    	return new JOrder(this, getInstrument(), side, OrderType.limit, quantity, price, 0.0, text);
    }

    public JOrder buyStopOrder(int quantity, double stopPrice) {
    	return buyStopOrder(quantity, stopPrice, "");
    }
    public void buyStop(int quantity, double stopPrice) {
    	sendOrder(buyStopOrder(quantity, stopPrice));
    }
    public JOrder buyStopOrder(int quantity, double stopPrice, String text) {
    	return stopOrder(OrderSide.BUY, quantity, stopPrice, text);
    }
    public JOrder sellStopOrder(int quantity, double stopPrice) {
    	return sellStopOrder(quantity, stopPrice, "");
    }
    public void sellStop(int quantity, double stopPrice) {
    	sendOrder(sellStopOrder(quantity, stopPrice));
    }
    public JOrder sellStopOrder(int quantity, double stopPrice, String text) {
    	return stopOrder(OrderSide.SELL, quantity, stopPrice, text);
    }
    private JOrder stopOrder(OrderSide side, int quantity, double stopPrice, String text ) {
    	return new JOrder(this, getInstrument(), side, OrderType.stop, quantity, 0.0, stopPrice, text);
    }
    
    public JOrder buyTrailingStopOrder(int quantity, double stopPrice, double trailAmt) {
    	return buyTrailingStopOrder(quantity, stopPrice, trailAmt, "");
    }
    public void buyTrailingStop(int quantity, double stopPrice, double trailAmt) {
    	sendOrder(buyTrailingStopOrder(quantity, stopPrice, trailAmt));
    }
    public JOrder buyTrailingStopOrder(int quantity, double stopPrice, double trailAmt, String text) {
    	return trailingStopOrder(OrderSide.BUY, quantity, stopPrice, trailAmt, text);
    }
    public JOrder sellTrailingStopOrder(int quantity, double stopPrice, double trailAmt) {
    	return sellTrailingStopOrder(quantity, stopPrice, trailAmt, "");
    }
    public void sellTrailingStop(int quantity, double stopPrice, double trailAmt) {
    	sendOrder(sellTrailingStopOrder(quantity, stopPrice, trailAmt));
    }
    public JOrder sellTrailingStopOrder(int quantity, double stopPrice, double trailAmt, String text) {
    	return trailingStopOrder(OrderSide.SELL, quantity, stopPrice, trailAmt, text);
    }
    private JOrder trailingStopOrder(OrderSide side, int quantity, double stopPrice, double trailAmt, String text ) {
    	return new JOrder(this, getInstrument(), side, OrderType.trailingStop, quantity, stopPrice, trailAmt, text);
    }
    
    public JOrder buyStopLimitOrder(int quantity, double price, double stopPrice) {
    	return buyStopLimitOrder(quantity, price, stopPrice, "");
    }
    public void buyStopLimit(int quantity, double price, double stopPrice) {
    	sendOrder(buyStopLimitOrder(quantity, price, stopPrice));
    }
    public JOrder buyStopLimitOrder(int quantity, double price, double stopPrice, String text) {
    	return stopLimitOrder(OrderSide.BUY, quantity, price, stopPrice, text);
    }
    public JOrder sellStopLimitOrder(int quantity, double price, double stopPrice) {
    	return buyStopLimitOrder(quantity, price, stopPrice, "");
    }
    public void sellStopLimit(int quantity, double price, double stopPrice) {
    	sendOrder(sellStopLimitOrder(quantity, price, stopPrice));
    }
    public JOrder sellStopLimitOrder(int quantity, double price, double stopPrice, String text) {
    	return stopLimitOrder(OrderSide.SELL, quantity, price, stopPrice, text);
    }
    private JOrder stopLimitOrder(OrderSide side, int quantity, double price, double stopPrice, String text ) {
    	return new JOrder(this, getInstrument(), side, OrderType.stop, quantity, price, stopPrice, text);
    }


	public final void setStrategyEngine(StrategyEngine strategyEngine) {
		this.strategyEngine = strategyEngine;
	}

	public final Instrument getInstrument() {
		return instrument;
	}
	
	public final void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

}
