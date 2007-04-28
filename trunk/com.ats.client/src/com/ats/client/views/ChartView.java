package com.ats.client.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.Figure;
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
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

import com.ats.client.Activator;
import com.ats.client.charting.figures.BarSeriesFigure;
import com.ats.client.charting.figures.ContributionFigure;
import com.ats.client.charting.figures.ExecutionFigure;
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

public class ChartView extends ViewPart {
	public static final String ID = "com.ats.client.views.chartView";
	
	private TimeSpan span = TimeSpan.daily;
	private Position position;

	private ISelectionListener listener;
	private LightweightSystem lws;
	private ScrollPane scrollpane;
	
	private Action zoomInAction;
	private Action zoomOutAction;
	
	private Action tallerAction;
	private Action shorterAction;
	
	private ControlContribution timeSpanControl;
	
	private BarSeriesFigure seriesFigure;
	private List<ExecutionFigure> executionFigures = Collections.emptyList();
	private List<ExecutionFigure> selectedFigures = new ArrayList<ExecutionFigure>();
	
	private void createActions() {
		zoomInAction = new Action() {
			public void run() {
				Display.getCurrent().asyncExec(new Runnable() {
					public void run() {
						BarSeriesFigure.increaseWidth(5);
						((BarSeriesFigure) scrollpane.getContents())
								.resetConstraints();
						//scrollpane.invalidate();
					}
				});
			}
		};
		zoomInAction.setText("Zoom in");
        zoomInAction.setImageDescriptor(Activator.getImageDescriptor("/icons/plus.gif"));
		
		zoomOutAction = new Action() {
			public void run() {
				Display.getCurrent().asyncExec(new Runnable() {
					public void run() {
						BarSeriesFigure.decreaseWidth(5);
						((BarSeriesFigure)scrollpane.getContents()).resetConstraints();
						scrollpane.repaint();
					}
				});
			}
		};
		zoomOutAction.setText("Zoom out");
        zoomOutAction.setImageDescriptor(Activator.getImageDescriptor("/icons/minus.gif"));
		
		tallerAction = new Action() {
			public void run() {
				Display.getCurrent().asyncExec(new Runnable() {
					public void run() {
						if( seriesFigure != null ) {
							seriesFigure.increaseHeight(100);
							scrollpane.repaint();
						}
					}
				});
			}
		};
		tallerAction.setText("Increase height");
		tallerAction.setImageDescriptor(Activator.getImageDescriptor("/icons/arrow_up.gif"));
		
		shorterAction = new Action() {
			public void run() {
				Display.getCurrent().asyncExec(new Runnable() {
					public void run() {
						if( seriesFigure != null ) {
							seriesFigure.decreaseHeight(100);
							scrollpane.repaint();
						}
					}
				});
			}
		};
		shorterAction.setText("Decrease height");
		shorterAction.setImageDescriptor(Activator.getImageDescriptor("/icons/arrow_down.gif"));
		
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
		mgr.add(tallerAction);
		mgr.add(shorterAction);
		mgr.add(zoomInAction);
		mgr.add(zoomOutAction);
		mgr.add(timeSpanControl);
	}
	
	
	@Override
	public void createPartControl(Composite parent) {
		Canvas canvas = new Canvas(parent, SWT.NO_BACKGROUND);
		canvas.setLayout(new FillLayout());

		lws = new LightweightSystem(canvas);
		
		IFigure panel = new Figure();
		ToolbarLayout layout = new ToolbarLayout();
		layout.setStretchMinorAxis(true);
		layout.setVertical(true);
		layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
		panel.setLayoutManager(layout);
		lws.setContents(panel);
		
		scrollpane = new ScrollPane();
//		scrollpane.setBounds(new Rectangle(0,0,510,320));
		scrollpane.setHorizontalScrollBarVisibility(ScrollPane.ALWAYS);
		scrollpane.setVerticalScrollBarVisibility(ScrollPane.ALWAYS);
		scrollpane.getViewport().setBorder(new LineBorder());
		scrollpane.setBorder(new LineBorder());
		scrollpane.setContents(new BarSeriesFigure());
		scrollpane.getViewport().addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if( ChartView.this.seriesFigure != null ) {
					Viewport viewPort = scrollpane.getViewport();
					seriesFigure.resetConstraints(viewPort.getViewLocation(), viewPort.getClientArea());
				}
			}
		});
	
		panel.add(scrollpane);

		createActions();
	}
	

	private void setTimeSpan(TimeSpan span) {
		this.span = span;
		renderChart();
	}

	private void setPosition(Position pos) {
		this.position = pos;
		this.executionFigures.clear();
		timeSpanControl.update();
		renderChart();
	}
	private void renderChart() {
		if( position == null ) {
			//lws.setContents(new PriceSeriesFigure());
			seriesFigure = new BarSeriesFigure();
			scrollpane.setContents(new BarSeriesFigure());
			executionFigures = new ArrayList<ExecutionFigure>(0);
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
		seriesFigure = new BarSeriesFigure();
		seriesFigure.setSeries(series);
		
		// add executions
		executionFigures = new ArrayList<ExecutionFigure>();
		List<JExecution> executions = position.getExecutions();
		for (JExecution exec : executions) {
			ExecutionFigure fig = new ExecutionFigure(exec);
			executionFigures.add(fig);
			seriesFigure.add(fig);
		}

		Strategy strategy = position.getStrategy();
		// add chart contibutions
		for (ChartContribution contribution : strategy.getContributions(series)) {
			ContributionFigure contribFig = new ContributionFigure(series,
					contribution);
			seriesFigure.add(contribFig);
		}
		
		
		seriesFigure.resetConstraints();

		//lws.setContents(seriesFigure);
		scrollpane.setContents(seriesFigure);
	}
	
	

	private void clearSelections() {
		for(final ExecutionFigure fig : selectedFigures ) {
			fig.setSelected(false);
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					fig.repaint();
				}
			});
		}
		selectedFigures.clear();
	}
	private void selectExecution(JExecution execution) {
		clearSelections();
		for(final ExecutionFigure fig : executionFigures ) {
			if( fig.getExecution().equals(execution) ) {
				fig.setSelected(true);
				selectedFigures.add(fig);
				scrollpane.scrollHorizontalTo(fig.getBounds().x);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						fig.repaint();
					}
				});
			}
		}
	}
	
	private void selectTrade(TradeSummary trade) {
		clearSelections();
		final List<ExecutionFigure> buyFigs = new ArrayList<ExecutionFigure>();
		final List<ExecutionFigure> sellFigs = new ArrayList<ExecutionFigure>();
		ExecutionFigure firstFig = null;
		ExecutionFigure lastFig = null;
		// TODO: that's a lot of linear searches.  Does it affect performance?
		for( JExecution execution : trade.getExecutions() ) {
			for(final ExecutionFigure fig : executionFigures ) {
				if( fig.getExecution().equals(execution) ) {
					if( firstFig == null ) {
						firstFig = fig;
						scrollpane.scrollHorizontalTo(fig.getBounds().x);
					}
					lastFig = fig;
					if( execution.getSide() == OrderSide.BUY ) {
						fig.setSelected(true);
						selectedFigures.add(fig);
						buyFigs.add(fig);
					} else {
						fig.setSelected(true);
						sellFigs.add(fig);
						selectedFigures.add(fig);
					}
				}
			}
		}
		// connect the first and last
		final PolylineConnection connection = new PolylineConnection();
		connection.setSourceAnchor(firstFig.getConnectionAnchor());
		connection.setTargetAnchor(lastFig.getConnectionAnchor());
		//connection.setConnectionRouter( );
		seriesFigure.add(connection);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				connection.repaint();
				for(ExecutionFigure fig : buyFigs ) {
					fig.repaint();
				}
				for(ExecutionFigure fig : sellFigs ) {
					fig.repaint();
				}
			}
		});
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
				if( part == ChartView.this || !( selection instanceof IStructuredSelection) ) {
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
//			setInstrument(((Position)obj).getInstrument());
		} else if( obj instanceof JExecution ) {
			selectExecution((JExecution)obj);
		} else if( obj instanceof TradeSummary ) {
			selectTrade((TradeSummary)obj);
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(listener);
	}

}
