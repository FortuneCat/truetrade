package com.ats.client.wizards;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableTreeEditor;
import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.nebula.widgets.compositetable.CompositeTable;
import org.eclipse.swt.nebula.widgets.compositetable.IRowContentProvider;
import org.eclipse.swt.nebula.widgets.compositetable.RowFocusAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.ats.engine.StrategyDefinition;

public class ParamValuesPage extends WizardPage {
	private static final String PARAM_NAME = "Param Name";

	private static final String PARAM_VALUE = "Param value";

	private static final Logger logger = Logger.getLogger(ParamValuesPage.class);

	private StrategyDefinition stratDef;
	private ArrayList<ParamValues> values = new ArrayList<ParamValues>(0);
	//private String paramNames[];
	
	private Text totalTrials;
	private TableTreeViewer ttviewer;
	
	public ParamValuesPage() {
		super("Parameter Selection");
		setTitle("Select Parameter Values");
		setDescription("Select the range of values which will be used to optimize the performance.");
		setPageComplete(true);
	}

	public void setStratDefParams(final StrategyDefinition strategyDefinition,
			final String[] selectedParamNames) {
		if( strategyDefinition.equals(stratDef)) {
			// no action needs to be taken
			return;
		}
		this.stratDef = strategyDefinition;
		values = new ArrayList<ParamValues>(selectedParamNames.length);
		for(String name : selectedParamNames ) {
			ParamValues vals = new ParamValues();
			vals.paramName = name;
			vals.initVal = stratDef.getParameter(name);
			
			if( vals.initVal instanceof Integer ) {
				int initVal = vals.initVal.intValue();
				vals.start = initVal - 3;
				vals.finish = initVal + 3;
				vals.stepSize = 1;
				vals.numTrials = 7;
			} else if( vals.initVal instanceof Double) {
				double initVal = vals.initVal.doubleValue();
				vals.start = initVal - 2.0;
				vals.finish = initVal + 2.0;
				vals.stepSize = 0.5;
				vals.numTrials = 9;
			}
			values.add(vals);
		}
		calcNumTrials();
		
		ttviewer.setInput(values);
		ttviewer.expandAll();
	}
	
	private void calcNumTrials() {
		int numTrials = 0;
		for(ParamValues val : values) {
			numTrials += val.numTrials;
		}
		totalTrials.setText("" + numTrials);
	}
	
	@SuppressWarnings("deprecation")
	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		content.setLayout(layout);
		
		new Label(content, SWT.NULL).setText("Total trials:");
		totalTrials = new Text(content, SWT.NULL);
		totalTrials.setEditable(false);
		GridData gdata = new GridData();
		gdata.horizontalAlignment = GridData.FILL;
		gdata.grabExcessHorizontalSpace = true;
		totalTrials.setLayoutData(gdata);
		
		ttviewer = new TableTreeViewer(content, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		gdata = new GridData(GridData.FILL_BOTH);
		gdata.horizontalSpan = 2;
		ttviewer.getTableTree().setLayoutData(gdata);

		ttviewer.setContentProvider(new ParamValuesContentProvider());
		ttviewer.setLabelProvider(new ParamValuesLabelProvider());
		ttviewer.setInput(values);
		ttviewer.setColumnProperties(new String[]{ PARAM_NAME, PARAM_VALUE });
		
		// CellEditors don't work using the TableTreeEditor!  Have to use this
		// TableTreeEditor hack.  Bleh.  Maybe later releases of the Eclipse
		// framework will improve.
		
		
		final TableTreeEditor editor = new TableTreeEditor(ttviewer.getTableTree());
		//The editor must have the same size as the cell and must
		//not be any smaller than 50 pixels.
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 50;
		// editing the second column
		final int EDITABLECOLUMN = 1;
		
		ttviewer.getTableTree().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Clean up any previous editor control
				Control oldEditor = editor.getEditor();
				if (oldEditor != null) oldEditor.dispose();
		
				// Identify the selected row
				TableTreeItem item = (TableTreeItem)e.item;
				if (item == null) return;
		
				// The control that will be the editor must be a child of the Table
				Text newEditor = new Text(ttviewer.getTableTree().getTable(), SWT.NONE);
				newEditor.setText(item.getText(EDITABLECOLUMN));
				newEditor.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent evt) {
						Text text = (Text)editor.getEditor();
						ParamValueItem item = (ParamValueItem)((TableTreeItem)editor.getItem()).getData();
						String value = text.getText();
						logger.debug("before modifyText, values=" + ParamValuesPage.this.values.toString());
						try {
							boolean isInt = (item.values.initVal instanceof Integer);
							// tried trinary operator, but kept getting bugs.  Works with an
							// if/else, no idea why.
							Number val =  null;
							if( isInt ) {
								val = Integer.parseInt(value);
							} else {
								val = Double.parseDouble(value);
							}
							switch( item.type ) {
							case start:
								item.values.start = val;
								break;
							case finish:
								item.values.finish = val;
								break;
							case stepSize:
								item.values.stepSize = val;
								break;
							}
						} catch( Exception e) {
							logger.debug("Parse exception: " + e);
						}
						logger.debug("before modifyText, values=" + ParamValuesPage.this.values.toString());
						String res = "";
						switch( item.type ) {
						case start:
							res = item.values.start.toString();
							break;
						case finish:
							res = item.values.finish.toString();
							break;
						case stepSize:
							res = item.values.stepSize.toString();
							break;
						}
						editor.getItem().setText(EDITABLECOLUMN, res);
					}
				});
				newEditor.selectAll();
				newEditor.setFocus();
				editor.setEditor(newEditor, item, EDITABLECOLUMN);
			}
		});
		

