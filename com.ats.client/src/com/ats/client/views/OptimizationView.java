package com.ats.client.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.IntegerGene;
import org.jgap.impl.NumberGene;

import com.ats.client.dialogs.VisualizeDialog;
import com.ats.client.tools.BacktestFitnessFunction;
import com.ats.client.tools.ParamValuesChromosome;
import com.ats.client.wizards.OptimizeWizard;
import com.ats.client.wizards.ParamValues;
import com.ats.engine.PositionManager;
import com.ats.engine.StrategyDefinition;
import com.ats.engine.backtesting.BacktestFactory;
import com.ats.engine.backtesting.BacktestListener;
import com.ats.utils.StrategyAnalyzer;
import com.ats.utils.TradeStats;
import com.ats.utils.Utils;

public class OptimizationView extends ViewPart {
	private static final Logger logger = Logger.getLogger(OptimizationView.class);
	
	public static final String ID = "com.ats.client.views.optimizationView";

	/**
	 * A fun little hack for displaying the columns but dodging the SWT requirement to
	 * use Strings.  This lets you use a switch() block in the labels and dynamically
	 * re-assign the columns at runtime.
	 */
	enum ColType {
		netProfit("Net Profit"),
		commissions("Commissions"),
		trades("Trades"),
		maxDrawdown("Max Drawdown"),
		winners("#Winners"),
		losers("#Losers"),
		avgTrade("Avg Trade"),
		param("Param");
		
		String name;
		private ColType(String name) {
			this.name=name;
		}
	}
	
	private TableViewer viewer;
	private List<ParamValues> paramValues;
	private StrategyDefinition stratDef;
	private List<OptTrial> trials = new ArrayList<OptTrial>();
	private int maxTrials;
	private Action exportAction;
	private Action visualizeAction;
	
	public OptimizationView() {
		super();
	}
	
	private void createActions() {

		exportAction = new Action() {
			public void run() {
				// TODO: complete this functionality
			}
		};
		exportAction.setText("Export...");

		visualizeAction = new Action() {
			public void run() {
				VisualizeDialog dlg = new VisualizeDialog(viewer.getTable().getShell());
				dlg.setTrials(trials);
				//TODO it works only for macross strategy.
				dlg.setXparam("Fast Period");
				dlg.setYparam("Slow Period");
				dlg.open();
			}
		};
		visualizeAction.setText("Visualize...");

        MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager menuManager)
            {
            	menuManager.add(visualizeAction);
            	menuManager.add(exportAction);
            }
        });
        viewer.getTable().setMenu(menuMgr.createContextMenu(viewer.getTable()));

