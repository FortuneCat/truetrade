package com.ats.client.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.ats.client.views.AvailableDataView;
import com.ats.db.PlatformDAO;
import com.ats.platform.Instrument;
import com.ats.platform.Position;

public class SelectInstrumentDialog extends Dialog {
	
	private ListViewer instrList;
	
	private List<Instrument> availInstruments;
	
	private List<Instrument> selectedInstruments = null;
	
	public SelectInstrumentDialog(Shell parentShell) {
		super(parentShell);
	}
	
	
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);

		GridLayout contentLayout = new GridLayout();
		content.setLayout(contentLayout);
		contentLayout.numColumns = 1;
		content.layout();
		content.pack();
		content.setSize(300, 500);
		
		Label label = new Label(content, SWT.NONE);
		label.setText("Select instruments:");

		availInstruments = PlatformDAO.getAllInstruments();
		
		instrList = new ListViewer(content, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
		instrList.setContentProvider(new InstrContentProvider());
		instrList.setLabelProvider(new InstrLabelProvider());
		instrList.setInput(availInstruments);
		
		
		GridData ldata = new GridData();
		ldata.horizontalAlignment = GridData.FILL;
		ldata.grabExcessHorizontalSpace = true;
		ldata.verticalAlignment = GridData.FILL;
		ldata.grabExcessVerticalSpace = true;
		instrList.getControl().setLayoutData(ldata);
		
		return content;
	}
	
	@Override
	protected void okPressed() {
		IStructuredSelection sel = (IStructuredSelection)instrList.getSelection();
		selectedInstruments = (List<Instrument>)sel.toList();
		super.okPressed();
	}
	
	public List<Instrument> getSelectedInstruments() {
		return this.selectedInstruments;
	}

}

class InstrContentProvider implements IStructuredContentProvider {
	public Object[] getElements(Object inputElement) {
		return ((List)inputElement).toArray();
	}
	public void dispose() {
	}
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
class InstrLabelProvider extends LabelProvider {
	public String getText(Object base) {
		if( base instanceof Instrument ) {
			Instrument instr = (Instrument)base;
			return instr.getSymbol() + " - " + instr.getExchange() + " @ " + instr.getCurrency();
		}
		return "";
	}
	public Image getImage(Object obj) {
		return null;
	}
}
