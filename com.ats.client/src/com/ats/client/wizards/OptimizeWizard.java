package com.ats.client.wizards;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;

import com.ats.db.PlatformDAO;
import com.ats.engine.StrategyDefinition;

public class OptimizeWizard extends Wizard {
	private static final Logger logger = Logger.getLogger(OptimizeWizard.class);
	
	private SelectStrategyPage selectStrategyPage;
	private SelectParamsPage selectParamsPage;
	private ParamValuesPage paramValuesPage;
	private SelectOptimizerPage selectOptimizerPage;
	
	private StrategyDefinition stratDef;
	private java.util.List<ParamValues> values;
	
	public OptimizeWizard() {
		setWindowTitle("Optimize Strategy");
		setNeedsProgressMonitor(true);
		
		selectStrategyPage = new SelectStrategyPage();
		selectParamsPage = new SelectParamsPage();
		paramValuesPage = new ParamValuesPage();
		selectOptimizerPage = new SelectOptimizerPage();
	}

	@Override
	public void addPages() {
		addPage(selectStrategyPage);
		addPage(selectParamsPage);
		addPage(paramValuesPage);
		addPage(selectOptimizerPage);
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
		}
		return super.getNextPage(page);
	}


	@Override
	public boolean performFinish() {
		values = paramValuesPage.getParamValues();
		return true;
	}
	
	public java.util.List<ParamValues> getParamValues() {
		return values;
	}
	public StrategyDefinition getStrategyDefinition() {
		return stratDef;
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
		new Label(content, SWT.NONE).setText("     Will exhaustively try all alternatives");
		new Label(content, SWT.NONE).setText("     Recommended for a small number of trials.");

		geneticAlgBtn = new Button(content, SWT.RADIO);
		geneticAlgBtn.setText("Genetic Algorithm.");
		geneticAlgBtn.setEnabled(false);
		GridData gdata = new GridData();
		gdata.verticalIndent = 15;
		geneticAlgBtn.setLayoutData(gdata);
		new Label(content, SWT.NONE).setText("     Will search options looking for 'good enough' combinations");
		new Label(content, SWT.NONE).setText("     Recommended for a large number of trials.");
		new Label(content, SWT.NONE).setText("     Not currently supported.");

		setControl(content);
	}
}

