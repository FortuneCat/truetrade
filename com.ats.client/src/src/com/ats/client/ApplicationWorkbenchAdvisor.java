package com.ats.client;

import java.sql.DriverManager;

import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.ats.client.perspectives.BacktestPerspective;
import com.ats.client.perspectives.DataPerspective;
import com.ats.utils.Utils;

/**
 * This workbench advisor creates the window advisor, and specifies
 * the perspective id for the initial window.
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {
	
    @Override
	public void postShutdown() {
		super.postShutdown();
		if( Utils.getPreferenceStore().getBoolean(Utils.DB_USE_DEFAULT)) {
			// shut down all derby instances
			try {
				DriverManager.getConnection("jdbc:derby:;shutdown=true");
			} catch( Exception e) {
				// This always throws a SQL Exception, so ignore it
			}
		}
	}

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

	public String getInitialWindowPerspectiveId() {
		return DataPerspective.ID;
	} 
	
}
