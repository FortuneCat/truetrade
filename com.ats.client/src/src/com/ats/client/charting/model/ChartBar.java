package com.ats.client.charting.model;

import org.eclipse.draw2d.geometry.Dimension;

import com.ats.platform.Bar;

public class ChartBar extends BaseChartElement {
	
	private Bar bar;
	
	public ChartBar(Bar bar) {
		this.bar = bar;
		setSize(new Dimension(30, 30));
	}

}
