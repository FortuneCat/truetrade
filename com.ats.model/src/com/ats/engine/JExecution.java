package com.ats.engine;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;

import com.ats.platform.Instrument;
import com.ats.platform.JOrder;
import com.ats.platform.OrderSide;
import com.ib.client.Execution;

public class JExecution {
	private static final Logger logger = Logger.getLogger(JExecution.class);
	
	private Instrument instrument;
	private int quantity;
	private double price;
	private OrderSide side;
	private Date dateTime;
	private JOrder order;
	
	private static final DateFormat execTimeFormat = new SimpleDateFormat("yyyyMMdd  kk:mm:ss");
	
	public JExecution() {
		
	}
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append("instrument",instrument)
			.append("quantity",quantity)
			.append("price", price)
			.append("side", side)
			.toString();
	}
	
	@Override
	public Object clone() {
		JExecution ret = new JExecution();
		ret.setDateTime(getDateTime());
		ret.setInstrument(getInstrument());
		ret.setOrder(getOrder());
		ret.setPrice(getPrice());
		ret.setSide(getSide());
		return ret;
	}
	
	public boolean equals(Object o) {
		if( o == null || !(o instanceof JExecution)) {
			return false;
		}
		JExecution that = (JExecution)o;
		return this.order.getOrderId() == that.getOrder().getOrderId()
			&& this.dateTime.equals(that.dateTime)
			&& this.instrument.equals(that.instrument)
			&& this.price == that.price
			&& this.quantity == that.quantity;
	}
	
	
	/** copy constructor */
	public JExecution(Execution e) {
		price = e.m_price;
		quantity = e.m_shares;
		try {
			dateTime = execTimeFormat.parse(e.m_time);
		} catch (ParseException ex) {
			logger.error("Could not parse execution time[" + e.m_time + "]", ex);
		}
		if( "BOT".equals(e.m_side) ) {
			side = OrderSide.BUY;
		} else if( "SLD".equals(e.m_side)) {
			side = OrderSide.SELL;
		}
	}
	
	public Instrument getInstrument() {
		return instrument;
	}
	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
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

	public Date getDateTime() {
		return dateTime;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	public JOrder getOrder() {
		return order;
	}

	public void setOrder(JOrder order) {
		this.order = order;
	}

}
