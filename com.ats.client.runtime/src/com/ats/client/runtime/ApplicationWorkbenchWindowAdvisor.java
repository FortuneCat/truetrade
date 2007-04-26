package com.ats.client.runtime;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.ats.client.runtime.views.StrategyStatusView;
import com.ats.db.PlatformDAO;
import com.ats.engine.StrategyDefinition;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }
    
    @Override
    public void postWindowOpen() {
        int instanceNum = 0;
		for(StrategyDefinition stratDef : PlatformDAO.getAllStrategyDefinitions()) {
			if( stratDef.isRuntime() ) {
				// create a view for it
				IViewPart view;
				try {
					view = getWindowConfigurer().getWindow().getActivePage().showView(
							StrategyStatusView.ID, Integer.toString(instanceNum++), IWorkbenchPage.VIEW_ACTIVATE);
					((StrategyStatusView)view).setStrategyDefinition(stratDef);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

    }
    
    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(600, 400));
        configurer.setShowCoolBar(false);
        configurer.setShowStatusLine(false);
    }
    
}
