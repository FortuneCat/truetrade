package com.ats.client.wizards;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;

import com.ats.db.PlatformDAO;
import com.ats.engine.StrategyDefinition;

public class OptimizeWizard extends Wizard {
	private static final Logger logger = Logger.getLogger(OptimizeWizard.class);
	
	private SelectStrategyPage selectStrategyPage;
	private SelectParamsPage selectParamsPage;
	private ParamValuesPage paramValuesPage;
	private SelectOptimizerPage selectOptimizerPage;
	private GAParamsPage gaParamsPage;
	
	private StrategyDefinition stratDef;
	private java.util.List<ParamValues> values;
	
	private boolean isBruteForce;
	private int numGATrials;
	private int numOrganismsPerIter;
	private double netProfitOffset;
	
	public OptimizeWizard() {
		setWindowTitle("Optimize Strategy");
		setNeedsProgressMonitor(true);
		
		selectStrategyPage = new SelectStrategyPage();
		selectParamsPage = new SelectParamsPage();
		paramValuesPage = new ParamValuesPage();
		selectOptimizerPage = new SelectOptimizerPage();
		gaParamsPage = new GAParamsPage();
	}

	@Override
	public void addPages() {
		addPage(selectStrategyPage);
		addPage(selectParamsPage);
		addPage(paramValuesPage);
		addPage(selectOptimizerPage);
		addPage(gaParamsPage);
	}
	
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if( page instanceof SelectStrategyPage ) {
			stratDef = selectStrategyPage.getStrategyDefinition();
			selectParamsPage.setStrategyDefinition(stratDef);
		} else if( page instanceof SelectParamsPage ) {
			paramValuesPage.setStratDefParams(selectStrategyPage
					.getStrategyDefinition(), selectParamsPage
					.getSelectedParamNames());
		} else if( page instanceof ParamValuesPage ) {
			int numTrials = paramValuesPage.getNumTrials();
			gaParamsPage.setNumTrials(numTrials);
		} else if( page instanceof SelectOptimizerPage ) {
			if( selectOptimizerPage.isBruteForce() ) {
				return null;
			} else {
				return gaParamsPage;
			}
		}
		return super.getNextPage(page);
	}


	@Override
	public boolean performFinish() {
		values = paramValuesPage.getParamValues();
		isBruteForce = selectOptimizerPage.isBruteForce();
		numGATrials = gaParamsPage.getNumGATrials();
		numOrganismsPerIter = gaParamsPage.getNumOrganismsPerIter();
		netProfitOffset = gaParamsPage.getProfitOffset();
		return true;
	}
	
	public java.util.List<ParamValues> getParamValues() {
		return values;
	}
	public StrategyDefinition getStrategyDefinition() {
		return stratDef;
	}
	public boolean isBruteForce() {
		return isBruteForce;
	}
	public int getNumGATrials() {
		return numGATrials;
	}

	public double getNetProfitOffset() {
		return netProfitOffset;
	}

	public int getNumOrganismsPerIter() {
		return numOrganismsPerIter;
	}
	
}

class SelectStrategyPage extends WizardPage {

	private List stratList;
	
	public SelectStrategyPage() {
		super("Select Strategy");
		setTitle("Select Strategy");
		setDescription("Select the strategy to optimize.");
		setPageComplete(false);
	}

	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NULL);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		content.setLayout(layout);
		
		stratList = new List(content, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);
		GridData gdata = new GridData();
		gdata.grabExcessVerticalSpace = true;
		gdata.verticalAlignment = GridData.FILL;
		stratList.setLayoutData(gdata);
		stratList.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				setPageComplete(stratList.getSelectionCount() > 0);
			}
		});
		
		for(StrategyDefinition stratDef : PlatformDAO.getAllStrategyDefinitions() ) {
			if( stratDef.getParameterNames().size() > 0 ) {
				String name = stratDef.getStrategyClass().getSimpleName();
				stratList.add(name);
				stratList.setData(name, stratDef);
			}
		}
		
		setControl(content);
	}
	
	public StrategyDefinition getStrategyDefinition() {
		String names[] = stratList.getSelection();
		if( names.length <= 0 ) {
			return null;
		}
		return (StrategyDefinition)stratList.getData(names[0]);
	}
}

