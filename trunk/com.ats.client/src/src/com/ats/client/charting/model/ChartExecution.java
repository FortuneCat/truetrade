package com.ats.client.charting.model;

import org.eclipse.draw2d.geometry.Dimension;

import com.ats.engine.JExecution;

public class ChartExecution extends BaseChartElement {
	
	private JExecution execution;
	
	public ChartExecution(JExecution execution) {
		setSize(new Dimension(5, 5));
	}
}
