package com.ats.client.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.ats.db.PlatformDAO;
import com.ats.platform.Instrument;

public class InstrumentPage extends WizardPage {

	private org.eclipse.swt.widgets.List instrumentList;
	private boolean isSingleSelect = false;
	
	public InstrumentPage(boolean isSingleSelect) {
		super("InstrumentPage");
		setTitle("Instrument Selection");
		setDescription("Select the financial instruments to be updated");
		this.isSingleSelect = isSingleSelect;
	}
	
	public List<Instrument> getInstruments() {
		List<Instrument> ret = new ArrayList<Instrument>();
		for( String key : instrumentList.getSelection() ) {
			ret.add((Instrument)instrumentList.getData(key));
		}
		return ret;
	}
	
	
	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NULL);
		
		GridLayout thisLayout = new GridLayout();
		thisLayout.numColumns = 2;
		content.setLayout(thisLayout);
		
		if( isSingleSelect ) {
			instrumentList = new org.eclipse.swt.widgets.List(content, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
		} else {
			instrumentList = new org.eclipse.swt.widgets.List(content, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		}
		
		GridData gdata = new GridData();
		gdata.verticalSpan = 2;
		gdata.verticalAlignment = GridData.FILL;
		gdata.grabExcessVerticalSpace = true;
		gdata.widthHint = 250;
		instrumentList.setLayoutData(gdata);
		
		List<Instrument> instruments = PlatformDAO.getAllInstruments();
		for(Instrument instr : instruments ) {
			String key = instr.getSymbol() + " (" + instr.getInstrumentType() + ")";
			instrumentList.add(key);
			instrumentList.setData(key, instr);
		}
		
		setPageComplete(false);
		instrumentList.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				setPageComplete(instrumentList.getSelectionCount() > 0);
			}
		});
		
		if( !isSingleSelect ) {
			
			Button selectAllBtn = new Button(content, SWT.NONE);
			gdata = new GridData();
			gdata.verticalAlignment = GridData.BEGINNING;
			selectAllBtn.setLayoutData(gdata);
			
			selectAllBtn.setText("Select All");
			selectAllBtn.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event evt) {
					instrumentList.selectAll();
					setPageComplete(true);
				}
			});
			
			Button selectNoneBtn = new Button(content, SWT.NONE);
			selectNoneBtn.setLayoutData(gdata);
			
			selectNoneBtn.setText("Deselect All");
			selectNoneBtn.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event evt) {
					instrumentList.deselectAll();
					setPageComplete(false);
				}
			});
		}
		
		setControl(content);
	}
	


}
