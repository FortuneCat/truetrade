package com.ats.client.charting.figures;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.ats.platform.Bar;
import com.ats.utils.Utils;

public class BarFigure extends Figure {
	private static final Logger logger = Logger.getLogger(BarFigure.class);

	private static Color COLOR_UP = ColorConstants.white;
	private static Color COLOR_DOWN = ColorConstants.red;
	private static Color COLOR_BORDER = ColorConstants.black;
	
	
	private Bar priceBar;
	
	public BarFigure(Bar priceBar) {
		this.priceBar = priceBar;
		setOpaque(true);
		
		setToolTip(new PriceBarTooltip(
				"H= " + priceBar.getHigh()
				+ "\nL= " + priceBar.getLow()
				+ "\nO= " + priceBar.getOpen()
				+ "\nC= " + priceBar.getClose()
				+ "\nV= " + priceBar.getVolume()
				+ "\nBegin=" + Utils.timeAndDateFormat.format(priceBar.getBeginTime())
				+ "\nEnd=" + Utils.timeAndDateFormat.format(priceBar.getEndTime())));
		
		//setBorder(new LineBorder());
	}
	
	public Bar getBar() {
		return priceBar;
	}
	
	@Override
	protected void paintClientArea(Graphics graphics) {
		//Rectangle rect = (Rectangle)getParent().getLayoutManager().getConstraint(this);
		Rectangle rect = getBounds();
		//logger.debug("PriceBar bounds: " + rect);

		double highestHigh = ((BarSeriesFigure)getParent()).getHighestHigh();
		double lowestLow = ((BarSeriesFigure)getParent()).getLowestLow();
		double dollarRange = highestHigh - lowestLow;
		
		// the ratio of the total colum this bar will fill
		double rangeRatio = (priceBar.getHigh() - priceBar.getLow())/dollarRange;
		// the amount of white space at the top
		double topRatio = (highestHigh - priceBar.getHigh())/dollarRange;
		// space btween top and open
		double openRatio = (highestHigh - priceBar.getOpen())/dollarRange;
		double closeRatio = (highestHigh - priceBar.getClose())/dollarRange;
		double lowRatio = (highestHigh - priceBar.getLow())/dollarRange;
		
		int drawHigh = (int)((topRatio)*rect.height);
		int drawOpen = (int)((openRatio)*rect.height);
		int drawClose = (int)((closeRatio)*rect.height);
		int drawLow = (int)((lowRatio)*rect.height);

		
		// move in 1 pixel so the inside borders show up
		graphics.translate(rect.x, rect.y);
		
		Dimension size = new Dimension(rect.width, rect.height);
		
		size.width = size.width-2;
		
		int top = Math.min(drawOpen, drawClose);
		int bottom = Math.max(drawOpen, drawClose);
		
		if( priceBar.getClose() >= priceBar.getOpen() ) {
			graphics.setBackgroundColor(COLOR_UP);
		} else {
			graphics.setBackgroundColor(COLOR_DOWN);
		}
		graphics.setForegroundColor(COLOR_BORDER);
		// draw body
		graphics.fillRectangle(0, top, size.width, bottom-top);
		graphics.drawRectangle(0, top, size.width, bottom-top);
		
//		logger.debug("Drawing price bar, high=" + priceBar.getHigh() + ", drawing " + top);
		
		// upper wick
		graphics.drawLine(size.width/2, drawHigh, size.width/2, top);
		
		// lower wick
		graphics.drawLine(size.width/2, bottom, size.width/2, drawLow);
	}
	
	
	class PriceBarTooltip extends Figure {
		private String tip;
		public PriceBarTooltip(String tip) {
			this.tip = tip;
			setLayoutManager(new ToolbarLayout());
			add(new Label(tip));
		}
	}
}
