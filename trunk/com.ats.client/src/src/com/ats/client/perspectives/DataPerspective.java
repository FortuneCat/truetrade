package com.ats.client.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.ats.client.views.AvailableDataView;
import com.ats.client.views.InstrumentView;
import com.ats.client.views.StrategyView;

public class DataPerspective implements IPerspectiveFactory {
	public static final String ID = "com.ats.client.perspectives.dataPerspective";

	public void createInitialLayout(IPageLayout layout) {
		//layout.addPerspectiveShortcut(BacktestPerspective.ID);
		
		String editorArea = layout.getEditorArea();
/*		
//		 Top left: Resource Navigator view and Bookmarks view placeholder
		 IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.25f,
		    editorArea);
		 topLeft.addView(IPageLayout.ID_RES_NAV);
		 topLeft.addPlaceholder(IPageLayout.ID_BOOKMARKS);

		 // Bottom left: Outline view and Property Sheet view
		 IFolderLayout bottomLeft = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, 0.50f,
		 	   "topLeft");
		 bottomLeft.addView(IPageLayout.ID_OUTLINE);
		 bottomLeft.addView(IPageLayout.ID_PROP_SHEET);

		 // Bottom right: Task List view
		 layout.addView(IPageLayout.ID_TASK_LIST, IPageLayout.BOTTOM, 0.66f, editorArea);
*/		 
		
		layout.setEditorAreaVisible(false);
		IFolderLayout folder = layout.createFolder("watchlistView", IPageLayout.LEFT, 0.25f, editorArea);
		folder.addView(InstrumentView.ID);
		
		folder = layout.createFolder("strategyView", IPageLayout.RIGHT, 0.70f, editorArea);
		folder.addView(StrategyView.ID);

		 // Bottom left: Outline view and Property Sheet view
		 folder = layout.createFolder("bottomRight", IPageLayout.BOTTOM, 0.65f,
		 	   editorArea);
		 folder.addView(IPageLayout.ID_PROP_SHEET);
		
		
		folder = layout.createFolder("availableDataView", IPageLayout.TOP, 0.8f, editorArea);
		folder.addView(AvailableDataView.ID);
		
		//layout.addStandaloneView(ContractView.ID,  false, IPageLayout.RIGHT, 0.25f, editorArea);

		
//		layout.getViewLayout(ContractView.ID).setCloseable(true);
//		layout.getViewLayout(StrategyView.ID).setCloseable(true);
		
		
		layout.addPerspectiveShortcut(BacktestPerspective.ID);
	}
	
}
