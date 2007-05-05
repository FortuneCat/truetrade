package com.ats.engine;

import com.ats.platform.JOrder;

public interface ExecutionListener {
	
	public void execution(JOrder order, JExecution execution);
}
