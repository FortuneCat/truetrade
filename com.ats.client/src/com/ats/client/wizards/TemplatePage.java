package com.ats.client.wizards;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.ats.client.tools.ImportTemplate;
import com.ats.platform.TimeSpan;

public class TemplatePage extends WizardPage {
	private static final Logger logger = Logger.getLogger(TemplatePage.class);
	private static final int MAX_DISPLAY_LINES = 30;
	private static final int NUM_COLS = 8;
	
	private static final String COL_DATE = "Date";
	private static final String COL_BEGIN_TIME = "Begin Time";
	private static final String COL_END_TIME = "End Time";
	private static final String COL_HIGH = "High";
	private static final String COL_LOW = "Low";
	private static final String COL_OPEN = "Open";
	private static final String COL_CLOSE = "Close";
	private static final String COL_VOL = "Volume";
	private static final String COL_NONE = "<None>";
	
	private static final String colHeaders[] = new String[]{
		COL_NONE, COL_DATE, COL_BEGIN_TIME, COL_END_TIME, COL_HIGH,
		COL_LOW, COL_OPEN, COL_CLOSE, COL_VOL};
	
	private Map<Integer, String> colSelections = new HashMap<Integer, String>();

	
	enum Delimiter {
		comma(","),
		tab("\t"),
		colon(":"),
		semi_colon(";");
		
		private String delim;
		private Delimiter(String delim) {
			this.delim = delim;
		}
		public String getDelim() {
			return delim;
		}
	}
	
	private Group csvGroup;
	private Group dataGroup;
//	private Label label2;
//	private Label label6;
	private Composite templateComposite;
	private Table previewTable;
	private Text symbolText;
	private Button cutExtensionBtn;
	private CCombo dateCombo;
	private Button dateManualBtn;
	private Button saveAsBtn;
	private Button applyBtn;
	private Combo templateCombo;
	private Button dateColBtn;
	private Combo timeSpanCombo;
//	private Label label5;
	private Combo spanCombo;
//	private Label label4;
//	private Label label3;
	private Spinner headerSpinner;
	private Combo separatorCombo;
//	private Label label1;
//	private Group symbolGroup;
//	private Group dateGroup;
	private Button useHundredsBtn;
	
	private Listener listener;
	
	private File file;

	public TemplatePage() {
		super("Template");
		setTitle("Import template");
		setDescription("Describe template which will be used to parse each row of trade data.");
		setPageComplete(false);
	}
	
	public void setFile(File file) {
		this.file = file;
		reloadTable();
	}
	
