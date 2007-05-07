package com.ats.engine;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ats.platform.Bar;
import com.ats.platform.JOrder;
import com.ats.platform.OrderSide;
import com.ats.platform.Strategy;
import com.ats.platform.Trade;
import com.ats.platform.JOrder.OrderState;
import com.ats.utils.Utils;

public class BacktestOrderManager extends OrderManager implements TickListener {
	private static final Logger logger = Logger.getLogger(BacktestOrderManager.class);
	
	@Override
	public synchronized void cancelOrder(Strategy strategy, JOrder order) {
		super.cancelOrder(strategy, order);
		cancelledOrder(strategy, order);
	}

	@Override
	public void placeOrder(Strategy strategy, JOrder order) {
		super.placeOrder(strategy, order);
		
		
	}
	
	public synchronized void onTrade(Trade trade) {
		List<JOrder> completedOrders = new ArrayList<JOrder>();
		List<JOrder> cancelledOrders = new ArrayList<JOrder>();  // for OCA groups
		
		// copy all outstanding orders to prevent concurrent mod exception
		List<JOrder> tmp = getOutstandingOrders(trade.getInstrument());
		List<JOrder> outstandingOrders = new ArrayList<JOrder>(tmp.size());
		outstandingOrders.addAll(tmp);
		
		// see if we should fake a trade
		for(JOrder order : outstandingOrders) {
			if( order.isDone() ) {
				// how did we get here?
				completedOrders.add(order);
				continue;
			}
			if( completedOrders.contains(order) || cancelledOrders.contains(order)) {
				// most likely a OCA group caused a cancel, so move on to the next
				continue;
			}
			boolean placeTrade = false;
			switch( order.getType() ) {
			case market:
				placeTrade = true;
				break;
			case limit:
				placeTrade = ( order.getSide() == OrderSide.BUY && order.getPrice() >= trade.getPrice() );
				placeTrade |= ( order.getSide() == OrderSide.SELL && order.getPrice() <= trade.getPrice() );
				break;
			case stop:
			case stopLimit:
				placeTrade = ( order.getSide() == OrderSide.BUY && order.getStopPrice() <= trade.getPrice() );
				placeTrade |= ( order.getSide() == OrderSide.SELL && order.getStopPrice() >= trade.getPrice() );
				break;
			case trailingStop:
				placeTrade = ( order.getSide() == OrderSide.BUY && order.getPrice() <= trade.getPrice() );
				placeTrade |= ( order.getSide() == OrderSide.SELL && order.getPrice() >= trade.getPrice() );
				if( order.getSide() == OrderSide.BUY ) {
					order.setPrice(Math.min(trade.getPrice() + order.getStopPrice(), order.getPrice()));
				} else {
					// sell
					order.setPrice(Math.max(trade.getPrice() - order.getStopPrice(), order.getPrice()));
				}
				break;
			}
			if( placeTrade ) {
				order.setState(OrderState.filled);
				completedOrders.add(order);

				JExecution exec = new JExecution();
				exec.setOrder(order);
				exec.setDateTime(trade.getDateTime());
				exec.setInstrument(trade.getInstrument());
				exec.setPrice(trade.getPrice());
				exec.setQuantity(order.getQuantity());
				exec.setSide(order.getSide());
				order.setFilledSize(order.getQuantity());
				
				double price = trade.getPrice();
				// slippage
				int tickSlip = (int)Utils.getPreferenceStore().getDouble(Utils.SLIPPAGE_TICK_VALUE);
				if( tickSlip > 0 ) {
					double tickSize = trade.getInstrument().getTickSize();
					double slip = ((double)tickSlip * tickSize );
					price = order.getSide() == OrderSide.BUY ? price + slip : price - slip;
				}
				order.setAvgPrice(price);
				fireExecution(order, exec);
				
				// check for OCA groups
				if( order.getOneCancelsAllGroup() != null && order.getOneCancelsAllGroup().length() > 0 ) {
					for(JOrder o : outstandingOrders) {
						if( order == o ) {
							// '==' is intentional, it really is the same object
							continue;
						}
						if( order.getOneCancelsAllGroup().equals(o.getOneCancelsAllGroup()) && !o.isDone()) {
							cancelledOrders.add(o);
						}
					}
				}
			}
		}
		for(JOrder order : completedOrders) {
			completeOrder(order);
		}
		for(JOrder order : cancelledOrders) {
			cancelOrder(order.getStrategy(), order);
		}
	}

	public void onBar(Bar bar) {
		// do nothing, it's all done in onTrade
	}
	
	public void onBarOpen(Bar bar) {
		// do nothing
	}

}
