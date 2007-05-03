package com.ats.client.views;

import java.awt.Color;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

import com.ats.client.Activator;
import com.ats.client.tools.BarSeriesDataset;
import com.ats.db.PlatformDAO;
import com.ats.engine.JExecution;
import com.ats.platform.BarSeries;
import com.ats.platform.OrderSide;
import com.ats.platform.Position;
import com.ats.platform.TimeSpan;
import com.meterware.httpunit.GetMethodWebRequest;

/**
 * This is a stop-gap measure to provide simple, reliable charting while
 * working on delivering a proper, high-powered charting system.
 * 
 * Used techniques described at: http://www.jfree.org/phpBB2/viewtopic.php?t=6342
 * 
 * @author Adrian
 *
 */
public class JFreeChartView extends ViewPart {
	private static final Logger logger = Logger.getLogger(JFreeChartView.class);
	
	public static final String ID = "com.ats.client.views.jfreeChartView";
	
	private TimeSpan span = TimeSpan.daily;
	private Position position;
	private BarSeries series;

	private ISelectionListener listener;
	private ChartPanel jfreeChartPanel;
	private Slider slider;
	private Range initialRange;
	
	private Action zoomInAction;
	private Action zoomOutAction;
//	private Action increaseWidth;
//	private Action decreaseWidth;
//	private Action increaseSpacing;
//	private Action decreaseSpacing;
	
	private ControlContribution timeSpanControl;
	
	private static JFreeChartView instance;
	private Canvas canvas;
	public Rectangle getBounds() {
		return canvas.getBounds();
	}
	public static JFreeChartView getInstance() {
		return instance;
	}
	
	public JFreeChartView() {
		instance = this;
	}
	
	private void createActions() {
		
		zoomInAction = new Action() {
			public void run() {
				int newThumb = Math.max( 10, slider.getThumb() / 2);
				slider.setThumb(newThumb);
				slider.setIncrement(slider.getThumb()/2);
				resynchChart();
			}
		};
		zoomInAction.setText("Zoom in");
        zoomInAction.setImageDescriptor(Activator.getImageDescriptor("/icons/plus.gif"));
		
		zoomOutAction = new Action() {
			public void run() {
				slider.setThumb(slider.getThumb() * 2);
				slider.setIncrement(slider.getThumb()/2);
				resynchChart();
			}
		};
		zoomOutAction.setText("Zoom out");
        zoomOutAction.setImageDescriptor(Activator.getImageDescriptor("/icons/minus.gif"));
//		
//		tallerAction = new Action() {
//			public void run() {
//				Display.getCurrent().asyncExec(new Runnable() {
//					public void run() {
//						if( seriesFigure != null ) {
//							seriesFigure.increaseHeight(100);
//							scrollpane.repaint();
//						}
//					}
//				});
//			}
//		};
//		tallerAction.setText("Increase height");
//		tallerAction.setImageDescriptor(Activator.getImageDescriptor("/icons/arrow_up.gif"));
//		
//		shorterAction = new Action() {
//			public void run() {
//				Display.getCurrent().asyncExec(new Runnable() {
//					public void run() {
//						if( seriesFigure != null ) {
//							seriesFigure.decreaseHeight(100);
//							scrollpane.repaint();
//						}
//					}
//				});
//			}
//		};
//		shorterAction.setText("Decrease height");
//		shorterAction.setImageDescriptor(Activator.getImageDescriptor("/icons/arrow_down.gif"));
		
		timeSpanControl = new ControlContribution("Time") {
			private Combo combo;
			protected Control createControl(Composite parent) {
				// TODO: remove time spans not accessible for this instrument
				combo = new Combo(parent, SWT.READ_ONLY);
				combo.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
						TimeSpan span = (TimeSpan)combo.getData(combo.getText());
						setTimeSpan(span);
					}
					public void widgetSelected(SelectionEvent e) {
						TimeSpan span = (TimeSpan)combo.getData(combo.getText());
						setTimeSpan(span);
					}
				});
				update();
				return combo;
			}
			@Override
			public void update() {
				combo.removeAll();
				if( position == null || position.getInstrument() == null ) {
					return;
				}
				// TODO: should use the time scale of the strategy definition
				List<Map<String, Object>> seriesData = PlatformDAO.getSeriesOverview(position.getInstrument());
				List<TimeSpan> supportedSpans = new ArrayList<TimeSpan>();
				for(Map<String, Object> curr : seriesData) {
					TimeSpan span = TimeSpan.values()[(Integer)curr.get("timespanId")];
					if( !supportedSpans.contains(span)) {
						supportedSpans.add(span);
					}
					for(TimeSpan s : TimeSpan.values()) {
						if( canConvert(span, s) ) {
							if( ! supportedSpans.contains(s)) {
								supportedSpans.add(s);
							}
						}
					}
				}
				for( TimeSpan span : supportedSpans ) {
					combo.add(span.toString());
					combo.setData(span.toString(), span);
				}
				if( supportedSpans.size() > 0 ) {
					combo.setText(supportedSpans.get(0).toString());
				}
			}
		};
		

		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