	private void reloadTable() {
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				try {
					previewTable.removeAll();
					
					
					BufferedReader reader = new BufferedReader( new FileReader(file) );
					int numLines = 0;
					String line = reader.readLine();
					while(line != null && numLines < MAX_DISPLAY_LINES) {
						TableItem item = new TableItem(previewTable, SWT.NONE);
						StringTokenizer st = new StringTokenizer(line, ""+getDelimiter());
						int index = 0;
						while(st.hasMoreTokens()) {
							item.setText(index, st.nextToken());
							index++;
						}
						numLines++;
						line = reader.readLine();
					}
					
					previewTable.showSelection();
				} catch( Exception e) {
					logger.error("Could not load table", e);
				}
			}
		});
		hideHeader();
	}
	
	private void hideHeader() {
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				int numHeaders = headerSpinner.getSelection();
				for( int i = 0; i < numHeaders; i++ ) {
					previewTable.getItem(i).setForeground(ColorConstants.gray);
					previewTable.getItem(i).setBackground(ColorConstants.yellow);
				}
			}
		});
	}
	
	public ImportTemplate getImportTemplate() {
		ImportTemplate template = new ImportTemplate();
		template.setDateFormatString(dateCombo.getText());
		template.setTimeFormatString("HH:mm");  // TODO: add to GUI?
		template.setManualDate(false);  // TODO: use check box
		template.setDelimiter(getDelimiter());
		template.setNumHeaderLines(headerSpinner.getSelection());
		template.setTimeSpan(getTimeSpan());
		template.setVolAsHundreds(useHundredsBtn.getSelection());
		
		for(int col : colSelections.keySet()) {
			String title = colSelections.get(col);
			if("Date".equals(title)) {
				template.setDateCol(col);
			} else if("Begin Time".equals(title)) {
				template.setBeginTimeCol(col);
			} else if("End Time".equals(title)) {
				template.setEndTimeCol(col);
			} else if("High".equals(title)) {
				template.setHighCol(col);
			} else if("Low".equals(title)) {
				template.setLowCol(col);
			} else if("Open".equals(title)) {
				template.setOpenCol(col);
			} else if("Close".equals(title)) {
				template.setCloseCol(col);
			} else if("Volume".equals(title)) {
				template.setVolumeCol(col);
			}
		}
		return template;
	}

	private TimeSpan getTimeSpan() {
		return Enum.valueOf(TimeSpan.class, timeSpanCombo.getText());
	}
	
	private void createListener() {
		listener = new Listener() {
			public void handleEvent(Event event) {
				checkIsComplete();
			}
		};
	}

	private void checkIsComplete() {
		String dateFormat = dateCombo.getText();
		if( dateFormat.trim().length() <= 0 ) {
			setPageComplete(false);
			return;
		} else {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(dateFormat.trim());
				setErrorMessage(null);
			} catch( IllegalArgumentException e ) {
				setPageComplete(false);
				return;
			}
		}
		
		// check to see if the table columns have all been defined
		boolean allColsDefined = colSelections.containsValue("Date") && 
			colSelections.containsValue("High") &&
			colSelections.containsValue("Low") &&
			colSelections.containsValue("Open") &&
			colSelections.containsValue("Close") &&
			colSelections.containsValue("Volume");
		if( getTimeSpan() != TimeSpan.daily ) {
			// don't need begin time with daily chart
			allColsDefined &= colSelections.containsValue("BeginTime");
		}
		
		if( ! allColsDefined ) {
			setPageComplete(false);
			return;
		}
		
		setPageComplete(true);
	}


	private String getDelimiter() {
		return Enum.valueOf(Delimiter.class, separatorCombo.getText()).getDelim();
	}
	
	public void createControl(Composite parent) {
		createListener();	
		
		
		Composite content = new Composite(parent, SWT.NONE);
		try {
			GridLayout dialogShellLayout = new GridLayout();
			dialogShellLayout.numColumns = 3;
			content.setLayout(dialogShellLayout);
			content.layout();
			content.pack();
			//content.setSize(648, 402);
			{
				csvGroup = new Group(content, SWT.NONE);
				GridLayout csvGroupLayout = new GridLayout();
				csvGroupLayout.makeColumnsEqualWidth = true;
				csvGroupLayout.numColumns = 3;
				csvGroup.setLayout(csvGroupLayout);
				csvGroup.setText("CSV");
				{
					Label label1 = new Label(csvGroup, SWT.NONE);
					label1.setText("Separator");
				}
				{
					separatorCombo = new Combo(csvGroup, SWT.READ_ONLY);
					GridData separatorComboLData = new GridData();
					separatorComboLData.horizontalSpan = 2;
					separatorCombo.setLayoutData(separatorComboLData);
					for(Delimiter delim : Delimiter.values()) {
						separatorCombo.add(delim.name());
					}
					separatorCombo.setText(Delimiter.tab.name());
					separatorCombo.addModifyListener(new ModifyListener() {
						public void modifyText(ModifyEvent e) {
							reloadTable();
						}
					});
				}
				{
					Label label2 = new Label(csvGroup, SWT.NONE);
					label2.setText("Header with");
				}
				{
					headerSpinner = new Spinner(csvGroup, SWT.BORDER);
					GridData headerSpinnerLData = new GridData();
					headerSpinnerLData.widthHint = 38;
					headerSpinnerLData.heightHint = 13;
					headerSpinner.setLayoutData(headerSpinnerLData);
					headerSpinner.setDigits(0);
					headerSpinner.addModifyListener(new ModifyListener() {
						public void modifyText(ModifyEvent e) {
							hideHeader();
						}
					});
				}
				{
					Label label3 = new Label(csvGroup, SWT.NONE);
					label3.setText("lines");
				}
			}
			{
				dataGroup = new Group(content, SWT.NONE);
				GridLayout dataGroupLayout = new GridLayout();
				dataGroupLayout.makeColumnsEqualWidth = true;
				dataGroupLayout.numColumns = 2;
				dataGroup.setLayout(dataGroupLayout);
				dataGroup.setText("Data");
				
				new Label(dataGroup, SWT.NONE).setText("Bar size");
				timeSpanCombo = new Combo(dataGroup, SWT.READ_ONLY);
				for(TimeSpan span : TimeSpan.values()) {
					timeSpanCombo.add(span.name());
				}
				timeSpanCombo.setText(TimeSpan.daily.name());
				
				useHundredsBtn = new Button(dataGroup, SWT.CHECK);
				useHundredsBtn.setText("Volume in 100's");
				GridData gdata = new GridData();
				gdata.horizontalSpan = 2;
				useHundredsBtn.setLayoutData(gdata);
				
				
			}
			{
				Group dateGroup = new Group(content, SWT.NONE);
				GridLayout dateGroupLayout = new GridLayout();
				dateGroupLayout.makeColumnsEqualWidth = true;
				dateGroup.setLayout(dateGroupLayout);
				dateGroup.setText("Date");
//				{
//					dateColBtn = new Button(dateGroup, SWT.RADIO | SWT.LEFT);
//					dateColBtn.setText("column");
//				}
//				{
//					dateManualBtn = new Button(dateGroup, SWT.RADIO | SWT.LEFT);
//					dateManualBtn.setText("manually");
//				}
				{
					new Label(dateGroup, SWT.NONE).setText("Format:");
					
					dateCombo = new CCombo(dateGroup, SWT.BORDER);
					dateCombo.addListener(SWT.Selection, listener);
					String dateFormats[] = new String[]{
							"MM/dd/yyyy",
							"dd/MM/yyyy",
							"MM/dd/yy",
							"dd/MM/yy"
					};
					for( String fmt : dateFormats ) {
						dateCombo.add(fmt);
					}
				}
			}
			{
				previewTable = new Table(content, SWT.MULTI);
				previewTable.setHeaderVisible(true);
				previewTable.setLinesVisible(true);
				
				GridData previewTableLData = new GridData();
				previewTableLData.verticalAlignment = GridData.FILL;
				previewTableLData.horizontalAlignment = GridData.FILL;
				previewTableLData.horizontalSpan = 3;
				previewTableLData.grabExcessHorizontalSpace = true;
				previewTableLData.grabExcessVerticalSpace = true;
				previewTableLData.heightHint = 100;
				previewTable.setLayoutData(previewTableLData);
				for(int i = 0; i < NUM_COLS; i++ ) {
					final TableColumn col = new TableColumn(previewTable, SWT.NONE);
					col.setData(i);
					final Menu menuBar = new Menu(getShell(), SWT.POP_UP);
					for( int colIdx = 0; colIdx < colHeaders.length; colIdx++ ) {
						addColSelectMenuItem(menuBar, col, colIdx);
					}
					menuBar.addListener(SWT.Show, new Listener() {
						public void handleEvent(Event event) {
							for( MenuItem item : menuBar.getItems()) {
								int headerIdx = (Integer)item.getData();
								item.setEnabled(! colSelections.containsValue(colHeaders[headerIdx]));
							}
						}
					});
					col.setWidth(70);
					col.setResizable(true);
					col.addSelectionListener(new SelectionListener() {
						public void widgetDefaultSelected(SelectionEvent e) {
						}
						public void widgetSelected(SelectionEvent e) {
							menuBar.setVisible(true);
						}
					});
				}
			}
			{
				templateComposite = new Composite(content, SWT.NONE);
				GridLayout templateCompositeLayout = new GridLayout();
				templateCompositeLayout.makeColumnsEqualWidth = true;
				templateCompositeLayout.numColumns = 4;
				GridData templateCompositeLData = new GridData();
				templateCompositeLData.horizontalSpan = 3;
				//templateCompositeLData.horizontalAlignment = GridData.FILL;
				templateCompositeLData.grabExcessHorizontalSpace = true;
				templateComposite.setLayoutData(templateCompositeLData);
				templateComposite.setLayout(templateCompositeLayout);
				{
					Label label6 = new Label(templateComposite, SWT.NONE);
					label6.setText("Template");
				}
				{
					templateCombo = new Combo(templateComposite, SWT.NONE);
					try {
						File baseFile = new File(".");
						String names[] = baseFile.list(new FilenameFilter() {
							public boolean accept(File dir, String name) {
								return name.endsWith(".tpl");
							}
						});
						for(String name : names ) {
							templateCombo.add(name);
						}
					} catch( Exception e) {
						logger.error("Could not load initial template list", e);
					}
					
				}
				{
					applyBtn = new Button(templateComposite, SWT.PUSH
						| SWT.CENTER);
					applyBtn.setText("Apply");
					applyBtn.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event event) {
							final String templateName = templateCombo.getText();
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									try {
										ObjectInputStream ois = new ObjectInputStream(new FileInputStream(templateName));
										ImportTemplate template = (ImportTemplate)ois.readObject();
										
										// set fields based on this
										// set separator
										String separator = template.getDelimiter();
										for(Delimiter d : Delimiter.values()) {
											if( d.delim.equals(separator)) {
												separatorCombo.setText(d.toString());
												break;
											}
										}
										
										headerSpinner.setSelection(template.getNumHeaderLines());
										timeSpanCombo.setText(template.getTimeSpan().toString());
										useHundredsBtn.setSelection(template.getVolAsHundreds());
										dateCombo.setText(template.getDateFormatString());
										
										// handle column headers
										colSelections.clear();
										colSelections.put(template.getBeginTimeCol(), COL_BEGIN_TIME);
										colSelections.put(template.getCloseCol(), COL_CLOSE);
										colSelections.put(template.getHighCol(), COL_HIGH);
										colSelections.put(template.getLowCol(), COL_LOW);
										colSelections.put(template.getOpenCol(), COL_OPEN);
										colSelections.put(template.getDateCol(), COL_DATE);
										colSelections.put(template.getEndTimeCol(), COL_END_TIME);
										colSelections.put(template.getVolumeCol(), COL_VOL);
										for(int colIdx : colSelections.keySet()) {
											if( colIdx >= 0 ) {
												logger.debug("setting col " + colIdx + " to " + colSelections.get(colIdx));
												previewTable.getColumn(colIdx).setText(colSelections.get(colIdx));
											}
										}

										
									} catch( Exception e) {
										logger.error("Could not load template: " + templateName, e );
									}
									checkIsComplete();
									getControl().redraw();
									
								}
							});
						}
					});
				}
				{
					saveAsBtn = new Button(templateComposite, SWT.PUSH
						| SWT.CENTER);
					saveAsBtn.setText("Save As...");
					saveAsBtn.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event evt) {
							InputDialog dialog = new InputDialog(getShell(), 
									"Template Name", "What name will you use for this template?", "", null);
							if( dialog.open() == Window.OK ) {
								String name = dialog.getValue();
								try {
									ImportTemplate template = getImportTemplate();
									File file = new File(name + ".tpl");
									ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
									oos.writeObject(template);
									oos.flush();
									oos.close();
									logger.debug("Wrote template: " + file.getAbsolutePath());
									templateCombo.add(name + ".tpl");
									templateCombo.setText(name + ".tpl");
								} catch( Exception e) {
									// TODO: warning dialog
								}
							}
						}
					});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		setControl(content);
	}

	private void addColSelectMenuItem(final Menu menuBar, final TableColumn col, final int colNameIdx ) {
		col.setText(colHeaders[0]);
		MenuItem item = new MenuItem(menuBar, SWT.PUSH);
		item.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						col.setText(colHeaders[colNameIdx]);
						if( colNameIdx == 0 ) {
							colSelections.remove((Integer)col.getData());
						} else {
							colSelections.put((Integer)col.getData(), colHeaders[colNameIdx]);
						}
						checkIsComplete();
					}
				});
			}
		});
		item.setText(colHeaders[colNameIdx]);
		item.setData(colNameIdx);
	}
}

