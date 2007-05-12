package com.ats.client.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.ats.engine.StrategyDefinition;

public class ParamValuesPage extends WizardPage {
	private static final String PARAM_NAME = "Param Name";

	private static final String PARAM_VALUE = "Param value";

	private static final Logger logger = Logger.getLogger(ParamValuesPage.class);

	private StrategyDefinition stratDef;
	private ArrayList<ParamValues> values = new ArrayList<ParamValues>(0);
	//private String paramNames[];
	
	private Text totalTrials;
	private TreeViewer tviewer;
	
	public ParamValuesPage() {
		super("Parameter Selection");
		setTitle("Select Parameter Values");
		setDescription("Select the range of values which will be used to optimize the performance.");
		setPageComplete(true);
	}

	public void setStratDefParams(final StrategyDefinition strategyDefinition,
			final String[] selectedParamNames) {
		if( strategyDefinition.equals(stratDef)) {
			// see if paramNames are the same
			if( selectedParamNames.length == values.size() ) {
				List<String> paramNames = Arrays.asList(selectedParamNames);
				boolean foundAll = true;
				for(ParamValues val : values ) {
					if( ! paramNames.contains(val.paramName) ) {
						foundAll = false;
					}
				}
				if( foundAll ) {
					return;
				}
			}
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
		
		tviewer.setInput(values);
		tviewer.expandAll();
	}
	
	private void calcNumTrials() {
		int numTrials = 1;
		for(ParamValues val : values) {
			numTrials *= val.numTrials;
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
		
		tviewer = new TreeViewer(content, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		gdata = new GridData(GridData.FILL_BOTH);
		gdata.horizontalSpan = 2;
		tviewer.getTree().setLayoutData(gdata);

		tviewer.setContentProvider(new ParamValuesContentProvider());
		tviewer.setLabelProvider(new ParamValuesLabelProvider());
		tviewer.setInput(values);
		tviewer.setColumnProperties(new String[]{ PARAM_NAME, PARAM_VALUE });
		
		configureTreeEditor();
		
		
		Tree tree = tviewer.getTree();
		TreeColumn col = new TreeColumn(tree, SWT.LEFT);
		col.setText(PARAM_NAME);
		col.setWidth(130);
		col = new TreeColumn(tree, SWT.LEFT);
		col.setText(PARAM_VALUE);
		col.setWidth(150);
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		
		setControl(content);
	}

	private void configureTreeEditor() {
		final int EDITABLECOLUMN = 1;

		// if only CellEditors would work, but they don't.  They never get focus!
		// So we have to do this beastly work-around hack.
		
		final TreeEditor editor = new TreeEditor(tviewer.getTree());
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		// do editing on cell selection
		tviewer.getTree().addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Tree tree = tviewer.getTree();
				final TreeItem item = tree.getSelection()[0];
				if( !(item.getData() instanceof ParamValueItem) ) {
					return;
				}
				if( ((ParamValueItem)item.getData()).type == ParamValueType.numTrades ) {
					return;
				}
				
				final Text text = new Text(tree, SWT.NONE);
				text.setText(item.getText(EDITABLECOLUMN));
				text.selectAll();
				text.setFocus();
				
				text.addFocusListener(new FocusAdapter() {
					public void focusLost(FocusEvent event) {
						setValueFromEditor(item, text);
						text.dispose();
					}

				});
				// If they hit Enter, set the text into the tree and end the
				// editing session. If they hit Escape, ignore the text and end the
				// editing session
//				text.addKeyListener(new KeyAdapter() {
//					public void keyPressed(KeyEvent event) {
//						switch (event.keyCode) {
//						case SWT.CR:
//							// Enter hit--set the text into the tree 
//							setValueFromEditor(item, text);
//							text.dispose();
//							break;
//						case SWT.ESC:
//							// End editing session
//							text.dispose();
//							break;
//						}
//					}
//				});

				// Set the text field into the editor
				editor.setEditor(text, item, EDITABLECOLUMN);
			}
			private void setValueFromEditor(final TreeItem item, final Text text) {
				final ParamValueItem pvi = ((ParamValueItem) item.getData());
				String res = "";
				try {
					boolean isInt = (pvi.values.initVal instanceof Integer);
					// tried trinary operator, but kept getting
					// bugs. Works with an if/else, no idea why.
					Number val = null;
					if (isInt) {
						val = Integer.parseInt(text.getText());
					} else {
						val = Double.parseDouble(text.getText());
					}
					switch (pvi.type) {
					case start:
						pvi.values.start = val;
						res = pvi.values.start.toString();
						break;
					case finish:
						pvi.values.finish = val;
						res = pvi.values.finish.toString();
						break;
					case stepSize:
						pvi.values.stepSize = val;
						res = pvi.values.stepSize.toString();
						break;
					}
					// reset the number of trials
					if (isInt) {
						pvi.values.numTrials = (pvi.values.finish.intValue() - pvi.values.start.intValue())
								/ pvi.values.stepSize.intValue() + 1;
					} else {
						pvi.values.numTrials = (int) ((pvi.values.finish.doubleValue() 
								- pvi.values.start.doubleValue()) / pvi.values.stepSize.doubleValue()) + 1;
					}
				} catch (Exception e) {
					logger.debug("Parse exception: " + e);
				}
				
				
				logger.debug("before modifyText, values="
						+ ParamValuesPage.this.values.toString());
				if( ! "".equals(res)) {
					item.setText(EDITABLECOLUMN, res);
					for(TreeItem child : item.getParentItem().getItems()) {
						ParamValueItem curr = ((ParamValueItem) child.getData());
						if( curr.type == ParamValueType.numTrades ) {
							child.setText(EDITABLECOLUMN, "" + curr.values.numTrials);
						}
					}
					calcNumTrials();
				}
			}
		});
	}

	public List<ParamValues> getParamValues() {
		return values;
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