/*		
		ttviewer.setCellEditors(new CellEditor[]{
				null,
				new TextCellEditor(parent)
		});
		ttviewer.setCellModifier(new ICellModifier() {
			public boolean canModify(Object element, String property) {
				return ( (element instanceof ParamValueItem) && PARAM_VALUE.equals(property));
			}
			public Object getValue(Object element, String property) {
				ParamValueItem item = (ParamValueItem)element;
				switch( item.type ) {
				case start:
					return item.values.start.toString();
				case finish:
					return item.values.finish.toString();
				case stepSize:
					return "" + item.values.stepSize;
				}
				logger.debug("get value returning null");
				return null;
			}
			public void modify(final Object element, String property, Object obvalue) {
				ParamValueItem item = (ParamValueItem)((TableTreeItem)element).getData();
				String value = (String)obvalue;
				logger.debug("Modify prop[" + property + "], set to " + value);
				try {
					boolean isInt = (item.values.initVal instanceof Integer);
					
					switch( item.type ) {
					case start:
						item.values.start = isInt ? Integer.parseInt(value) : Double.parseDouble(value);
						break;
					case finish:
						item.values.finish = isInt ? Integer.parseInt(value) : Double.parseDouble(value);
						break;
					case stepSize:
						item.values.stepSize = isInt ? Integer.parseInt(value) : Double.parseDouble(value);
						break;
					}
				} catch( Exception e) {
					logger.debug("Parse exception: " + e);
				}
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						ttviewer.refresh(element);
					}
				});
			}
		});
*/		
		Table table = ttviewer.getTableTree().getTable();
		TableColumn col = new TableColumn(table, SWT.LEFT);
		col.setText(PARAM_NAME);
		col.setWidth(130);
		col = new TableColumn(table, SWT.LEFT);
		col.setText(PARAM_VALUE);
		col.setWidth(150);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		setControl(content);
	}
}
enum ParamValueType {
	root,
	start,
	finish,
	stepSize,
	numTrades;
}

class ParamValueItem {
	public ParamValueItem(ParamValues values, ParamValueType type) {
		this.type = type;
		this.values = values;
	}
	public ParamValueType type;
	public ParamValues values;
}

class ParamValuesContentProvider implements ITreeContentProvider {
	public Object[] getChildren(Object parentElement) {
		if( parentElement instanceof ParamValues ) {
			ParamValues values = (ParamValues)parentElement;
			return new ParamValueItem[]{
					new ParamValueItem(values, ParamValueType.start),
					new ParamValueItem(values, ParamValueType.finish),
					new ParamValueItem(values, ParamValueType.stepSize),
					new ParamValueItem(values, ParamValueType.numTrades)
			};
		}
		return null;
	}
	public Object getParent(Object element) {
		if( element instanceof ParamValueItem ) {
			return ((ParamValueItem)element).values;
		}
		return null;
	}
	public boolean hasChildren(Object element) {
		return (element instanceof ParamValues);
	}
	public Object[] getElements(Object inputElement) {
		return ((java.util.List)inputElement).toArray();
	}
	public void dispose() {
	}
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
class ParamValuesLabelProvider implements ITableLabelProvider {
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
	public String getColumnText(Object element, int columnIndex) {
		if( element instanceof ParamValues ) {
			ParamValues vals = (ParamValues)element;
			if( columnIndex == 0) {
				return vals.paramName;
			} else {
				return vals.initVal.getClass().getSimpleName() + ": default=" + vals.initVal;
			}
		} else if( element instanceof ParamValueItem ) {
			ParamValueItem item = (ParamValueItem)element;
			switch(item.type) {
			case start:
				return columnIndex == 0 ? "Start:" : item.values.start.toString();
			case finish:
				return columnIndex == 0 ? "Finish:" : item.values.finish.toString();
			case stepSize:
				return columnIndex == 0 ? "Step size:" : item.values.stepSize.toString();
			case numTrades:
				return columnIndex == 0 ? "#Trades:" : "" + item.values.numTrials;
			}
		}
		return null;
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
