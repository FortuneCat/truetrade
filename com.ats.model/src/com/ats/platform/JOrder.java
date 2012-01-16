package com.ats.platform;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.ib.client.Execution;
import com.ib.client.Order;

public class JOrder {
	public enum OrderState {
		building,
		pendingSubmit,
		submitted,
		rejected,
		partiallyFilled,
		filled,
		pendingCancel,
		cancelled 
	}
	
	public enum OrderTif {
		day("Day"),
		gtc("GTC"),
		ioc("IOC"),
		gtd("GTD");
		private String cmd;
		private OrderTif(String cmd) {
			this.cmd = cmd;
		}
		public String getIbCmd() {
			return cmd;
		}
	}
	
	private List<JOrder> triggerOrders = new ArrayList<JOrder>();
	
	private double avgPrice;
	private int filledSize = 0;
	
	private Instrument instrument;
	private Strategy strategy;
	
	private OrderState state;
	private double price;
	private double stopPrice;
	private int quantity;
	private OrderSide side;
	private String text;
	private OrderType type;
	private OrderTif tif = OrderTif.day;
	private boolean rthOnly = true;
	private String oneCancelsAllGroup;
	
	/** when transmitted, the IB order id */
	private int orderId;
	
	public JOrder(Strategy strat) {
		this.strategy = strat;
	}
	public String toString() {
		return new ToStringBuilder(this)
			.append("instrument", instrument)
			.append("state", state)
			.append("price", price)
			.append("stopPrice", stopPrice)
			.append("side", side)
			.append("type", type)
			.append("avgPrice", avgPrice)
			.append("filledSize", filledSize)
			.append("text", text)
			.toString();
	}
	
    public JOrder(Strategy strat, Instrument instrument, OrderSide side, OrderType type,
			int quantity, double price, double stopPrice, String text) {
		this.strategy = strat;

		this.instrument = instrument;
		this.quantity = quantity;
		this.side = side;
		this.type = type;
		this.price = price;
		this.stopPrice = stopPrice;
		this.text = text;
	}

	public Order buildIBOrder() {
        Order order = new Order();
        order.m_action = getSide().getIbCmd();
        order.m_totalQuantity = quantity;
        order.m_orderType = getType().getIbCmd();//"MKT";
        order.m_tif = tif.getIbCmd();
        if( getInstrument().isStock() ) {
        	order.m_rthOnly = rthOnly;
        }
        order.m_ocaGroup = oneCancelsAllGroup;
        order.m_lmtPrice = price;
        order.m_auxPrice = stopPrice;
        return order;
	}
	
	public void addExecution(Execution execution) {
		int newSize = execution.m_shares + filledSize;
		avgPrice = ((execution.m_price * execution.m_shares) + (avgPrice * filledSize))/newSize;
		filledSize += execution.m_shares;
		OrderState prevState = state;
		if( filledSize >= quantity ) {
			state = OrderState.filled;
		} else {
			state = OrderState.partiallyFilled;
		}
		if( prevState != state ) {
			strategy.onOrderStatusChanged(this);
		}
		strategy.onOrderFilled(this);
		if( state == OrderState.filled ) {
			strategy.onOrderDone(this);
		}
	}
	
	public void setCancelled() {
		state = OrderState.cancelled;
		strategy.onOrderCancelled(this);
	}
	public void setSubmitted() {
		state = OrderState.submitted;
		strategy.onOrderStatusChanged(this);
	}
	public void setPendingCancel() {
		state = OrderState.pendingCancel;
		strategy.onOrderStatusChanged(this);
	}

	public boolean isCancelled() {
		return state == OrderState.cancelled;
	}
	public boolean isDone() {
		return (state == OrderState.filled 
				|| state == OrderState.rejected 
				|| state == OrderState.cancelled);
	}
	public boolean isFilled() {
		return state == OrderState.filled;
	}
	public String getOneCancelsAllGroup() {
		return oneCancelsAllGroup;
	}
	public void setOneCancelsAllGroup(String oneCancelsAllGroup) {
		this.oneCancelsAllGroup = oneCancelsAllGroup;
	}
	public boolean isPartiallyFilled() {
		return state == OrderState.partiallyFilled;
	}
	public boolean isRejected() {
		return state == OrderState.rejected;
	}
	public double getAvgPrice() {
		return avgPrice;
	}
	public void setAvgPrice(double avgPrice) {
		this.avgPrice = avgPrice;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public OrderSide getSide() {
		return side;
	}
	public void setSide(OrderSide side) {
		this.side = side;
	}
	public double getStopPrice() {
		return stopPrice;
	}
	public void setStopPrice(double stopPrice) {
		this.stopPrice = stopPrice;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public OrderType getType() {
		return type;
	}
	public void setType(OrderType type) {
		this.type = type;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	protected OrderState getState() {
		return state;
	}

	/**
	 * not to be called by any client, only for use by the
	 * order management system.
	 * 
	 * @param state
	 */
	public void setState(OrderState state) {
		this.state = state;
	}

	public OrderTif getTif() {
		return tif;
	}

	public void setTif(OrderTif tif) {
		this.tif = tif;
	}

	public boolean isRthOnly() {
		return rthOnly;
	}

	public void setRthOnly(boolean rthOnly) {
		this.rthOnly = rthOnly;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

	public int getFilledSize() {
		return filledSize;
	}

	public void setFilledSize(int filledSize) {
		this.filledSize = filledSize;
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	public void addTriggerOrder(JOrder order) {
		triggerOrders.add(order);
	}
	public List<JOrder> getTriggerOrders() {
		return triggerOrders;
	}

}
