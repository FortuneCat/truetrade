package com.ats.client.dialogs;

import org.apache.log4j.Logger;
import org.eclipse.gef.ui.palette.SettingsAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ats.platform.Instrument;
import com.ats.platform.Instrument.InstrumentType;

public class AddContractDialog extends Dialog {
	private static final Logger logger = Logger.getLogger(AddContractDialog.class);
	

	private Combo typeCombo;
	private Combo exchangeCombo;
	private Combo currencyCombo;
	private Text symbolText;
	private Text tickSizeText;
	private Text multiplierText;
	
	private Instrument instrument;
	
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Add Instrument");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(3, false);
        gridLayout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        gridLayout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        gridLayout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        gridLayout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        content.setLayout(gridLayout);
        content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

        Label label = new Label(content, SWT.NONE);
        label.setText("Symbol: ");
        label.setLayoutData(new GridData(60, SWT.DEFAULT));

        symbolText = new Text(content, SWT.BORDER);
        symbolText.setSize(80, SWT.DEFAULT);
        GridData gridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1);
        gridData.widthHint = 80;
        symbolText.setLayoutData(gridData);
        symbolText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String symbol = symbolText.getText();
				getOKButton().setEnabled(symbol != null && symbol.trim().length() > 0);
			}
        });
        

        typeCombo = new Combo(content, SWT.READ_ONLY);
        typeCombo.setLayoutData(new GridData(0, 0, false, false, 1, 1));
        for( InstrumentType type : InstrumentType.values() ) {
        	typeCombo.add(type.toString());
        	typeCombo.setData(type.toString(), type);
        }
        typeCombo.setText(InstrumentType.stock.toString());

        exchangeCombo = new Combo(content, SWT.READ_ONLY);
        exchangeCombo.setLayoutData(new GridData(0, 0, false, false, 1, 1));
        for( String exch : Instrument.EXCHANGES ) {
        	exchangeCombo.add(exch);
        }
        exchangeCombo.setText("SMART");
        
        currencyCombo = new Combo(content, SWT.NONE);
        currencyCombo.setLayoutData(new GridData(0, 0, false, false, 1, 1));
        currencyCombo.add("USD");
        currencyCombo.setText("USD");
        
        label = new Label(content, SWT.NONE);
        GridData gdata = new GridData();
        gdata.horizontalSpan = 3;
        gdata.heightHint = 25;
        label.setLayoutData(gdata);
        
        
        label = new Label(content, SWT.NONE);
        label.setText("Tick size: ");
        gdata = new GridData();
        label.setLayoutData(gdata);

        tickSizeText = new Text(content, SWT.BORDER);
        tickSizeText.setSize(80, SWT.DEFAULT);
        tickSizeText.setText("0.01");
        gdata.widthHint = 80;
        tickSizeText.setLayoutData(gridData);
        
        
        label = new Label(content, SWT.NONE);
        label.setText("Multiplier: ");
        gdata = new GridData();
        label.setLayoutData(gdata);

        multiplierText = new Text(content, SWT.BORDER);
        multiplierText.setSize(80, SWT.DEFAULT);
        multiplierText.setText("1");
        gdata = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1);
        gdata.widthHint = 80;
        multiplierText.setLayoutData(gridData);
        
        return content;
    }

	@Override
	protected void okPressed() {
		// create and store the contract
		try {
			instrument = new Instrument();
			InstrumentType instrumentType = InstrumentType.valueOf(typeCombo.getText());
			instrument.getContract().m_secType = instrumentType.getIbType();
			instrument.setExchange(exchangeCombo.getText());
			instrument.setCurrency(currencyCombo.getText());
			instrument.setSymbol(symbolText.getText().toUpperCase());
			try {
				instrument.setMultiplier(Integer.parseInt(multiplierText.getText()));
				instrument.setTickSize(Double.parseDouble(tickSizeText.getText()));
			} catch( Exception e ) {
				// TODO: popup message
				logger.error("Could not parse multiplier or tick size", e);
			}
		} catch( Exception e) {
			logger.error("Could not create instrument", e);
			instrument = null;
		}
		
		super.okPressed();
	}

	public AddContractDialog(Shell parentShell) {
		super(parentShell);
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

}