//		mgr.add(tallerAction);
//		mgr.add(shorterAction);
		mgr.add(zoomInAction);
		mgr.add(zoomOutAction);
		mgr.add(timeSpanControl);
	}
	
	
	@Override
	public void createPartControl(Composite parent) {
		Composite root = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
        gridLayout.numColumns = 1;
        root.setLayout(gridLayout);
		
		Composite content = new Composite(root, SWT.EMBEDDED);  // EMBEDDED to support JFreeChart
		GridData gdata = new GridData();
		gdata.grabExcessHorizontalSpace = true;
		gdata.grabExcessVerticalSpace = true;
		gdata.horizontalAlignment = GridData.FILL;
		gdata.verticalAlignment = GridData.FILL;
		content.setLayoutData(gdata);

		
		gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
        gridLayout.numColumns = 1;
        content.setLayout(gridLayout);
        
		Frame chartFrame = SWT_AWT.new_Frame(content);
		jfreeChartPanel = new ChartPanel(null, true);
		chartFrame.add(jfreeChartPanel);
		jfreeChartPanel.setMouseZoomable(false);
		
		slider = new Slider(root, SWT.NONE);
		gdata = new GridData();
		gdata.grabExcessHorizontalSpace = true;
		gdata.horizontalAlignment = GridData.FILL;
		slider.setLayoutData(gdata);
		slider.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				resynchChart();
			}
		});
		

		createActions();
	}

	private void resynchChart() {
		JFreeChart chart = jfreeChartPanel.getChart();
		if( chart != null ) {
			XYPlot plot = chart.getXYPlot();
			
			DateAxis dateAxis = (DateAxis)plot.getDomainAxis();
			double lower = (((double)slider.getSelection())*1000.0);
			double upper = lower + (((double)slider.getThumb())*1000.0);
			dateAxis.setRange(lower, upper );
			
			double high = series.highestHigh(dateAxis.getMinimumDate(), dateAxis.getMaximumDate());
			double low = series.lowestLow(dateAxis.getMinimumDate(), dateAxis.getMaximumDate());
			
			plot.getRangeAxis().setRangeWithMargins(low, high);
			
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					slider.redraw();
				}
			});
		}
	}


	private void setTimeSpan(TimeSpan span) {
		this.span = span;
		renderChart();
	}

	private void setPosition(Position pos) {
		this.position = pos;
		timeSpanControl.update();
		renderChart();
	}
	private void renderChart() {
		if( position == null ) {
			series = null;
			jfreeChartPanel.setChart(null);
			return;
		}
		
		List<Map<String, Object>> seriesData = PlatformDAO.getSeriesOverview(position.getInstrument());
		for(Map<String, Object> curr : seriesData) {
			TimeSpan tmpSpan = TimeSpan.values()[(Integer)curr.get("timespanId")];
			if( tmpSpan == span ) {
				series = PlatformDAO.getBarSeries(position.getInstrument(), tmpSpan);
				break;
			}
			if( canConvert(tmpSpan, span) ) {
				BarSeries tmpSeries = PlatformDAO.getBarSeries(position.getInstrument(), tmpSpan);
				series = tmpSeries.convertTimeSpan(span);
				break;
			}
		}

		if( series == null ) {
			// could not get data at this timeframe
			return;
		}
		OHLCDataset dataset = new BarSeriesDataset(series);
		JFreeChart chart = ChartFactory.createCandlestickChart( 
				series.getInstrument().getSymbol(),
				"Time", 
				"Price", 
				dataset,
				false);
		((CandlestickRenderer)chart.getXYPlot().getRenderer()).setDrawVolume(false);
		((CandlestickRenderer)chart.getXYPlot().getRenderer()).setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_AVERAGE);
		((CandlestickRenderer)chart.getXYPlot().getRenderer()).setAutoWidthGap(0);

		chart.getXYPlot().setOrientation(PlotOrientation.VERTICAL);
		
		NumberAxis vaxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
		vaxis.setAutoRangeIncludesZero(false);
		vaxis.setAutoRange(true);
		
		jfreeChartPanel.setChart(chart);
		DateAxis dateAxis = (DateAxis)chart.getXYPlot().getDomainAxis();
		initialRange = dateAxis.getRange();
		slider.setMinimum((int)(initialRange.getLowerBound()/1000));
		slider.setMaximum((int)(initialRange.getUpperBound()/1000));
		slider.setThumb((int)((initialRange.getUpperBound() - initialRange.getLowerBound())/10000));
		slider.setIncrement(slider.getThumb()/2);
		dateAxis.setRange(initialRange.getLowerBound(), 
				initialRange.getLowerBound() + (((double)slider.getThumb())*1000.0) );
		
		
		// add executions
		XYSeries buySeries = new XYSeries("BUY");
		XYSeries sellSeries = new XYSeries("SELL");
		List<JExecution> executions = position.getExecutions();
		for (JExecution exec : executions) {
			if( exec.getSide() == OrderSide.BUY ) {
				buySeries.add(exec.getDateTime().getTime(), exec.getPrice());
			} else {
				sellSeries.add(exec.getDateTime().getTime(), exec.getPrice());
			}
		}
		chart.getXYPlot().setDataset(1, new XYSeriesCollection(buySeries));
		chart.getXYPlot().setDataset(2, new XYSeriesCollection(sellSeries));
		DefaultXYItemRenderer buyRenderer = new DefaultXYItemRenderer();
		buyRenderer.setShape(ShapeUtilities.createUpTriangle(4));
		buyRenderer.setPaint(Color.black);
		buyRenderer.setLinesVisible(false);
		buyRenderer.setSeriesItemLabelPaint(1, Color.cyan);
		buyRenderer.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
		chart.getXYPlot().setRenderer(1, buyRenderer);
		chart.getXYPlot().setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		
		DefaultXYItemRenderer sellRenderer = new DefaultXYItemRenderer();
		sellRenderer.setShape(ShapeUtilities.createDownTriangle(4));
		sellRenderer.setPaint(Color.black);
		sellRenderer.setLinesVisible(false);
		sellRenderer.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
		sellRenderer.setSeriesItemLabelPaint(2, Color.orange);
		chart.getXYPlot().setRenderer(2, sellRenderer);
		
		// timeline zoomRange(lowerPercent, upperPercent)
		
