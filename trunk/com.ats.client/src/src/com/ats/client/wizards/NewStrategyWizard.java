package com.ats.client.wizards;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.ats.engine.StrategyDefinition;
import com.ats.platform.Strategy;

public class NewStrategyWizard extends Wizard {
	private static final Logger logger = Logger.getLogger(NewStrategyWizard.class);
	
	private ClassLocationPage classLocPage;
	private ClassNamePage classNamePage;
	private StrategyParametersPage stratParamPage;
	
	private StrategyDefinition stratDef;
	
	public NewStrategyWizard() {
		setWindowTitle("Create Strategy");
		setNeedsProgressMonitor(false);
		
		classNamePage = new ClassNamePage();
		classLocPage = new ClassLocationPage();
		stratParamPage = new StrategyParametersPage();
	}
	
	public StrategyDefinition getStrategyDefinition() {
		return stratDef;
	}
	
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if( page instanceof ClassNamePage ) {
			String className = ((ClassNamePage)page).getClassName();
			if( stratDef == null || stratDef.getStrategyClass() == null ||
							! stratDef.getStrategyClass().getName().equals(className)) {
				// replace strategy
				try {
					stratDef = new StrategyDefinition();
					stratDef.setStrategyClassName(className);
					stratParamPage.setStrategyDefinition(stratDef);
				} catch( Exception e) {
					// should not happen, the class name page should filter this out
				}
			}
		}
		return super.getNextPage(page);
	}
	
	@Override
	public void addPages() {
		addPage(classNamePage);
		addPage(classLocPage);
		addPage(stratParamPage);
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return true;
	}
	
}


class ClassNamePage extends WizardPage {
	
	private Text nameText;
	
	
	public ClassNamePage() {
		super("ClassName");
		setTitle("Select class name");
		setDescription("Select the name of the strategy class");
		setPageComplete(true);
	}
	
	public String getClassName() {
		return nameText.getText();
	}
	
	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NULL);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		content.setLayout(layout);
		
		new Label(content, SWT.NONE).setText("Enter the strategy class name:");

		nameText = new Text(content, SWT.BORDER);
		GridData gdata = new GridData();
		gdata.horizontalAlignment = GridData.FILL;
		gdata.grabExcessHorizontalSpace = true;
		nameText.setLayoutData(gdata);
		nameText.setText("");
		
		setControl(content);
	}
}

class StratParam {
	public String param;
	public Number value;
}

class StrategyParametersPage extends WizardPage {
	
	private TableViewer tableViewer;
	
	private StrategyDefinition stratDef;
	
	public StrategyParametersPage() {
		super("StrategyParameters");
		setTitle("Configure Parameters");
		setDescription("Configure the optional parameters of the strategy");
		setPageComplete(true);
	}
	
	public void setStrategyDefinition(StrategyDefinition stratDef) {
		this.stratDef = stratDef;
		
		if( tableViewer != null && !tableViewer.getTable().isDisposed()) {
			tableViewer.setInput(stratDef);
		}
	}
	
	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NULL);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		content.setLayout(layout);

		tableViewer = new TableViewer(content, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		attachContentProvider();
		attachLabelProvider();
		attachCellEditors();
		
		GridData gdata = new GridData();
		gdata.horizontalAlignment = GridData.FILL;
		gdata.heightHint = 150;
		
		Table table = tableViewer.getTable();
		table.setLayoutData(gdata);
		
		table.setHeaderVisible(true);
		TableColumn col = new TableColumn(table, SWT.LEFT);
		col.setText("Parameter");
		col.setWidth(150);
		
		col = new TableColumn(table, SWT.RIGHT);
		col.setText("Value");
		col.setWidth(150);
		
		
		setControl(content);
	}
	
	private void attachCellEditors() {
		tableViewer.setCellModifier(new ICellModifier() {
			public boolean canModify(Object element, String property) {
				if("Value".equals(property)) {
					return true;
				}
				return false;
			}
			public Object getValue(Object element, String property) {
				StratParam sp = (StratParam)element;
				if( "Value".equals(property) ) {
					return sp.value.toString();
				} else {
					return sp.param;
				}
			}
			public void modify(Object element, String property, Object value) {
				TableItem item = (TableItem)element;
				StratParam sp = (StratParam)item.getData();
				if( "Value".equals(property)) {
					try {
						Number num = sp.value;
						if( num instanceof Integer ) {
							Integer i = Integer.parseInt((String)value);
							sp.value = i;
						} else if( num instanceof Double ) {
							Double d = Double.parseDouble((String)value);
							sp.value = d;
						}
					} catch( Exception e ) {
					}
				}
				tableViewer.refresh(sp);
			}
		});
		tableViewer.setCellEditors(new CellEditor[]{ null, new TextCellEditor(tableViewer.getTable())} );
		tableViewer.setColumnProperties(new String[]{"Property", "Value"});
	}
	
	private void attachLabelProvider() {
		tableViewer.setLabelProvider(new ITableLabelProvider() {
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
			public String getColumnText(Object element, int columnIndex) {
				StratParam sp = (StratParam)element;
				if( columnIndex == 0 ) {
					return sp.param;
				} else {
					return sp.value.toString();
				}
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
		});
	}
	
	private void attachContentProvider() {
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				StrategyDefinition stratDef = (StrategyDefinition)inputElement;
				java.util.List<StratParam> stratParams = new ArrayList<StratParam>();
				for(String name: stratDef.getParameterNames()) {
					StratParam sp = new StratParam();
					sp.param = name;
					sp.value = stratDef.getParameter(name);
					stratParams.add(sp);
				}

				return stratParams.toArray();
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
		});
		tableViewer.setInput(stratDef);
	}
}


class ClassLocationPage extends WizardPage {
	
	private Button classpathBtn;
	private Button jarBtn;
	private Button dirBtn;
	private Text jarText;
	private Text dirText;
	
	
	public ClassLocationPage() {
		super("ClassLocation");
		setTitle("Select class location");
		setDescription("Select the location of the strategy class");
		setPageComplete(true);
	}
	
	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NULL);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		content.setLayout(layout);

		classpathBtn = new Button(content, SWT.RADIO);
		classpathBtn.setText("On system classpath");
		GridData gdata = new GridData();
		gdata.horizontalSpan = 3;
		gdata.horizontalAlignment = GridData.BEGINNING;
		classpathBtn.setLayoutData(gdata);
		classpathBtn.setSelection(true);
		
		// TODO: support JAR files
		jarBtn = new Button(content, SWT.RADIO);
		jarBtn.setText("Select JAR file: ");
		jarBtn.setEnabled(false);
		
		jarText = new Text(content, SWT.BORDER);
		gdata = new GridData();
		gdata.minimumWidth = 300;
		jarText.setLayoutData(gdata);
		jarText.setText("");
		jarText.setEnabled(false);
		
		Button browseBtn = new Button(content, SWT.NONE);
		browseBtn.setText("Browse...");
		browseBtn.setEnabled(false);
		
		// TODO: support extra class directories
		dirBtn = new Button(content, SWT.RADIO);
		dirBtn.setText("Select directory: ");
		dirBtn.setEnabled(false);
		
		dirText = new Text(content, SWT.BORDER);
		dirText.setText("");
		gdata = new GridData();
		gdata.minimumWidth = 400;
		dirText.setLayoutData(gdata);
		dirText.setEnabled(false);
		
		browseBtn = new Button(content, SWT.NONE);
		browseBtn.setText("Browse...");
		browseBtn.setEnabled(false);
		
		setControl(content);
	}
}
