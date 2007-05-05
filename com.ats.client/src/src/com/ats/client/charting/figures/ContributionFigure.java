package com.ats.client.charting.figures;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import com.ats.platform.BarSeries;
import com.ats.platform.ChartContribution;
import com.ats.platform.OrderSide;

public class ContributionFigure extends Figure {
	private static final Logger logger = Logger.getLogger(ContributionFigure.class);

	private double data[];
	private BarSeries series;
	private ChartContribution contribution;
	
	public ContributionFigure(BarSeries series, ChartContribution contribution) {
		this.data = contribution.getData();
		this.series = series;
		this.contribution = contribution;
		
		setOpaque(false);
	}
	
	@Override
	protected void paintClientArea(Graphics graphics) {
		if( data == null || data.length <= 0 ) {
			return;
		}
		//Rectangle rect = (Rectangle)getParent().getLayoutManager().getConstraint(this);
		Rectangle rect = getBounds();
		//logger.debug("Contribution bounds: " + rect);
		graphics.translate(rect.x, rect.y);

		double highestHigh = ((BarSeriesFigure)getParent()).getHighestHigh();
		double lowestLow = ((BarSeriesFigure)getParent()).getLowestLow();
		double dollarRange = highestHigh - lowestLow;
		int barwidth = rect.width / series.size();
		

		if( contribution.getColor() != null ) {
			graphics.setForegroundColor(contribution.getColor());
		} else {
			graphics.setForegroundColor(ColorConstants.darkBlue);
		}

		int deltax = series.size() - data.length;
		for( int i = 1; i < data.length; i++ ) {
			double topRatio0 = (highestHigh - data[i-1])/dollarRange;
			int y0 = (int)((topRatio0)*rect.height);
		
			double topRatio1 = (highestHigh - data[i])/dollarRange;
			int y1 = (int)((topRatio1)*rect.height);
			
			int x0 = (i-1+deltax)*barwidth;
			int x1 = (i+deltax)*barwidth;
			
			graphics.drawLine(x0, y0, x1, y1);
		}
	}
}
