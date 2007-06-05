package com.ats.client.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.ats.db.PlatformDAO;
import com.ats.engine.ImportDataManager;
import com.ats.engine.ImportDataManager.HistDataProvider;
import com.ats.platform.BarSeries;
import com.ats.platform.Instrument;
import com.ats.platform.TimeSpan;
import com.tiff.common.ui.datepicker.DatePickerCombo;


public class DownloadHistDataWizard extends Wizard {
	private static final Logger logger = Logger.getLogger(DownloadHistDataWizard.class);
	
	private ProviderPage providerPage;
	private InstrumentPage instrumentPage;
	private DataTypePage dataTypePage;
	private DateRangePage dateRangePage;
	private List<Instrument> selectedInstruments;
	
	
	public DownloadHistDataWizard() {
		setWindowTitle("Download historical data");
		setNeedsProgressMonitor(true);
		
		providerPage = new ProviderPage();
		instrumentPage = new InstrumentPage(false);
		dataTypePage = new DataTypePage();
		dateRangePage = new DateRangePage();
	}
	
	public void setSelectedInstruments(List<Instrument> selectedInstruments) {
		this.selectedInstruments = selectedInstruments;
	}
	
	public void addPages() {
		addPage(providerPage);
		if( selectedInstruments == null || selectedInstruments.size() <= 0 ) {
			addPage(instrumentPage);
		}
		addPage(dataTypePage);
		addPage(dateRangePage);
	}

	@Override
	public boolean performFinish() {
		final HistDataProvider provider = providerPage.getProvider();
		//final List<Instrument> instruments = instrumentPage.getInstruments();
		if( selectedInstruments == null || selectedInstruments.size() <= 0 ) {
			selectedInstruments = instrumentPage.getInstruments();
		}
		final TimeSpan timeSpan = dataTypePage.getTimeSpan();
		final Date startDate = dateRangePage.getStartDate();
		final Date endDate = dateRangePage.getEndDate();
		
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Download historical data", selectedInstruments.size());
					for(Instrument instr : selectedInstruments ) {
						// download data
						monitor.subTask("Downloading " + instr.getSymbol());
						try {
							BarSeries series = ImportDataManager.getInstance().downloadHistData(provider, instr, timeSpan, startDate, endDate);
							BarSeries currSeries = PlatformDAO.getBarSeries(instr, timeSpan);
							if( currSeries != null && series != null ) {
								//currSeries.addHistory(series);
								//PlatformDAO.insertBarSeries(currSeries);
								PlatformDAO.insertBars(currSeries);
							} else if( series != null ){
								PlatformDAO.insertBarSeries(series);
							}
						} catch( Exception e) {
							logger.error("Could not download data for " + instr.getSymbol());
						}
						monitor.worked(1);
					}
					monitor.done();
				}
			});
		} catch( Exception e) {
			logger.error("Download data interrupted", e);
		}
		
		
		return true;
	}

}

class ProviderPage extends WizardPage {
	private Combo providerCombo;

	public ProviderPage() {
		super("SelectProvider");
		setTitle("Historical provider");
		setDescription("Select an historical data provider.");
		setPageComplete(true);
	}
	
	public HistDataProvider getProvider() {
		return Enum.valueOf(HistDataProvider.class, providerCombo.getText());
	}

	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NULL);

		GridLayout thisLayout = new GridLayout();
		thisLayout.numColumns = 1;
		content.setLayout(thisLayout);
		
		providerCombo = new Combo(content, SWT.READ_ONLY);
		for(HistDataProvider hdp : HistDataProvider.values()) {
			providerCombo.add(hdp.name());
		}
		providerCombo.setText(HistDataProvider.Yahoo.name());
		setControl(content);
	}
}

class DataTypePage extends WizardPage {
	private Combo timeSpanCombo;

	public DataTypePage() {
		super("DataType");
		setTitle("Time span");
		setDescription("Select the time span to download.");
		setPageComplete(true);
	}
	
	public TimeSpan getTimeSpan() {
		return Enum.valueOf(TimeSpan.class, timeSpanCombo.getText());
	}

	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NULL);

		GridLayout thisLayout = new GridLayout();
		thisLayout.numColumns = 1;
		content.setLayout(thisLayout);
		
		timeSpanCombo = new Combo(content, SWT.READ_ONLY);
		for(TimeSpan span : TimeSpan.values()) {
			timeSpanCombo.add(span.name());
		}
		timeSpanCombo.setText(TimeSpan.daily.name());
		setControl(content);
	}
}

class DateRangePage extends WizardPage {
	private DatePickerCombo startDateChooser;
	private DatePickerCombo endDateChooser;

	public DateRangePage() {
		super("DateRange");
		setTitle("Date Range");
		setDescription("Select the start and end dates for this historical data request.");
		setPageComplete(true);
	}
	
	public Date getStartDate() {
		return startDateChooser.getDate();
	}
	
	public Date getEndDate() {
		return endDateChooser.getDate();
	}

	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NULL);

		GridLayout thisLayout = new GridLayout();
		thisLayout.numColumns = 1;
		content.setLayout(thisLayout);

		new Label(content, SWT.NONE).setText("Select the start date");

		startDateChooser = new DatePickerCombo(content, SWT.BORDER);
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.YEAR, -2);
		startDateChooser.setDate(cal.getTime());
		
		new Label(content, SWT.NONE).setText("Select the end date");
		
		endDateChooser = new DatePickerCombo(content, SWT.BORDER);
		endDateChooser.setDate(new Date());
		
		setControl(content);
	}
}
