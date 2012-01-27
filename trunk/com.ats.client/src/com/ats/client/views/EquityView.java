package com.ats.client.views;

import java.awt.Frame;
import java.util.Collection;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.ats.engine.TradeSummary;
import com.ats.platform.Position;

public class EquityView extends ViewPart {
	public static final String ID = "com.ats.client.views.equityView";

	//private Composite content;
	private ChartPanel jfreeChartPanel;
	
	private ISelectionListener listener;
	
	@Override
	public void setFocus() {
		ISelection selection = getSite().getWorkbenchWindow().getSelectionService().getSelection(StrategyResultsView.ID);
		setSelection(selection);
	}


	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		
		listener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				if( part == EquityView.this || !( selection instanceof IStructuredSelection) ) {
					// don't listen to my own or unstructured messages
					return;
				}
				setSelection(selection);
			}

		};
		
		getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(StrategyResultsView.ID, listener);
	}
	
	private void setSelection(ISelection selection) {
		IStructuredSelection sel = (IStructuredSelection)selection;
		Object obj = sel.getFirstElement();
		if( obj instanceof Position ) {
			setTrades(((Position)obj).getTradeSummary());
		} else if( obj instanceof Collection) {
			setTrades((Collection<TradeSummary>)obj); 
		} else {
			setTrades(null);
		}
	}

	private void setTrades(Collection<TradeSummary> trades) {
		if( trades == null || trades.size() <= 0) {
			jfreeChartPanel.setChart(null);
			return;
		}
		
		XYSeries pnlSeries = new XYSeries("P&L");
		XYSeries perTradeSeries = new XYSeries("Trade P&L");
		XYSeries drawdownSeries = new XYSeries("Drawdown");
		pnlSeries.add(0, 0);
		double pnlTotal = 0.0;
		double drawdownTotal = 0.0;
		int index = 0;
		for(TradeSummary trade : trades ) {
			pnlTotal += trade.getRealizedNetPnL();
			drawdownTotal += trade.getRealizedNetPnL();
			drawdownTotal = Math.min(drawdownTotal, 0);
			pnlSeries.add(index+1, pnlTotal);
			perTradeSeries.add(index+1, trade.getRealizedPnL());
			drawdownSeries.add(index, drawdownTotal);
			
			index++;
		}
		XYSeriesCollection pnlData = new XYSeriesCollection(pnlSeries);
		JFreeChart chart = ChartFactory.createXYLineChart(
				"",
				"Trade Number",
				"Net profit",
				pnlData,
				PlotOrientation.VERTICAL,
				false,
				false,
				false);
		
		
		// prepare chart for multiple charts
		CombinedDomainXYPlot plot = new CombinedDomainXYPlot(chart.getXYPlot().getDomainAxis());
		plot.add(chart.getXYPlot(), 2);
		
		// add per-trade histogram
		XYDataset perTradeDataset = new XYSeriesCollection(perTradeSeries);
		XYItemRenderer perTradeRenderer = new XYBarRenderer();
		XYPlot perTradePlot = new XYPlot(perTradeDataset, null, new NumberAxis("$ / trade"), perTradeRenderer);
		perTradePlot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
		plot.add(perTradePlot, 1);
		
		// add drawdown histogram
		XYDataset drawdownDataset = new XYSeriesCollection(drawdownSeries);
		XYItemRenderer drawdownRenderer = new XYAreaRenderer();
		drawdownRenderer.setBaseItemLabelPaint(java.awt.Color.blue);
		XYPlot drawdownPlot = new XYPlot(drawdownDataset, null, new NumberAxis("drawdown"), drawdownRenderer);
		perTradePlot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
		plot.add(drawdownPlot, 1);
		
		
		plot.setOrientation(PlotOrientation.VERTICAL);
		
		chart = new JFreeChart( "", null, plot, false);
		chart.setBackgroundPaint(java.awt.Color.white);

		jfreeChartPanel.setChart(chart);
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite content = new Composite(parent, SWT.EMBEDDED);  // EMBEDDED to support JFreeChart
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
        gridLayout.numColumns = 1;
        content.setLayout(gridLayout);
        
		Frame chartFrame = SWT_AWT.new_Frame(content);
		jfreeChartPanel = new ChartPanel(null);
		chartFrame.add(jfreeChartPanel);
	}

}