class SelectParamsPage extends WizardPage {

	private List selParamList;
	private List availParamList;
	private StrategyDefinition defn;
	
	public SelectParamsPage() {
		super("Select Parameters");
		setTitle("Select Parameters");
		setDescription("Select the parameters which will be optimized.");
		setPageComplete(false);
	}
	
	public void setStrategyDefinition(final StrategyDefinition stratDef) {
		if( stratDef.equals(defn)) {
			return;
		}
		defn = stratDef;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				availParamList.removeAll();
				selParamList.removeAll();
				setPageComplete(false);
				for(String name : defn.getParameterNames()) {
					availParamList.add(name);
				}
			}
		});
	}
	
	public String[] getSelectedParamNames() {
		return selParamList.getItems();
	}

	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NULL);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		content.setLayout(layout);
		
		availParamList = new List(content, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);
		GridData gdata = new GridData();
		gdata.grabExcessVerticalSpace = true;
		gdata.verticalAlignment = GridData.FILL;
		gdata.grabExcessHorizontalSpace = true;
		gdata.horizontalAlignment = GridData.FILL;
		gdata.verticalSpan = 4;
		availParamList.setLayoutData(gdata);
		
		Button btn = new Button(content, SWT.PUSH);
		btn.setText(">");
		gdata = new GridData();
		gdata.horizontalAlignment = GridData.FILL;
		gdata.verticalAlignment = GridData.END;
		btn.setLayoutData(gdata);
		btn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				String sels[] = availParamList.getSelection();
				if( sels != null && sels.length > 0 ) {
					final String sel = sels[0];
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							selParamList.add(sel);
							availParamList.remove(sel);
							setPageComplete(selParamList.getItemCount() > 0);
						}
					});
				}
			}
		});
		
		selParamList = new List(content, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);
		gdata = new GridData();
		gdata.grabExcessVerticalSpace = true;
		gdata.verticalAlignment = GridData.FILL;
		gdata.grabExcessHorizontalSpace = true;
		gdata.horizontalAlignment = GridData.FILL;
		gdata.verticalSpan = 4;
		selParamList.setLayoutData(gdata);
		
		
		btn = new Button(content, SWT.PUSH);
		btn.setText(">>");
		gdata = new GridData();
		gdata.horizontalAlignment = GridData.FILL;
		gdata.verticalAlignment = GridData.CENTER;
		btn.setLayoutData(gdata);
		btn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						String items[] = availParamList.getItems();
						for( String sel : items ) {
							selParamList.add(sel);
							availParamList.remove(sel);
						}
						setPageComplete(selParamList.getItemCount() > 0);
					}
				});
			}
		});
		
		btn = new Button(content, SWT.PUSH);
		btn.setText("<");
		gdata = new GridData();
		gdata.horizontalAlignment = GridData.FILL;
		gdata.verticalAlignment = GridData.CENTER;
		btn.setLayoutData(gdata);
		btn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				String sels[] = selParamList.getSelection();
				if( sels != null && sels.length > 0 ) {
					final String sel = sels[0];
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							availParamList.add(sel);
							selParamList.remove(sel);
							setPageComplete(selParamList.getItemCount() > 0);
						}
					});
				}
			}
		});
		
		btn = new Button(content, SWT.PUSH);
		btn.setText("<<");
		gdata = new GridData();
		gdata.horizontalAlignment = GridData.FILL;
		gdata.verticalAlignment = GridData.BEGINNING;
		btn.setLayoutData(gdata);
		btn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						String items[] = selParamList.getItems();
						for( String sel : items ) {
							availParamList.add(sel);
							selParamList.remove(sel);
						}
						setPageComplete(selParamList.getItemCount() > 0);
					}
				});
			}
		});
		
		setControl(content);
	}
}
class SelectOptimizerPage extends WizardPage {

