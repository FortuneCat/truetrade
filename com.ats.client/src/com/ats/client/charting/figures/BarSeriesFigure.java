package com.ats.client.charting.figures;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;


import com.ats.client.views.ChartView;
import com.ats.platform.Bar;
import com.ats.platform.BarSeries;

public class BarSeriesFigure extends Figure {
	
	private LayoutManager layout;
	private BarSeries series;
	
	private double highestHigh;
	private double lowestLow;
	
	private static int width = 10;
	private int height = 300;
	
	public BarSeriesFigure() {
		//layout = new StackLayout();
		layout = new XYLayout();
		setLayoutManager(layout);
		setBackgroundColor(ColorConstants.white);
		setOpaque(true);
	}
	
	public static void increaseWidth(int increase) {
		width += Math.abs(increase);
	}
	public static void decreaseWidth(int decrease) {
		width -= Math.abs(decrease);
		if( width < 4 ) {
			width = 4;
		}
	}
	
//	public void increaseHeight() {
//	}
	
	public void  increaseHeight(int increase) {
		height *= 1.5;
		resetConstraints();
	}
	public void decreaseHeight(int decrease) {
		height -= decrease;
		if( height < 100 ) {
			height = 100;
			resetConstraints();
		}
	}
	
	public void resetConstraints(Point viewLocation, Rectangle clientArea ) {
		//height = getParent().getBounds().height;
		
		int firstFig = viewLocation.x / width;
		int lastFig = firstFig + (clientArea.width / width);
		
		highestHigh = series.highestHigh(series.size() - firstFig - 1, series.size() - lastFig - 1).getHigh(); 
		lowestLow = series.lowestLow(series.size() - firstFig - 1, series.size() - lastFig - 1).getLow(); 
		double delta = highestHigh - lowestLow;
		highestHigh += (delta * 0.1);
		lowestLow -= (delta * 0.1);
		layout();
	}

	public void resetConstraints() {
		//height = getParent().getBounds().height;
		height = Math.max( height, ChartView.getInstance().getBounds().height);

		List<Figure> children = (List<Figure>)getChildren();
		List<BarFigure> barFigures = new ArrayList<BarFigure>();
		List<ExecutionFigure> executionFigures = new ArrayList<ExecutionFigure>();
		int count = 0;
		for(Figure figure : children) {
			if( figure instanceof BarFigure ) {
				layout.setConstraint(figure, new Rectangle(count * width, 0, width, height));
				barFigures.add((BarFigure)figure);
				count++;
			}
			if( figure instanceof ExecutionFigure ) {
				executionFigures.add((ExecutionFigure)figure);
			}
			if( figure instanceof ContributionFigure ) {
				layout.setConstraint(figure, new Rectangle(0, 0, width * series.size(), height));
			}
		}
		
		// calc constraints for executions
		for( ExecutionFigure fig : executionFigures ) {
			// it would be nice to calculate this somehow, but this is simple
			Date time = fig.getExecution().getDateTime();
			for( BarFigure barFig : barFigures ) {
				if( barFig.getBar().contains(time) ) {
					layout.setConstraint(fig, layout.getConstraint(barFig));
					break;
				}
			}
		}
		
		layout();
	}
	
	public void setSeries(BarSeries series) {
		this.series = series;
		
		Iterator<Bar> it = series.getBarsFromOldest();
		int count = 0;
		while( it.hasNext() ) {
			Bar bar = it.next();
			BarFigure figure = new BarFigure(bar);
			add(figure);
			layout.setConstraint(figure, new Rectangle(count * width, 0, width, height));
			count++;
		}
	}

	public double getHighestHigh() {
		if( highestHigh > 0 ) {
			return highestHigh;
		} else {
			return series.highestHigh().getHigh();
		}
	}
	public double getLowestLow() {
		if( lowestLow > 0 ) {
			return lowestLow;
		} else {
			return series.lowestLow().getLow();
		}
	}
}

