package com.ats.client.runtime;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.ats.client.runtime.views.MessagesView;
import com.ats.client.runtime.views.StrategyStatusView;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		
		IFolderLayout folder = layout.createFolder("messageView", IPageLayout.BOTTOM, 0.65f, editorArea);
		folder.addView(MessagesView.ID);
		layout.getViewLayout(MessagesView.ID).setCloseable(false);
		
		
		folder = layout.createFolder("messages", IPageLayout.TOP, 0.75f, editorArea);
		folder.addPlaceholder(StrategyStatusView.ID + ":*");
		//folder.addView(StrategyStatusView.ID);
		

		//layout.getViewLayout(StrategyStatusView.ID).setCloseable(false);
		

	}
}
