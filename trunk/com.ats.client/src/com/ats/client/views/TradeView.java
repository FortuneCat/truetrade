package com.ats.client.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.ats.engine.JExecution;
import com.ats.engine.TradeSummary;
import com.ats.platform.Position;
import com.ats.utils.Utils;

public class TradeView extends ViewPart implements ISelectionProvider {
	public static final String ID = "com.ats.client.views.tradeView";

	
	private TableViewer tableViewer;

	private List<TradeSummary> trades = new ArrayList<TradeSummary>();

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		
		ISelectionListener listener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				if( part == TradeView.this || !( selection instanceof IStructuredSelection) ) {
					// don't listen to my own or unstructured messages
					return;
				}
				IStructuredSelection sel = (IStructuredSelection)selection;
				Object obj = sel.getFirstElement();
				if( obj instanceof Position ) {
					setPosition((Position)obj);
				} else {
					setPosition((Position)null);
				}
			}
		};
		
		getSite().setSelectionProvider(this);

		getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(StrategyResultsView.ID, listener);
	}
	
	private void setPosition(Position position) {
		if( position == null ) {
			return;
		}
		
		// need to parse all of the executions to come up with trades.
		// Some executions will close one trade and simultaneously start a new one
		// (ie: it will be used by two trades).  Do we create two new virtual
		// executions for record-keeping?  That may have some consequences
		// for commissions, but IMHO it's an acceptible trade off
		
		trades = position.getTradeSummary();
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				tableViewer.setInput(trades);
				tableViewer.refresh();
			}
		});
		
	}

	
	@Override
	public void createPartControl(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
        content.setLayout(gridLayout);
        
        tableViewer = new TableViewer(content, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
        tableViewer.setContentProvider(new TradeContentProvider());
        tableViewer.setLabelProvider(new TradeLabelProvider());
        tableViewer.setInput(trades);

        
        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText("Trade Number");
        column.setWidth(40);
        
        column = new TableColumn(table, SWT.NONE);
        column.setText("Contract");
        column.setWidth(50);
        
        column = new TableColumn(table, SWT.NONE);
        column.setText("Entry Date/Time");
        column.setWidth(90);
        
        column = new TableColumn(table, SWT.NONE);
        column.setText("Exit Date/Time");
        column.setWidth(90);
        
        column = new TableColumn(table, SWT.NONE);
        column.setText("Entry Side");
        column.setWidth(50);
        
        column = new TableColumn(table, SWT.NONE);
        column.setText("Buy Qty");
        column.setWidth(50);
        
        column = new TableColumn(table, SWT.NONE);
        column.setText("Avg Buy Price");
        column.setWidth(50);
        
        column = new TableColumn(table, SWT.NONE);
        column.setText("Sell Qty");
        column.setWidth(50);
        
        column = new TableColumn(table, SWT.NONE);
        column.setText("Avg Sell Price");
        column.setWidth(50);
        
        column = new TableColumn(table, SWT.NONE);
        column.setText("Gross PNL");
        column.setWidth(50);
        
        column = new TableColumn(table, SWT.NONE);
        column.setText("Entry Text");
        column.setWidth(50);

        column = new TableColumn(table, SWT.NONE);
        column.setText("Exit Text");
        column.setWidth(50);

	}
	
	
	class TradeContentProvider implements IStructuredContentProvider {
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public Object[] getElements( Object input ) {
			return ((List)input).toArray();
		}
	}
	
	class TradeLabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			TradeSummary trade = (TradeSummary)element;
			String ret = null;
			switch(columnIndex) {
			case 0:
				ret = "" + trades.indexOf(trade);
				break;
			case 1:
				ret = trade.getInstrument().getSymbol();
				break;
			case 2:  // Entry date/time
				ret = Utils.timeAndDateFormat.format(trade.getBeginDate());
				break;
			case 3:  // Exit date/time
				ret = Utils.timeAndDateFormat.format(trade.getEndDate());
				break;
			case 4:  // Entry side
				ret = trade.getEntrySide().name();
				break;
			case 5:  // buy size
				ret = Integer.toString(trade.getTotalBuyQty()); 
				break;
			case 6:  // Avg buy price
				ret = Double.toString(trade.getAvgBuyPrice());
				break;
			case 7:  // avg sell size
				ret = Integer.toString(trade.getTotalSellQty());
				break;
			case 8:  // avg sell price
				ret = Double.toString(trade.getAvgSellPrice());
				break;
			case 9:  // Gross P&L
				ret = Double.toString(trade.getRealizedPnL());
				break;
			case 10: // Entry Text
				trade.getExecutions().get(0).getOrder().getText();
				break;
			case 11: // Exit Text
				trade.getExecutions().get(trade.getExecutions().size()-1).getOrder().getText();
				break;
			}
			return ret;
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
	

	@Override
	public void setFocus() {
		tableViewer.getControl().setFocus();
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		tableViewer.addSelectionChangedListener(listener);
	}

	public void addSelectionListener(ISelectionChangedListener listener) {
		tableViewer.addSelectionChangedListener(listener);
	}

	public ISelection getSelection() {
		return tableViewer.getSelection();
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		tableViewer.removeSelectionChangedListener(listener);
	}

	public void removeSelectionListener(ISelectionChangedListener listener) {
		tableViewer.removeSelectionChangedListener(listener);
	}

	public void setSelection(ISelection selection) {
	}

}
