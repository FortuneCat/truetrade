package com.ats.client.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import static com.ats.utils.Utils.*;

public class BacktestOrderPage extends PreferencePage  {
	
	
	private Button percBtn;
	private Text percText;
	private Button tickBtn;
	private Text tickText;
	
	private Button perShareBtn;
	private Text perShareText;
	private Button perOrderBtn;
	private Text perOrderText;
	
	public BacktestOrderPage() {
		super();
		setTitle("Backtesting");
		setDescription("Set parameters for backtesting orders.");
	}

	protected Control createContents(Composite parent) {
		
		Composite contents = new Composite(parent, SWT.NONE);
		GridLayout contentLayout = new GridLayout(1, false);
		contentLayout.verticalSpacing = 20;
		contents.setLayout(contentLayout);
	
		IPreferenceStore prefStore = getPreferenceStore();
		prefStore.setDefault(SLIPPAGE_PERCENT, true);
		prefStore.setDefault(SLIPPAGE_TICK, false);
		prefStore.setDefault(COMMISSION_SHARE, true);
		prefStore.setDefault(COMMISSION_ORDER, false);
		prefStore.setDefault(SLIPPAGE_PERCENT_VALUE, 0.2);
		prefStore.setDefault(COMMISSION_SHARE_VALUE, 0.01);
		
		// slippage
		Group group = new Group(contents, SWT.NONE);
		group.setText("Simulated slippage");
		GridLayout groupLayout = new GridLayout(1, false);
		groupLayout.numColumns = 2;
		group.setLayout(groupLayout);
		GridData groupData = new GridData();
		groupData.horizontalAlignment = SWT.FILL;
		groupData.grabExcessHorizontalSpace = true;
		group.setLayoutData(groupData);
		
		percBtn = new Button(group, SWT.RADIO);
		percBtn.setText("Percentage");
		percBtn.setSelection(prefStore.getBoolean(SLIPPAGE_PERCENT));
		
		percText = new Text(group, SWT.BORDER);
		percText.setText(prefStore.getString(SLIPPAGE_PERCENT_VALUE));
		GridData gdata = new GridData();
		gdata.widthHint = 100;
		percText.setLayoutData(gdata);
		
		tickBtn = new Button(group, SWT.RADIO);
		tickBtn.setText("Ticks");
		tickBtn.setSelection(prefStore.getBoolean(SLIPPAGE_TICK));
		
		tickText = new Text(group, SWT.BORDER);
		tickText.setText(prefStore.getString(SLIPPAGE_TICK_VALUE));
		gdata = new GridData();
		gdata.widthHint = 100;
		tickText.setLayoutData(gdata);
		
		
		// Comission
		group = new Group(contents, SWT.NONE);
		group.setText("Simulated commission");
		groupLayout = new GridLayout(1, false);
		groupLayout.numColumns = 2;
		group.setLayout(groupLayout);
		groupData = new GridData();
		groupData.horizontalAlignment = SWT.FILL;
		groupData.grabExcessHorizontalSpace = true;
		group.setLayoutData(groupData);
		
		perShareBtn = new Button(group, SWT.RADIO);
		perShareBtn.setText("per share");
		perShareBtn.setSelection(prefStore.getBoolean(COMMISSION_SHARE));
//		button.addSelectionListener(new SelectionListener() {
//			public void widgetDefaultSelected(SelectionEvent e) {
//				perShareText.setEnabled(((Button)e.item).getSelection());
//			}
//			public void widgetSelected(SelectionEvent e) {
//				perShareText.setEnabled(((Button)e.item).getSelection());
//			}
//		});

		
		// TODO: block non-double values
		perShareText = new Text(group, SWT.BORDER);
		perShareText.setText(prefStore.getString(COMMISSION_SHARE_VALUE));
		gdata = new GridData();
		gdata.widthHint = 100;
		perShareText.setLayoutData(gdata);
		
		perOrderBtn = new Button(group, SWT.RADIO);
		perOrderBtn.setText("per order");
		perOrderBtn.setSelection(prefStore.getBoolean(COMMISSION_ORDER));
		
		perOrderText = new Text(group, SWT.BORDER);
		perOrderText.setText(prefStore.getString(COMMISSION_ORDER_VALUE));
		gdata = new GridData();
		gdata.widthHint = 100;
		perOrderText.setLayoutData(gdata);

		
		
		
		return contents;
	}
	
	protected void performDefaults() {
		IPreferenceStore prefStore = getPreferenceStore();
		
		percText.setText(prefStore.getDefaultString(SLIPPAGE_PERCENT_VALUE));
		tickText.setText(prefStore.getDefaultString(SLIPPAGE_TICK_VALUE));
	}
	
	public boolean performOk() {
		IPreferenceStore prefStore = getPreferenceStore();

		prefStore.setValue(SLIPPAGE_PERCENT, percBtn.getSelection());
		prefStore.setValue(SLIPPAGE_PERCENT_VALUE, Double.parseDouble(percText.getText()));
		prefStore.setValue(SLIPPAGE_TICK, tickBtn.getSelection());
		prefStore.setValue(SLIPPAGE_TICK_VALUE, Double.parseDouble(tickText.getText()));
		
		prefStore.setValue(COMMISSION_SHARE, perShareBtn.getSelection());
		prefStore.setValue(COMMISSION_SHARE_VALUE, Double.parseDouble(perShareText.getText()));
		prefStore.setValue(COMMISSION_ORDER, perOrderBtn.getSelection());
		prefStore.setValue(COMMISSION_ORDER_VALUE, Double.parseDouble(perOrderText.getText()));
		
		return true;
	}

}