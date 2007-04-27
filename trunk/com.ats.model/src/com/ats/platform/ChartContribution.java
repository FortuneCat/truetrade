package com.ats.platform;

import org.eclipse.swt.graphics.Color;

public class ChartContribution {
	private String paneId;
	private String label;
	private Color color;
	
	private double data[];
	
	public double[] getData() {
		return data;
	}
	public void setData(double[] data) {
		this.data = data;
	}
	public String getPaneId() {
		return paneId;
	}
	public void setPaneId(String paneId) {
		this.paneId = paneId;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}
}
