package com.ats.client.views;

import java.awt.Color;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.Viewport;
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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.xy.DefaultOHLCDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

import com.ats.client.Activator;
import com.ats.client.charting.figures.BarSeriesFigure;
import com.ats.client.charting.figures.ContributionFigure;
import com.ats.client.charting.figures.ExecutionFigure;
import com.ats.client.tools.BarSeriesDataset;
import com.ats.db.PlatformDAO;
import com.ats.engine.JExecution;
import com.ats.engine.TradeSummary;
import com.ats.platform.BarSeries;
import com.ats.platform.ChartContribution;
import com.ats.platform.OrderSide;
import com.ats.platform.Position;
import com.ats.platform.Strategy;
import com.ats.platform.TimeSpan;
import com.ats.utils.Utils;

/**
 * This is a stop-gap measure to provide simple, reliable charting while
 * working on delivering a proper, high-powered charting system.
 * 
 * @author Adrian
 *
 */
public class JFreeChartView extends ViewPart {
	public static final String ID = "com.ats.client.views.jfreeChartView";
	
	private TimeSpan span = TimeSpan.daily;
	private Position position;

	private ISelectionListener listener;
	private ChartPanel jfreeChartPanel;
	
//	private Action zoomInAction;
//	private Action zoomOutAction;
//	
//	private Action tallerAction;
//	private Action shorterAction;
	
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
//		zoomInAction = new Action() {
//			public void run() {
//				Display.getCurrent().asyncExec(new Runnable() {
//					public void run() {
//						BarSeriesFigure.increaseWidth(5);
//						((BarSeriesFigure) scrollpane.getContents())
//								.resetConstraints();
//						//scrollpane.invalidate();
//					}
//				});
//			}
//		};
//		zoomInAction.setText("Zoom in");
//        zoomInAction.setImageDescriptor(Activator.getImageDescriptor("/icons/plus.gif"));
//		
//		zoomOutAction = new Action() {
//			public void run() {
//				Display.getCurrent().asyncExec(new Runnable() {
//					public void run() {
//						BarSeriesFigure.decreaseWidth(5);
//						((BarSeriesFigure)scrollpane.getContents()).resetConstraints();
//						scrollpane.repaint();
//					}
//				});
//			}
//		};
//		zoomOutAction.setText("Zoom out");
//        zoomOutAction.setImageDescriptor(Activator.getImageDescriptor("/icons/minus.gif"));
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
//		mgr.add(zoomInAction);
//		mgr.add(zoomOutAction);
		mgr.add(timeSpanControl);
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
		
		createActions();
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
			jfreeChartPanel.setChart(null);
			return;
		}
		
		BarSeries series = null;
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
		
		jfreeChartPanel.setChart(chart);
		
		
		// add executions
		XYSeries buySeries = new XYSeries("BUY");
		XYSeries sellSeries = new XYSeries("SELL");
		List<JExecution> executions = position.getExecutions();
		for (JExecution exec : executions) {
			if( exec.getSide() == OrderSide.BUY ) {
//				XYShapeAnnotation ann = new XYShapeAnnotation(ShapeUtilities.createUpTriangle(2));
//				chart.getXYPlot().addAnnotation(ann);
				buySeries.add(exec.getDateTime().getTime(), exec.getPrice());
			} else {
//				XYShapeAnnotation ann = new XYShapeAnnotation(ShapeUtilities.createDownTriangle(2));
//				chart.getXYPlot().addAnnotation(ann);
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
