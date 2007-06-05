package com.ats.client.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ats.client.tools.ImportTemplate;
import com.ats.db.PlatformDAO;
import com.ats.engine.ImportDataManager;
import com.ats.platform.BarSeries;
import com.ats.platform.Instrument;

public class ImportDataWizard extends Wizard {
	private static final Logger logger = Logger.getLogger(ImportDataWizard.class);
	
	private InstrumentPage instrumentPage;
	TemplatePage templatePage;
	SelectFilePage selectFilePage;
	private Instrument instrument;

	
	public ImportDataWizard() {
		setWindowTitle("Import data");
		setNeedsProgressMonitor(true);
		
		instrumentPage = new InstrumentPage(true);
		selectFilePage = new SelectFilePage();
		templatePage = new TemplatePage();
	}
	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	
	public void addPages() {
		if( instrument == null ) {
			addPage(instrumentPage);
		}
		addPage(selectFilePage);
		addPage(templatePage);
	}

	@Override
	public boolean performFinish() {
		final ImportTemplate template = templatePage.getImportTemplate();
		//final Instrument instrument = instrumentPage.getInstruments().get(0);
		if( instrument == null ) {
			instrument = instrumentPage.getInstruments().get(0);
		}
		final String fileName = selectFilePage.getFile().getAbsolutePath();
		// TODO: check for errors
		try {
			// TODO: enable "Cancel" during import
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Importing records... ", (int)(selectFilePage.getFile().length()/10));
					int count = template.loadBarsFromFile(fileName, instrument, monitor);
				}
			});
		} catch( Exception e) {
			logger.error("Download data interrupted", e);
		}
		return true;
	}
	
	public File getFile() {
		return selectFilePage.getFile();
	}

	public boolean performCancel() {
		return MessageDialog.openConfirm(getShell(), "Confirm", "Are you sure you want to cancel importing?");
	}
}

class SelectFilePage extends WizardPage {
	private Text fileText;
	private Button browseBtn;
	
	private File file;

	public SelectFilePage() {
		super("SelectFile");
		setTitle("Saved trade data");
		setDescription("Select a file with saved data to import and analyze.");
		setPageComplete(false);
	}
	
	public File getFile() {
		return file;
	}

	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NULL);

		GridLayout thisLayout = new GridLayout();
		thisLayout.numColumns = 3;
		content.setLayout(thisLayout);
		{
			Label label1 = new Label(content, SWT.NONE);
			GridData label1LData = new GridData();
			label1LData.grabExcessVerticalSpace = true;
			label1LData.horizontalAlignment = GridData.END;
			label1LData.grabExcessHorizontalSpace = true;
			label1.setLayoutData(label1LData);
			label1.setText("File:");
		}
		{
			GridData fileTextLData = new GridData();
			fileTextLData.widthHint = 200;
			//fileTextLData.heightHint = 13;
			fileTextLData.horizontalIndent = 5;
			fileText = new Text(content, SWT.BORDER);
			fileText.setLayoutData(fileTextLData);
			fileText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if( fileText.getText().trim().length() > 0 ) {
						setPageComplete(true);
					} else {
						setMessage("Please enter a file name");
					}
				}
			});
		}
		{
			browseBtn = new Button(content, SWT.PUSH | SWT.CENTER);
			GridData browseBtnLData = new GridData();
			browseBtnLData.grabExcessHorizontalSpace = true;
			browseBtnLData.horizontalIndent = 5;
			browseBtn.setLayoutData(browseBtnLData);
			browseBtn.setText("Browse...");
			browseBtn.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				public void widgetSelected(SelectionEvent e) {
					FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
					final String file = dialog.open();
					Display.getCurrent().asyncExec(new Runnable() {
						public void run() {
							if( file != null && file.length() > 0 ) {
								fileText.setText(file);
							}
						}
					});
				}
				
			});
		}
		
		setControl(content);
	}

	@Override
	public boolean canFlipToNextPage() {
		if( fileText.getText().trim().length() <= 0 ) {
			return false;
		}
		try {
			file = new File(fileText.getText());
			
			// hackety hack hack
			// it would be nice if one page didn't have to push data to another
			((ImportDataWizard)getWizard()).templatePage.setFile(file);
			return file.canRead();
		} catch( Exception e) {
			setErrorMessage("Cannot read file.");
			return false;
		}
	}
	
}
/*
class DateFormatPage extends WizardPage {

	public DateFormatPage() {
		super("DateFormat");
		setTitle("Trade date format");
		setDescription("Describe the format to parse your saved date data.");
		setPageComplete(true);
	}
	
	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NULL);
		setControl(content);
	}
}

*/