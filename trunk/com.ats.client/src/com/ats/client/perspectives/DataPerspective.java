package com.ats.client.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.internal.progress.ProgressView;

import com.ats.client.views.AvailableDataView;
import com.ats.client.views.InstrumentView;
import com.ats.client.views.OptimizationView;
import com.ats.client.views.StrategyView;

public class DataPerspective implements IPerspectiveFactory {
	public static final String ID = "com.ats.client.perspectives.dataPerspective";

	public void createInitialLayout(IPageLayout layout) {
		//layout.addPerspectiveShortcut(BacktestPerspective.ID);
		
		String editorArea = layout.getEditorArea();

		layout.setEditorAreaVisible(false);
		IFolderLayout folder = layout.createFolder("watchlistView", IPageLayout.LEFT, 0.20f, editorArea);
		folder.addView(InstrumentView.ID);
		
		folder = layout.createFolder("strategyView", IPageLayout.RIGHT, 0.75f, editorArea);
		folder.addView(StrategyView.ID);

		 // Bottom left: Outline view and Property Sheet view
		 folder = layout.createFolder("bottomRight", IPageLayout.BOTTOM, 0.65f,
		 	   editorArea);
		 folder.addView(IPageLayout.ID_PROP_SHEET);
		 folder.addView("org.eclipse.ui.views.ProgressView");
		
		
		folder = layout.createFolder("availableDataView", IPageLayout.TOP, 0.8f, editorArea);
		folder.addView(AvailableDataView.ID);
		folder.addPlaceholder(OptimizationView.ID + ":*");
		//folder.addView(OptimizationView.ID);
		
		//layout.addStandaloneView(ContractView.ID,  false, IPageLayout.RIGHT, 0.25f, editorArea);

		
//		layout.getViewLayout(ContractView.ID).setCloseable(true);
//		layout.getViewLayout(StrategyView.ID).setCloseable(true);
		
		
		layout.addPerspectiveShortcut(BacktestPerspective.ID);
	}
	
}
