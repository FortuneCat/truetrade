package com.ats.client.views;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.ats.client.wizards.DownloadHistDataWizard;
import com.ats.client.wizards.ImportDataWizard;
import com.ats.db.PlatformDAO;
import com.ats.engine.ImportDataManager;
import com.ats.platform.Bar;
import com.ats.platform.BarSeries;
import com.ats.platform.Instrument;
import com.ats.platform.TimeSpan;
import com.ats.utils.Utils;

public class AvailableDataView extends ViewPart {
	private static final int MAX_DETAIL_ROWS = 10;

	public static final String ID = "com.ats.client.views.availableDataView";

	private Action importDataAction;
	private Action downloadDataAction;
	
	private Table seriesTable;
	private Table detailTable;
	
	private ISelectionListener listener;
	private Instrument instrument;

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		createActions();
		
		listener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				if( part == AvailableDataView.this || !( selection instanceof IStructuredSelection) ) {
					// don't listen to my own or unstructured messages
					return;
				}
				IStructuredSelection sel = (IStructuredSelection)selection;
				Object obj = sel.getFirstElement();
				if( obj == null ) {
					setInstrument((Instrument)obj);
				}
				if( obj instanceof TreeObject ) {
					obj = ((TreeObject)obj).getObject();
				}
				if( obj instanceof InstrumentPropertySource ) {
					setInstrument(((InstrumentPropertySource)obj).getInstrument());
				} else if( obj instanceof Instrument ) { 
					setInstrument((Instrument)obj);
				} else {
					setInstrument((Instrument)null);
				}
			}
		};
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(InstrumentView.ID, listener );
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(StrategyView.ID, listener );
	}

	private void createActions() {
		importDataAction = new Action() {
			public void run() {
				WizardDialog dlg = new WizardDialog(seriesTable.getShell(), new ImportDataWizard());
				dlg.open();
			}
		};
		importDataAction.setText("Import data...");
		
		downloadDataAction = new Action() {
			public void run() {
				WizardDialog dlg = new WizardDialog(seriesTable.getShell(), new DownloadHistDataWizard());
				dlg.open();
			}
		};
		downloadDataAction.setText("Download historical data...");
		
		
		MenuManager dataMenuManager = new MenuManager("&Data");
		dataMenuManager.add(importDataAction);
		dataMenuManager.add(downloadDataAction);
		
		
		Menu menuBar = getSite().getShell().getMenuBar();
		dataMenuManager.fill(menuBar, -1);
		getSite().getShell().setMenuBar(menuBar);
		
	}
	
	@Override
	public void dispose() {
		super.dispose();
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(listener);
	}

	private void setInstrument(Instrument instrument) {
		this.instrument = instrument;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if( AvailableDataView.this.instrument == null ) {
					setPartName("Data");
					seriesTable.removeAll();
					detailTable.removeAll();
				} else {
					setPartName("Data: " + AvailableDataView.this.instrument.getSymbol());
					seriesTable.removeAll();
					detailTable.removeAll();
					List<Map<String, Object>> seriesData = PlatformDAO.getSeriesOverview(AvailableDataView.this.instrument);
					for(Map<String, Object> curr : seriesData) {
						TableItem item = new TableItem(seriesTable, SWT.NONE);
						item.setData(curr.get("seriesId"));
						item.setText(new String[]{
								TimeSpan.values()[(Integer)curr.get("timespanId")].name(),
								curr.get("barCount").toString(),
								Utils.timeAndDateFormat.format((Date)curr.get("beginTime")),
								Utils.timeAndDateFormat.format((Date)curr.get("endTime"))
							});

					}
				}
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
        
        seriesTable = new Table(content, SWT.MULTI | SWT.FULL_SELECTION);
        seriesTable.setHeaderVisible(true);
        seriesTable.setLinesVisible(true);
        seriesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager menuManager)
            {
            	menuManager.add(importDataAction);
            	menuManager.add(downloadDataAction);
            }
        });
        seriesTable.setMenu(menuMgr.createContextMenu(seriesTable));
        seriesTable.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				detailTable.removeAll();
				return;
			}
			public void widgetSelected(SelectionEvent e) {
				detailTable.removeAll();
				if( e.item == null ) {
					return;
				}
				int seriesId = (Integer)((TableItem)e.item).getData();
				List<Bar> bars = PlatformDAO.getSampleBarsForSeries(seriesId);
				for( Bar bar : bars ) {
					TableItem item = new TableItem(detailTable, SWT.NONE);
					NumberFormat nf = instrument.isForex() ? Utils.quadDecForm : Utils.doubleDecForm;
					item.setText(new String[]{
							Utils.timeAndDateFormat.format(bar.getBeginTime()),
							nf.format(bar.getHigh()),
							nf.format(bar.getLow()),
							nf.format(bar.getOpen()),
							nf.format(bar.getClose()),
							Utils.thousandsForm.format(bar.getVolume())
							});
				}
				TableItem item = new TableItem(detailTable, SWT.NONE);
				item.setText(new String[]{
						"...", "...", "...", "...", "...", "..."});
			}
        });


        TableColumn column = new TableColumn(seriesTable, SWT.NONE);
        column.setText("Data Series");
        column.setWidth(60);
        
        column = new TableColumn(seriesTable, SWT.NONE);
        column.setText("Object Count");
        column.setWidth(90);
        
        column = new TableColumn(seriesTable, SWT.NONE);
        column.setText("First date/time");
        column.setWidth(90);
        
        column = new TableColumn(seriesTable, SWT.NONE);
        column.setText("Last date/time");
        column.setWidth(90);
        
//        TableItem item = new TableItem(seriesTable, SWT.NONE);
//        item.setText(new String[]{"Daily", "2,019", "2 Jan 2007", "13 Mar 2007"});
        

        detailTable = new Table(content, SWT.MULTI | SWT.FULL_SELECTION);
        detailTable.setHeaderVisible(true);
        detailTable.setLinesVisible(true);
        detailTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        column = new TableColumn(detailTable, SWT.NONE);
        column.setText("Date/Time");
        column.setWidth(60);
        
        column = new TableColumn(detailTable, SWT.NONE);
        column.setText("High");
        column.setWidth(50);
        
        column = new TableColumn(detailTable, SWT.NONE);
        column.setText("Low");
        column.setWidth(50);
        
        column = new TableColumn(detailTable, SWT.NONE);
        column.setText("Open");
        column.setWidth(50);
        
        column = new TableColumn(detailTable, SWT.NONE);
        column.setText("Close");
        column.setWidth(50);
        
        column = new TableColumn(detailTable, SWT.NONE);
        column.setText("Volume");
        column.setWidth(60);
        
//        item = new TableItem(detailTable, SWT.NONE);
//        item.setText(new String[]{"13/Mar/2007 06:30", "19.27", "18.88", "18.96", "19.04", "280,100"});

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