//		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
//		mgr.add(exportAction);
	}
	
	@Override
	public void createPartControl(Composite parent) {

		Composite content = new Composite(parent, SWT.NONE);

		GridLayout contentLayout = new GridLayout();
		contentLayout.numColumns = 2;
		content.setLayout(contentLayout);
		
		viewer = new TableViewer(content, SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION );
        viewer.setContentProvider(new OptExecContentProvider());
        viewer.setLabelProvider(new OptExecLabelProvider());
        viewer.setInput(trials);
        viewer.setSorter(new OptSorter());
        viewer.addDoubleClickListener(new IDoubleClickListener() {
			
			public void doubleClick(DoubleClickEvent event) {
				//TODO open stats for given simulation
			}
		});
        
        Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        GridData gdata = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gdata.heightHint = 350;
        gdata.widthHint = 350;
        table.setLayoutData(gdata);
        
        for(ColType colType : ColType.values() ) {
        	if( colType == ColType.param ) {
        		// special case
        		continue;
        	}
            final TableColumn column = new TableColumn(table, SWT.LEFT);
            column.setText(colType.name);
            column.setWidth(50);
            viewer.setData(colType.name, colType);
            column.addSelectionListener(new SelectionAdapter() {
            	public void widgetSelected(SelectionEvent evt) {
            		((OptSorter)viewer.getSorter()).doSort(column);
            		viewer.refresh();
            	}
            });
        }

        createActions();
		launchOptimizer();
	}

	@Override
	public void setFocus() {
	}
	
	
	public void setStrategyDefinition(StrategyDefinition stratDef, List<ParamValues> values) {
		this.stratDef = stratDef;
		this.paramValues = values;
		
		maxTrials = 1;
		for(ParamValues val : values) {
			maxTrials *= val.numTrials;
		}
		trials = new ArrayList<OptTrial>(maxTrials);
		viewer.setInput(trials);
		
		for( ParamValues val : paramValues ) {
	        TableColumn column = new TableColumn(viewer.getTable(), SWT.LEFT);
	        column.setText(val.paramName);
	        column.setWidth(50);
            viewer.setData(val.paramName, ColType.param);
		}
		
	}
	
	private void launchOptimizer() {
		final OptimizeWizard wiz = new OptimizeWizard();
		WizardDialog dlg = new WizardDialog(getSite().getShell(), wiz);
		if( dlg.open() == WizardDialog.OK ) {
			setStrategyDefinition(wiz.getStrategyDefinition(), wiz.getParamValues());
			
			Job job = new Job("Run optimization") {
				@Override
				protected IStatus run(final IProgressMonitor monitor) {
							if( wiz.isBruteForce() ) {
								runBruteForce(monitor);
							} else {
								runGeneticAlgorithm(monitor, wiz.getNumGATrials(), wiz.getNumOrganismsPerIter(), wiz.getNetProfitOffset());
							}
					return Status.OK_STATUS;
				}
			};
			// Start the Job
			job.setUser(true);
			job.schedule();
			
			/*Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					try {
						new ProgressMonitorDialog(getSite().getShell()).run(true, true,
								new IRunnableWithProgress() {
									public void run(IProgressMonitor monitor) {
										if( wiz.isBruteForce() ) {
											runBruteForce(monitor);
										} else {
											runGeneticAlgorithm(monitor, wiz.getNumGATrials(), wiz.getNumOrganismsPerIter(), wiz.getNetProfitOffset());
										}
									}
						});
					} catch( Exception e ) {
					}
				}
			});*/
		}

	}
	public void addOptimizationTrial(final Map<String, Number> params, TradeStats stats) {
		final OptTrial trial = new OptTrial();
		trial.stats = stats;
		trial.paramVals = params;
		trials.add(trial);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				viewer.add(trial);
			}
		});
	}

	private void runGeneticAlgorithm(IProgressMonitor monitor, int numGATrials, int numOrgsPerIter, double netProfitOffset) {
		monitor.beginTask("Optimizing...", numGATrials);

		Configuration.reset();
		Configuration gaConf = new DefaultConfiguration();
		gaConf.setPreservFittestIndividual(true);
		gaConf.setKeepPopulationSizeConstant(false);
		Genotype genotype = null;

		try {
			// the genes represent the number of steps away from the starting position.
			// For simplicity, we start in the middle
			Gene genes[] = new Gene[this.paramValues.size()];
			for( int i = 0; i < genes.length; i++ ) {
				ParamValues vals = paramValues.get(i);
				genes[i] = new IntegerGene(gaConf, 0, vals.numTrials );
				((NumberGene)genes[i]).setAllele(vals.numTrials / 2);
			}
			ParamValuesChromosome chromo = new ParamValuesChromosome(
					gaConf, genes, stratDef, paramValues);
			
			gaConf.setSampleChromosome(chromo);
			gaConf.setPopulationSize(numOrgsPerIter);
			gaConf.setFitnessFunction(new BacktestFitnessFunction(this, stratDef, paramValues, netProfitOffset));
			genotype = Genotype.randomInitialGenotype(gaConf);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < numGATrials; i++) {
			try {
				monitor.setTaskName("Trial " + (i+1) + " of " + numGATrials + "...");
				genotype.evolve();
			} catch( Exception e) {
				e.printStackTrace();
			}
			monitor.worked(1);
			if (monitor.isCanceled()) {
				break;
			}
		}
		// Print summary.
		// --------------
		IChromosome fittest = genotype.getFittestChromosome();
//		System.out.println("Fittest Chromosome has fitness "
//				+ fittest.getFitnessValue());

	}

	private void runBruteForce(IProgressMonitor monitor) {

		monitor.beginTask("Optimizing...", trials.size());
		runStrategy(monitor);
		while (true) {
			try {
				// unsophisticated means of monitoring, I know. I'm
				// tired and this works.
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			if (monitor.isCanceled() || trials.size() >= maxTrials) {
				break;
			}
		}
	}

	private synchronized void runStrategy(final IProgressMonitor monitor) {
		if( trials.size() >= maxTrials ) {
			monitor.done();
			return;
		}
		if( monitor.isCanceled() ) {
			return;
		}
		
		int currTrial = trials.size();
		logger.debug("Running optimizer trial #" + currTrial);
		monitor.setTaskName("Trial " + (currTrial+1) + " of " + maxTrials + "...");
		
		// need to translate the number of trials to a specific permutation
		// of an arbitrarily large number of parameters.
		//
		// emulate a bit set, or a base(m,n) number
		// for param A...Z  with values A1...AN
		// currTrial = A + AN*B + AN*BN*C + ...
		// so A = size % AN
		//    B = (size / AN) % BN
		//    ...
		
		final Map<String, Number> params = new HashMap<String, Number>();
		for(ParamValues vals : paramValues) {
			int delta = currTrial % vals.numTrials;
			if( vals.initVal instanceof Integer ) {
				int val = vals.start.intValue() + (delta * vals.stepSize.intValue());
				params.put(vals.paramName, val);
			} else {
				// double
				double val = vals.start.doubleValue() + (delta * vals.stepSize.doubleValue());
				params.put(vals.paramName, val);
			}
			stratDef.setParameter(vals.paramName, params.get(vals.paramName));
			currTrial = (currTrial - delta)/vals.numTrials;
		}
		logger.debug("Running with params = " + params);
		
		new BacktestFactory().runBacktest(stratDef, new BacktestListener(){
			public void testComplete() {
				monitor.worked(1);
				TradeStats stats = StrategyAnalyzer.calculateTradeStats(PositionManager.getInstance().getAllTrades());
				addOptimizationTrial(params, stats);
				runStrategy(monitor);
			}

		});
	}

	class OptSorter extends  ViewerSorter {
		  private TableColumn column;
		  private boolean isAscending;
		  private ColType type = ColType.netProfit;
		  
		  /**
		   * Does the sort. If it's a different column from the previous sort, do an
		   * ascending sort. If it's the same column as the last sort, toggle the sort
		   * direction.
		   * 
		   * @param column
		   */
		  public void doSort(TableColumn column) {
			if (column == this.column) {
				// Same column as last sort; toggle the direction
				isAscending = !isAscending;
			} else {
				// New column; do an ascending sort
				this.column = column;
				isAscending = true;
			}
			String colText = column.getText();
			type = (ColType) viewer.getData(colText);
		}

		  /**
			 * Compares the object for sorting
			 */
		  public int compare(Viewer viewer, Object o1, Object o2) {
			OptTrial t1 = (OptTrial)o1;
			OptTrial t2 = (OptTrial)o2;


			// Determine which column and do the appropriate sort
			int rc = 0;
			switch(type) {
			case netProfit:
				rc = (int)(t1.stats.getTotalNet() - t2.stats.getTotalNet());
				break;
			case avgTrade:
				rc = (int)(t1.stats.getAvgTrade() - t2.stats.getAvgTrade());
				break;
			case maxDrawdown:
				rc = (int)(t1.stats.maxDrawdown - t2.stats.maxDrawdown);
				break;
			}

			if (!isAscending) {
				rc = -rc;
			}

			return rc;
		}
	}
	
	class OptExecLabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			OptTrial trial = (OptTrial)element;
			
			String colText = viewer.getTable().getColumn(columnIndex).getText();
			ColType type = (ColType)viewer.getData(colText);
			
			String ret = null;
			switch(type) {
			case avgTrade:
				ret = "";
				break;
			case commissions:
				//ret = "" + trial.stats.commissions;
				break;
			case losers:
				ret = "" + trial.stats.numLosers;
				break;
			case winners:
				ret = "" + trial.stats.numWinners;
				break;
			case netProfit:
				ret = Utils.currencyForm.format(trial.stats.getTotalNet());
				break;
			case trades:
				ret = "" + trial.stats.numTrades;;
				break;
			case maxDrawdown:
				ret = Utils.currencyForm.format(trial.stats.maxDrawdown);
				break;
			case param:
				try {
					ret = trial.paramVals.get(colText).toString();
				} catch( Exception e) {
					ret = "";
				}
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

}

class OptExecContentProvider implements IStructuredContentProvider {
	public Object[] getElements(Object inputElement) {
		return ((List<?>)inputElement).toArray();
	}
	public void dispose() {
	}
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}

