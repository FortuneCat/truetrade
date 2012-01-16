package com.ats.client.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.internal.progress.ProgressView;

import com.ats.client.views.ChartView;
import com.ats.client.views.EquityView;
import com.ats.client.views.ExecutionView;
import com.ats.client.views.JFreeChartView;
import com.ats.client.views.StrategyResultsView;
import com.ats.client.views.StrategySummaryView;
import com.ats.client.views.TradeView;

public class BacktestPerspective implements IPerspectiveFactory {
	public static final String ID = "com.ats.client.perspectives.backtestPerspective";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		IFolderLayout folder = layout.createFolder("watchlistView",
				IPageLayout.LEFT, 0.25f, editorArea);
		folder.addView(StrategyResultsView.ID);

		folder = layout.createFolder("strategyView", IPageLayout.TOP, 0.75f,
				editorArea);
		folder.addView(ChartView.ID);
		//folder.addView(JFreeChartView.ID);
		folder.addView(EquityView.ID);
		folder.addView(StrategySummaryView.ID);
		
		folder = layout.createFolder("execDetails", IPageLayout.BOTTOM, 0.25F, editorArea);
		folder.addView(ExecutionView.ID);
		folder.addView(TradeView.ID);
		folder.addView("org.eclipse.ui.views.ProgressView");
		
		layout.addPerspectiveShortcut(DataPerspective.ID);

	}

}