//
//		Strategy strategy = position.getStrategy();
//		// add chart contibutions
//		for (ChartContribution contribution : strategy.getContributions(series)) {
//			ContributionFigure contribFig = new ContributionFigure(series,
//					contribution);
//			seriesFigure.add(contribFig);
//		}
//		
//		
//		seriesFigure.resetConstraints();
//
//		//lws.setContents(seriesFigure);
//		scrollpane.setContents(seriesFigure);
	}
	
	
	
	private boolean canConvert(TimeSpan baseSpan, TimeSpan target) {
		return target.getSpanInMillis() / baseSpan.getSpanInMillis() > 0 &&
				target.getSpanInMillis() % baseSpan.getSpanInMillis() == 0;
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		
		listener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				if( part == JFreeChartView.this || !( selection instanceof IStructuredSelection) ) {
					// don't listen to my own or unstructured messages
					return;
				}
				setSelection(selection);
			}

		};
		getSite().getWorkbenchWindow().getSelectionService()
			.addSelectionListener(ExecutionView.ID, listener);
		getSite().getWorkbenchWindow().getSelectionService()
			.addSelectionListener(TradeView.ID, listener);
		getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(StrategyResultsView.ID, listener);
	}
	
	@Override
	public void setFocus() {
		ISelection selection = getSite().getWorkbenchWindow()
				.getSelectionService().getSelection(StrategyResultsView.ID);
		setSelection(selection);
	}

	private void setSelection(ISelection selection) {
		IStructuredSelection sel = (IStructuredSelection)selection;
		Object obj = sel.getFirstElement();
		if( obj == null ) {
			setPosition(null);
		}
		if( obj instanceof Position ) {
			setPosition((Position)obj);
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(listener);
	}

}
