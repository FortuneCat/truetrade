package com.ats.client.charting.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.XYAnchor;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.Path;
import org.eclipse.swt.graphics.Color;

import com.ats.engine.JExecution;
import com.ats.platform.OrderSide;

public class ExecutionFigure extends Figure {

	private JExecution execution;
	
	private boolean isSelected = false;
	
	private static Color COLOR_BUY = ColorConstants.blue;
	private static Color COLOR_SELL = ColorConstants.cyan;
	private static Color COLOR_BORDER = ColorConstants.black;

	
	public ExecutionFigure(JExecution execution) {
		this.execution = execution;
		
		setOpaque(false);
	}
	
	public ConnectionAnchor getConnectionAnchor() {
		Point p = new Point(0,0);
		translateToAbsolute(p);
		return new XYAnchor(p);
	}
	
	public JExecution getExecution() {
		return execution;
	}
	
	public void setSelected(boolean sel) {
		isSelected = sel;
	}
	
	@Override
	protected void paintClientArea(Graphics graphics) {
		//Rectangle rect = (Rectangle)getParent().getLayoutManager().getConstraint(this);
		Rectangle rect = getBounds();
		graphics.translate(rect.x, rect.y);

		double highestHigh = ((BarSeriesFigure)getParent()).getHighestHigh();
		double lowestLow = ((BarSeriesFigure)getParent()).getLowestLow();
		double dollarRange = highestHigh - lowestLow;
		
		// the amount of white space at the top
		double topRatio = (highestHigh - execution.getPrice())/dollarRange;
		
		int drawHigh = (int)((topRatio)*rect.height);

		Dimension size = new Dimension(rect.width, rect.height);

		graphics.setForegroundColor(COLOR_BORDER);
		if( OrderSide.BUY.equals(execution.getSide()) ) {
			graphics.setBackgroundColor(COLOR_BUY);
			if( isSelected ) {
				graphics.setBackgroundColor(ColorConstants.yellow);
			}
			PointList points = new PointList();
			points.addPoint(size.width/2, (int)drawHigh);
			points.addPoint(size.width/4, (int)drawHigh+(size.width/2));
			points.addPoint(3*size.width/4, (int)drawHigh+(size.width/2));
			graphics.fillPolygon(points);
			graphics.drawPolygon(points);
		} else {
			graphics.setBackgroundColor(COLOR_SELL);
			if( isSelected ) {
				graphics.setBackgroundColor(ColorConstants.yellow);
			}
			PointList points = new PointList();
			points.addPoint(size.width/2, (int)drawHigh);
			points.addPoint(size.width/4, (int)drawHigh-(size.width/2));
			points.addPoint(3*size.width/4, (int)drawHigh-(size.width/2));
			graphics.fillPolygon(points);
			graphics.drawPolygon(points);
		}
	}

}
