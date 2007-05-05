package com.ats.client.dialogs;

import org.eclipse.gef.ui.palette.SettingsAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ats.engine.StrategyDefinition;
import com.ats.platform.TimeSpan;


public class AddStrategyDialog extends org.eclipse.jface.dialogs.Dialog {

	private Text stratClassText;
	private Combo simulatedTimeSpanCombo;
	private Label label3;
	private Combo defTimeSpanCombo;
	private Label label2;
	private Label label1;
	
	private StrategyDefinition strategy;

	public AddStrategyDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Add Strategy");
	}
	


	@Override
	protected Control createDialogArea(Composite parent) {

		Composite content = new Composite(parent, SWT.NONE);

		GridLayout contentLayout = new GridLayout();
		content.setLayout(contentLayout);
		contentLayout.numColumns = 2;
		content.layout();
		content.pack();
		content.setSize(342, 191);
		{
			label1 = new Label(content, SWT.NONE);
			label1.setText("Strategy Class:");
		}
		{
			GridData stratClassTextLData = new GridData();
			stratClassTextLData.heightHint = 13;
			stratClassTextLData.grabExcessHorizontalSpace = true;
			stratClassTextLData.horizontalAlignment = GridData.FILL;
			stratClassText = new Text(content, SWT.BORDER);
			stratClassText.setLayoutData(stratClassTextLData);
		}
		{
			label2 = new Label(content, SWT.NONE);
			label2.setText("Saved data time span:");
		}
		{
			GridData defTimeSpanComboLData = new GridData();
			defTimeSpanCombo = new Combo(content, SWT.READ_ONLY);
			defTimeSpanCombo.setLayoutData(defTimeSpanComboLData);
			for (TimeSpan span : TimeSpan.values()) {
				defTimeSpanCombo.add(span.name());
			}
			defTimeSpanCombo.setText(TimeSpan.daily.name());

		}
		{
			label3 = new Label(content, SWT.NONE);
			label3.setText("Simulated time span:");
		}
		{
			GridData simulatedTimeSpanComboLData = new GridData();
			simulatedTimeSpanCombo = new Combo(content, SWT.READ_ONLY);
			simulatedTimeSpanCombo.setLayoutData(simulatedTimeSpanComboLData);
			for (TimeSpan span : TimeSpan.values()) {
				simulatedTimeSpanCombo.add(span.name());
			}
			simulatedTimeSpanCombo.setText(TimeSpan.daily.name());

		}
		return content;
	}
	
	@Override
	protected void okPressed() {
		// create and store the contract
		try {
			strategy = new StrategyDefinition();
			strategy.setStrategyClassName(stratClassText.getText().trim());
			strategy.setBacktestDataTimeSpan(Enum.valueOf(TimeSpan.class, defTimeSpanCombo.getText()));
			strategy.setBacktestSimulatedTimeSpan(Enum.valueOf(TimeSpan.class, simulatedTimeSpanCombo.getText()));
		} catch( Exception e) {
			strategy = null;
		}
		
		super.okPressed();
	}

	public StrategyDefinition getStrategyDefinition() {
		return strategy;
	}

	
}
