package com.ats.client.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.ats.utils.Utils;

public class DatabasePage extends PreferencePage {
	
	private Button useBuiltinButton;
	private Text urlText;
	private Text userText;
	private Text passwordText;
	private Text jdbcProviderText;
	private Text jdbcJarText;
	private Button browseBtn;
	
	public DatabasePage() {
		super();
		setTitle("Databases");
		setDescription("Configure database parameters");
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		GridLayout contentLayout = new GridLayout(1, false);
		contentLayout.numColumns = 3;
		contents.setLayout(contentLayout);
		
		IPreferenceStore prefStore = getPreferenceStore();

		useBuiltinButton = new Button(contents, SWT.CHECK);
		useBuiltinButton.setText("Use default database");
		GridData gdata = new GridData();
		gdata.horizontalSpan = 3;
		useBuiltinButton.setLayoutData(gdata);
		useBuiltinButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				customButtonSelected();
			}
		});
		
		Label label = new Label(contents, SWT.NONE);
		label.setText("User name:");
		
		userText = new Text(contents, SWT.BORDER);
		userText.setText(prefStore.getString(Utils.DB_USER));
		gdata = new GridData();
		gdata.horizontalSpan = 2;
		gdata.widthHint = 200;
		userText.setLayoutData(gdata);

		label = new Label(contents, SWT.NONE);
		label.setText("Password:");
		
		passwordText = new Text(contents, SWT.BORDER);
		passwordText.setText(prefStore.getString(Utils.DB_PASSWORD));
		gdata = new GridData();
		gdata.horizontalSpan = 2;
		gdata.widthHint = 200;
		passwordText.setLayoutData(gdata);

		label = new Label(contents, SWT.NONE);
		label.setText("JDBC URL:");
		
		urlText = new Text(contents, SWT.BORDER);
		urlText.setText(prefStore.getString(Utils.DB_URL));
		gdata = new GridData();
		gdata.horizontalSpan = 2;
		gdata.widthHint = 200;
		gdata.verticalIndent = 10;
		urlText.setLayoutData(gdata);

		label = new Label(contents, SWT.NONE);
		label.setText("JDBC Provider:");
		
		jdbcProviderText = new Text(contents, SWT.BORDER);
		jdbcProviderText.setText(prefStore.getString(Utils.DB_PROVIDER));
		gdata = new GridData();
		gdata.horizontalSpan = 2;
		gdata.widthHint = 200;
		jdbcProviderText.setLayoutData(gdata);

		label = new Label(contents, SWT.NONE);
		label.setText("JDBC Jar File:");
		
		jdbcJarText = new Text(contents, SWT.BORDER);
		jdbcJarText.setText(prefStore.getString(Utils.DB_JARFILE));
		gdata = new GridData();
		gdata.horizontalSpan = 1;
		gdata.widthHint = 200;
		jdbcJarText.setLayoutData(gdata);
		
		browseBtn = new Button(contents, SWT.PUSH);
		browseBtn.setText("Browse...");


		useBuiltinButton.setSelection(prefStore.getBoolean(Utils.DB_USE_DEFAULT));
		customButtonSelected();

		return contents;
	}

	public boolean performOk() {
		IPreferenceStore prefStore = getPreferenceStore();

		if( useBuiltinButton != null ) {
			prefStore.setValue(Utils.DB_USE_DEFAULT, useBuiltinButton.getSelection());
			prefStore.setValue(Utils.DB_JARFILE, jdbcJarText.getText());
			prefStore.setValue(Utils.DB_PROVIDER, jdbcProviderText.getText());
			prefStore.setValue(Utils.DB_PASSWORD, passwordText.getText());
			prefStore.setValue(Utils.DB_URL, urlText.getText());
			prefStore.setValue(Utils.DB_USER, userText.getText());
		}
		return true;
	}

	private void customButtonSelected() {
		boolean isSelected = useBuiltinButton.getSelection();
		jdbcJarText.setEnabled(!isSelected);
		jdbcProviderText.setEnabled(!isSelected);
		passwordText.setEnabled(!isSelected);
		urlText.setEnabled(!isSelected);
		userText.setEnabled(!isSelected);
	}

}
