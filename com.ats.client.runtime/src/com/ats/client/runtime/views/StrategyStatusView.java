package com.ats.client.runtime.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import com.ats.engine.StrategyDefinition;

public class StrategyStatusView extends ViewPart {

	public static final String ID = "com.ats.client.runtime.views.strategyStatusView";
	
	private Table positionTable;
	private Table executionTable;
	
	private StrategyDefinition strategyDefinition;
	
	public void setStrategyDefinition(StrategyDefinition stratDef) {
		this.strategyDefinition = stratDef;
	    this.setPartName(strategyDefinition.getStrategyClass().getSimpleName());
	}

	@Override
	public void createPartControl(Composite parent) {
		
		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		
		TabItem posItem = new TabItem(tabFolder, SWT.NONE);
		posItem.setText("Positions");
		
		Composite posComp = new Composite(tabFolder, SWT.NONE);
	    posComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	    posComp.setLayout(new GridLayout());
	    posItem.setControl(posComp);
	    
	    positionTable = new Table(posComp, SWT.MULTI | SWT.FULL_SELECTION);
	    positionTable.setHeaderVisible(true);
	    positionTable.setLinesVisible(true);
	    positionTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	    
	    
	    TableColumn column = new TableColumn(positionTable, SWT.NONE);
	    column.setText("Symb");
	    column.setWidth(60);
	    
	    column = new TableColumn(positionTable, SWT.NONE);
	    column.setText("Dir");
	    column.setWidth(60);
	    
	    column = new TableColumn(positionTable, SWT.NONE);
	    column.setText("Size");
	    column.setWidth(60);
	
	    column = new TableColumn(positionTable, SWT.NONE);
	    column.setText("Avg Cost");
	    column.setWidth(60);
	    
	    column = new TableColumn(positionTable, SWT.NONE);
	    column.setText("UnrP&L");
	    column.setWidth(60);
	    
	    column = new TableColumn(positionTable, SWT.NONE);
	    column.setText("RealP&L");
	    column.setWidth(60);
	
	    
		TabItem execItem = new TabItem(tabFolder, SWT.NONE);
		execItem.setText("Executions");
	
		Composite execComp = new Composite(tabFolder, SWT.NONE);
		execComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		execComp.setLayout(new GridLayout());
		execItem.setControl(execComp);
	
	    executionTable = new Table(execComp, SWT.MULTI | SWT.FULL_SELECTION);
	    executionTable.setHeaderVisible(true);
	    executionTable.setLinesVisible(true);
	    executionTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	
	    column = new TableColumn(executionTable, SWT.NONE);
	    column.setText("Symb");
	    column.setWidth(60);
	    
	    column = new TableColumn(executionTable, SWT.NONE);
	    column.setText("Dir");
	    column.setWidth(60);
	    
	    column = new TableColumn(executionTable, SWT.NONE);
	    column.setText("Size");
	    column.setWidth(60);
	    
	    column = new TableColumn(executionTable, SWT.NONE);
	    column.setText("Cost");
	    column.setWidth(60);
	    
	    column = new TableColumn(executionTable, SWT.NONE);
	    column.setText("Time");
	    column.setWidth(60);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}