	private Button bruteForceBtn;
	private Button geneticAlgBtn; 
	
//	private int numTrials = 100;
		
	public SelectOptimizerPage() {
		super("Select Optimizer Method");
		setTitle("Select Optimizer Method");
		setDescription("Select the technique used by the optimizer.");
		setPageComplete(true);
	}
	
	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NULL);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		content.setLayout(layout);
		
		bruteForceBtn = new Button(content, SWT.RADIO);
		bruteForceBtn.setText("Brue Force.");
		bruteForceBtn.setSelection(true);
		Text text = new Text(content, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
		text.setText("     Will exhaustively try all alternatives.\n"
				   + "     Recommended for a small number of trials.");
		bruteForceBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				setPageComplete(true);
			}
		});

		geneticAlgBtn = new Button(content, SWT.RADIO);
		geneticAlgBtn.setText("Genetic Algorithm.");
		GridData gdata = new GridData();
		gdata.verticalIndent = 15;
		geneticAlgBtn.setLayoutData(gdata);
		text = new Text(content, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
		text.setText("     Will search options looking for 'good enough' combinations.\n"
				   + "     Recommended only for a large number of trials.");
		geneticAlgBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				setPageComplete(true);
			}
		});
		
		setControl(content);
	}
	
	public boolean isBruteForce() {
		return bruteForceBtn.getSelection();
	}
}

class GAParamsPage extends WizardPage {

	private Scale gaScale;
	private Text numTrialsText;
	private Text numIterationsText;
	private Text profitOffsetText;
	
	public GAParamsPage() {
		super("Configure Genetic Analysis");
		setTitle("Configure Genetic Analysis");
		setDescription("Select the genetic analysis parameters.");
		setPageComplete(true);
	}

	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NULL);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		content.setLayout(layout);
		
		Label label = new Label(content, SWT.NONE);
		label.setText("Select the number of iterations for the genetic algorithm");
		GridData gdata = new GridData();
		gdata.verticalIndent = 10;
		label.setLayoutData(gdata);

		gaScale = new Scale(content, SWT.NONE);
		gaScale.setMinimum(1);
		gaScale.setMaximum(100);
		gdata = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
		gaScale.setLayoutData(gdata);
		numTrialsText = new Text(content, SWT.NONE);
		numTrialsText.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
		numTrialsText.setEditable(false);
		gaScale.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				numTrialsText.setText(Integer.toString(gaScale.getSelection()));
			}
			
		});
		
		label = new Label(content, SWT.NONE);
		label.setText("Select the number of 'organisms' per iteration:");
		gdata = new GridData();
		gdata.verticalIndent = 10;
		label.setLayoutData(gdata);
		numIterationsText = new Text(content, SWT.BORDER);
		numIterationsText.setText("10        ");
		
		Text text = new Text(content, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
		text.setText("Select the net profit offset:\n"
				   + "  The fitness function only works on positive net results\n"
				   + "  and works best when there is a significant percentage difference\n"
				   + "  between the results.  The offset will be added to net profit.\n"
				   + "  (Use a negative number to decrease the net profit.)");
		gdata = new GridData();
		gdata.verticalIndent = 10;
		text.setLayoutData(gdata);
		profitOffsetText = new Text(content, SWT.BORDER);
		profitOffsetText.setText("0.00           ");
		

		setControl(content);
	}
	public void setNumTrials(int numTrials) {
		gaScale.setMaximum(numTrials);
		gaScale.setSelection(numTrials/4);
		numTrialsText.setText("" + gaScale.getSelection());
	}

	public int getNumGATrials() {
		return gaScale.getSelection();
	}
	public int getNumOrganismsPerIter() {
		try {
			return Integer.parseInt(numIterationsText.getText());
		} catch( Exception e) {
			return 10;
		}
	}
	public double getProfitOffset() {
		try {
			return Double.parseDouble(profitOffsetText.getText());
		} catch( Exception e) {
			return 0.0;
		}
	}
	
}
