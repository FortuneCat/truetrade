package com.ats.client.runtime.views;

import static com.ats.engine.PositionManager.PROP_ADD_POSITION;
import static com.ats.engine.PositionManager.PROP_EXECUTION;
import static com.ats.engine.PositionManager.PROP_RESET;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;

import com.ats.db.PlatformDAO;
import com.ats.engine.JExecution;
import com.ats.engine.PositionManager;
import com.ats.engine.StrategyDefinition;
import com.ats.platform.Position;
import com.ats.platform.Strategy;
import com.ats.platform.TimeSpan;
import com.ats.utils.Utils;

public class StrategyStatusView extends ViewPart {
	private static final Logger logger = Logger.getLogger(StrategyStatusView.class);

	public static final String ID = "com.ats.client.runtime.views.strategyStatusView";
	
	private TableViewer positionTable;
	private TableViewer executionTable;
	
	private List<Position> positions;
	private List<JExecution> executions;
	
	private StrategyDefinition strategyDefinition;
	
	// TODO: remove, only for debugging
	private Strategy strategy;
	
	public void setStrategyDefinition(StrategyDefinition stratDef) {
		this.strategyDefinition = stratDef;
	    this.setPartName(strategyDefinition.getStrategyClass().getSimpleName());
	}
	
	@Override
	public void createPartControl(Composite parent) {
		
		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		
		TabItem posItem = new TabItem(tabFolder, SWT.NONE);
		posItem.setText("Positions");
		
		Composite posComp = new Composite(tabFolder, SWT.NONE);
	    posComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	    posComp.setLayout(new GridLayout());
	    posItem.setControl(posComp);
	    
	    positionTable = new TableViewer(posComp, SWT.MULTI | SWT.FULL_SELECTION);
	    positionTable.getTable().setHeaderVisible(true);
	    positionTable.getTable().setLinesVisible(true);
	    positionTable.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	    positionTable.setContentProvider(new PositionContentProvider());
	    positionTable.setLabelProvider(new PositionLabelProvider());
	    //positionTable.setInput()
	    
	    Table table = positionTable.getTable();
	    TableColumn column = new TableColumn(table, SWT.NONE);
	    column.setText("Symb");
	    column.setWidth(60);
	    
	    column = new TableColumn(table, SWT.NONE);
	    column.setText("Dir");
	    column.setWidth(60);
	    
	    column = new TableColumn(table, SWT.NONE);
	    column.setText("Size");
	    column.setWidth(60);
	
	    column = new TableColumn(table, SWT.NONE);
	    column.setText("Avg Cost");
	    column.setWidth(60);
	    
	    column = new TableColumn(table, SWT.NONE);
	    column.setText("UnrP&L");
	    column.setWidth(60);
	    
	    column = new TableColumn(table, SWT.NONE);
	    column.setText("RealP&L");
	    column.setWidth(60);
	
	    
		TabItem execItem = new TabItem(tabFolder, SWT.NONE);
		execItem.setText("Executions");
	
		Composite execComp = new Composite(tabFolder, SWT.NONE);
		execComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		execComp.setLayout(new GridLayout());
		execItem.setControl(execComp);
	
	    executionTable = new TableViewer(execComp, SWT.MULTI | SWT.FULL_SELECTION);
	    executionTable.getTable().setHeaderVisible(true);
	    executionTable.getTable().setLinesVisible(true);
	    executionTable.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	    executionTable.setContentProvider(new ExecutionContentProvider());
	    executionTable.setLabelProvider(new ExecutionLabelProvider());
	
	    table = executionTable.getTable();
	    column = new TableColumn(table, SWT.NONE);
	    column.setText("Symb");
	    column.setWidth(60);
	    
	    column = new TableColumn(table, SWT.NONE);
	    column.setText("Dir");
	    column.setWidth(60);
	    
	    column = new TableColumn(table, SWT.NONE);
	    column.setText("Size");
	    column.setWidth(60);
	    
	    column = new TableColumn(table, SWT.NONE);
	    column.setText("Cost");
	    column.setWidth(60);
	    
	    column = new TableColumn(table, SWT.NONE);
	    column.setText("Time");
	    column.setWidth(60);
	    
	    initPositionListener();
	    
	    
	    // TODO: remove, only for debugging
		TabItem debugItem = new TabItem(tabFolder, SWT.NONE);
		debugItem.setText("Debugging");
		final Composite debugComp = new Composite(tabFolder, SWT.NONE);
		debugComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		GridLayout glayout = new GridLayout();
		glayout.numColumns = 2;
		debugComp.setLayout(glayout);
		debugItem.setControl(debugComp);
		
		Button btn = new Button(debugComp, SWT.PUSH);
		btn.setText("BUY");
		btn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				
				getStrategy().buy(1);
			}
		});
		btn = new Button(debugComp, SWT.PUSH);
		btn.setText("SELL");
		btn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				getStrategy().sell(1);
			}
		});

		btn = new Button(debugComp, SWT.PUSH);
		btn.setText("BUY Limit");
		btn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				InputDialog id = new InputDialog(debugComp.getShell(), "Limit", "Limit buy price", "0.0", null);
				if( id.open() == id.OK ) {
					try {
						double limit = Double.parseDouble(id.getValue());
						getStrategy().buyLimit(1, limit);
					} catch( Exception e ) {}
				}
			}
		});
		btn = new Button(debugComp, SWT.PUSH);
		btn.setText("SELL Limit");
		btn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				InputDialog id = new InputDialog(debugComp.getShell(), "Limit", "Limit sell price", "0.0", null);
				if( id.open() == id.OK ) {
					try {
						double limit = Double.parseDouble(id.getValue());
						getStrategy().sellLimit(1, limit);
					} catch( Exception e ) {}
				}
			}
		});

		btn = new Button(debugComp, SWT.PUSH);
		btn.setText("BUY Stop");
		btn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				InputDialog id = new InputDialog(debugComp.getShell(), "Limit", "Stop buy price", "0.0", null);
				if( id.open() == id.OK ) {
					try {
						double limit = Double.parseDouble(id.getValue());
						getStrategy().buyStop(1, limit);
					} catch( Exception e ) {}
				}
			}
		});
		btn = new Button(debugComp, SWT.PUSH);
		btn.setText("SELL Stop");
		btn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				InputDialog id = new InputDialog(debugComp.getShell(), "Limit", "Stop sell price", "0.0", null);
				if( id.open() == id.OK ) {
					try {
						double limit = Double.parseDouble(id.getValue());
						getStrategy().sellStop(1, limit);
					} catch( Exception e ) {}
				}
			}
		});

		btn = new Button(debugComp, SWT.PUSH);
		btn.setText("BUY Trailing Stop");
		btn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				try {
					InputDialog id = new InputDialog(debugComp.getShell(), "Limit", "Stop buy price", "0.0", null);
					id.open();
					double stopPrice = Double.parseDouble(id.getValue());
					id = new InputDialog(debugComp.getShell(), "Trail", "Trail amount", "0.0", null);
					id.open();
					double trailAmt = Double.parseDouble(id.getValue());
					getStrategy().buyTrailingStop(1, stopPrice, trailAmt);
				} catch( Exception e ) {}
			}
		});
		btn = new Button(debugComp, SWT.PUSH);
		btn.setText("SELL Trailing Stop");
		btn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				try {
					InputDialog id = new InputDialog(debugComp.getShell(), "Limit", "Stop sell price", "0.0", null);
					id.open();
					double stopPrice = Double.parseDouble(id.getValue());
					id = new InputDialog(debugComp.getShell(), "Trail", "Trail amount", "0.0", null);
					id.open();
					double trailAmt = Double.parseDouble(id.getValue());
					getStrategy().sellTrailingStop(1, stopPrice, trailAmt);
				} catch( Exception e ) {}
			}
		});

		
		btn = new Button(debugComp, SWT.PUSH);
		btn.setText("Erroneous Order");
		btn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				getStrategy().buyLimit(1, -12.3);
			}
		});

	}
	
	/** debugging */
	// TODO: debugging
	private Strategy getStrategy() {
		return strategy;
	}
	
	private void initPositionListener() {
		PositionManager.getInstance().addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if( PROP_RESET.equals(evt.getPropertyName()) ) {
					positions = new ArrayList<Position>();
					executions = new ArrayList<JExecution>();
					positionTable.setInput(positions);
					executionTable.setInput(executions);
				} else if( PROP_ADD_POSITION.equals(evt.getPropertyName()) ) {
					final Position pos = (Position)evt.getNewValue();
					if( ! strategyDefinition.getStrategyClass().equals(pos.getStrategy().getClass()) ) {
						// not for us
						return;
					}
					// TODO: remove, only for debugging
					if( strategy == null ) {
						strategy = pos.getStrategy();
					}
					positions.add(pos);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							positionTable.add(pos);
							positionTable.refresh(pos);
						}
					});
				} else if( PROP_EXECUTION.equals(evt.getPropertyName()) ) {
					final Position pos = (Position)evt.getOldValue();
					if( ! strategyDefinition.getStrategyClass().equals(pos.getStrategy().getClass()) ) {
						// not for us
						return;
					}
					final JExecution exec = (JExecution)evt.getNewValue();
					executions.add(exec);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							try {
								if( positionTable != null && !positionTable.getTable().isDisposed()) {
									positionTable.refresh(pos);
								}
								if( executionTable != null && !executionTable.getTable().isDisposed()) {
									executionTable.add(exec);
									executionTable.refresh(exec);
								}
							} catch( Exception e) {}
						}
					});
				}
			}
		});

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	
	class PositionLabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			Position pos = (Position)element;
			switch(columnIndex) {
			case 0:
				return pos.getInstrument().getSymbol();
			case 1:
				return pos.getSide().toString();
			case 2:
				return Integer.toString(pos.getQuantity());
			case 3:
				return Utils.currencyForm.format(pos.getAvgPrice());
			case 4:
				return "unreal";
			case 5:
				return "real";	
			}
			return "";
		}
		public void addListener(ILabelProviderListener listener) {
		}
		public void dispose() {
		}
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}
		public void removeListener(ILabelProviderListener listener) {
		}
	}
	class PositionContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((List)inputElement).toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class ExecutionLabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			JExecution exec = (JExecution)element;
			switch(columnIndex) {
			case 0:
				logger.debug("For symbol: " + exec + " + " + exec.getInstrument());
				return exec.getInstrument().getSymbol();
			case 1:
				return exec.getSide().toString();
			case 2:
				return Integer.toString(exec.getQuantity());
			case 3:
				return Utils.currencyForm.format(exec.getPrice());
			case 4:
				return Utils.timeAndDateFormat.format(exec.getDateTime());
			}
			return "";
		}
		public void addListener(ILabelProviderListener listener) {
		}
		public void dispose() {
		}
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}
		public void removeListener(ILabelProviderListener listener) {
		}
	}
	class ExecutionContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((List)inputElement).toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
}
