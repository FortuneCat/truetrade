package com.ats.client.charting.model;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

/**
 * base class for all objects to be rendered on a chart
 * 
 * @author Adrian
 *
 */
public class BaseChartElement {
	private Point location;
	private Dimension size = new Dimension(-1, -1);
	
	
	public Point getLocation() {
		return location;
	}
	public void setLocation(Point location) {
		this.location = location;
	}
	public Dimension getSize() {
		return size;
	}
	public void setSize(Dimension size) {
		this.size = size;
	}
}
