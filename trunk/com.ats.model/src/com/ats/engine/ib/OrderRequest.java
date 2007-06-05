package com.ats.engine.ib;

import com.ats.platform.JOrder;

public class OrderRequest extends IBRequest {

	private JOrder order;
	
	public OrderRequest(RequestListener listener, JOrder order) {
		super(listener);
		this.order = order;
	}

	@Override
	protected void doRequest(IBHelper helper) {
		order.setOrderId(getId());
    	IBHelper.getInstance().placeOrder(getId(), order.getInstrument().getContract(), order.buildIBOrder());
	}

	public JOrder getOrder() {
		return order;
	}

}
