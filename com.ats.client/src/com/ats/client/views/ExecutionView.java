package com.ats.client.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.ats.engine.JExecution;
import com.ats.platform.Position;
import com.ats.utils.Utils;

public class ExecutionView extends ViewPart implements ISelectionProvider {
	public static final String ID = "com.ats.client.views.executionView";

	private TableViewer tableViewer;
	
	private List<JExecution> executions = new ArrayList<JExecution>();

	// TODO: want to do this automatically
	private Action refreshAction;
	
	private ISelectionListener listener;
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		
		listener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				if( part == ExecutionView.this || !( selection instanceof IStructuredSelection) ) {
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
	
	private void createActions() {
	}

	@Override
	public void createPartControl(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
        content.setLayout(gridLayout);
        
        tableViewer = new TableViewer(content, SWT.SINGLE );
        tableViewer.setContentProvider(new ExecContentProvider());
        tableViewer.setLabelProvider(new ExecLabelProvider());
        tableViewer.setInput(executions);
        
        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText("Contract");
        column.setWidth(50);
        
        column = new TableColumn(table, SWT.NONE);
        column.setText("Date/Time");
        column.setWidth(90);
        
        column = new TableColumn(table, SWT.NONE);
        column.setText("Side");
        column.setWidth(50);
        
        column = new TableColumn(table, SWT.NONE);
        column.setText("Size");
        column.setWidth(50);
        
        column = new TableColumn(table, SWT.NONE);
        column.setText("Avg Price");
        column.setWidth(50);
        
        column = new TableColumn(table, SWT.NONE);
        column.setText("Text");
        column.setWidth(100);
        
        createActions();
	}
	
	private void setPosition(final Position pos) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				executions.clear();
				if( pos != null ) {
					executions.addAll(pos.getExecutions());
				}
				tableViewer.refresh();
			}
		});

	}
	
	class ExecContentProvider implements IStructuredContentProvider {
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public Object[] getElements( Object input ) {
			return ((List)input).toArray();
		}
	}
	
	class ExecLabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			JExecution exec = (JExecution)element;
			String ret = null;
			switch(columnIndex) {
			case 0:
				ret = exec.getInstrument().getSymbol();
				break;
			case 1:
				ret = Utils.timeAndDateFormat.format(exec.getDateTime());
				break;
			case 2:
				ret = exec.getSide().toString();
				break;
			case 3:
				ret = Integer.toString(exec.getQuantity());
				break;
			case 4:
				ret = Double.toString(exec.getPrice());
				break;
			case 5:
				ret = exec.getOrder().getText();
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
