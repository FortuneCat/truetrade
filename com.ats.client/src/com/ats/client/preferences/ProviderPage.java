package com.ats.client.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ats.utils.Utils;

public class ProviderPage extends PreferencePage {
	
	private Text hostText;
	private Text portText;
	private Text clientidText;
	
	private Text otUserText;
	private Text otPasswordText;
	
	public ProviderPage() {
		super();
		setTitle("Providers");
		setDescription("Configure preferences for data providers.");
		
	}
	
	
	
	@Override
	protected Control createContents(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		GridLayout contentLayout = new GridLayout(1, false);
		contentLayout.verticalSpacing = 15;
		contents.setLayout(contentLayout);
		
		IPreferenceStore prefStore = getPreferenceStore();

		// InteractiveBrokers
		Group group = new Group(contents, SWT.NONE);
		group.setText("InteractiveBrokers");
		GridLayout groupLayout = new GridLayout(1, false);
		groupLayout.numColumns = 2;
		group.setLayout(groupLayout);
		GridData groupData = new GridData();
		groupData.horizontalAlignment = SWT.FILL;
		groupData.grabExcessHorizontalSpace = true;
		group.setLayoutData(groupData);

		Label label = new Label(group, SWT.NONE);
		label.setText("Host name:");
		hostText = new Text(group, SWT.BORDER);
		hostText.setText(prefStore.getString(Utils.INTERACTIVE_BROKERS_HOST));
		GridData gdata = new GridData();
		gdata.widthHint = 200;
		hostText.setLayoutData(gdata);
		
		label = new Label(group, SWT.NONE);
		label.setText("Port number:");
		portText = new Text(group, SWT.BORDER);
		portText.setText(prefStore.getString(Utils.INTERACTIVE_BROKERS_PORT));
		gdata = new GridData();
		gdata.widthHint = 200;
		portText.setLayoutData(gdata);
		
		label = new Label(group, SWT.NONE);
		label.setText("Client id:");
		clientidText = new Text(group, SWT.BORDER);
		clientidText.setText(prefStore.getString(Utils.INTERACTIVE_BROKERS_CLIENTID));
		gdata = new GridData();
		gdata.widthHint = 200;
		clientidText.setLayoutData(gdata);

		// Open Tick
		group = new Group(contents, SWT.NONE);
		group.setText("OpenTick");
		groupLayout = new GridLayout(1, false);
		groupLayout.numColumns = 2;
		group.setLayout(groupLayout);
		groupData = new GridData();
		groupData.horizontalAlignment = SWT.FILL;
		groupData.grabExcessHorizontalSpace = true;
		group.setLayoutData(groupData);

		label = new Label(group, SWT.NONE);
		label.setText("User name:");
		otUserText = new Text(group, SWT.BORDER);
		otUserText.setText(prefStore.getString(Utils.OPENTICK_USER));
		gdata = new GridData();
		gdata.widthHint = 200;
		otUserText.setLayoutData(gdata);
		
		label = new Label(group, SWT.NONE);
		label.setText("Password:");
		otPasswordText = new Text(group, SWT.BORDER | SWT.PASSWORD);
		otPasswordText.setText(prefStore.getString(Utils.OPENTICK_PASSWORD));
		gdata = new GridData();
		gdata.widthHint = 200;
		otPasswordText.setLayoutData(gdata);
		
		
		return contents;
		
	}

	protected void performDefaults() {
		hostText.setText("127.0.0.1");
		portText.setText("6887");
		clientidText.setText("0");
		
		otUserText.setText("username");
		otPasswordText.setText("");
	}
	
	public boolean performOk() {
		IPreferenceStore prefStore = getPreferenceStore();

		if( hostText != null ) {
			prefStore.setValue(Utils.INTERACTIVE_BROKERS_HOST, hostText.getText());
		}
		if( portText != null ) {
			prefStore.setValue(Utils.INTERACTIVE_BROKERS_PORT, portText.getText());
		}
		if( clientidText != null ) {
			prefStore.setValue(Utils.INTERACTIVE_BROKERS_CLIENTID, clientidText.getText());
		}
		if( otUserText != null ) {
			prefStore.setValue(Utils.OPENTICK_USER, otUserText.getText());
		}
		if( otPasswordText != null ) {
			prefStore.setValue(Utils.OPENTICK_PASSWORD, otPasswordText.getText());
		}
		return true;
	}

}
