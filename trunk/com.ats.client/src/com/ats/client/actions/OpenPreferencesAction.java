package com.ats.client.actions;

import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.ui.IWorkbenchWindow;

import com.ats.client.ICommandIds;
import com.ats.client.preferences.BacktestOrderPage;
import com.ats.client.preferences.DatabasePage;
import com.ats.client.preferences.ProviderPage;
import com.ats.utils.Utils;

public class OpenPreferencesAction extends Action {
    private final IWorkbenchWindow window;

    public OpenPreferencesAction(String text, IWorkbenchWindow window) {
        super(text);
        this.window = window;
        // The id is used to refer to the action in a menu or toolbar
        setId(ICommandIds.CMD_OPEN_PREFERENCES);
        // Associate the action with a pre-defined command, to allow key bindings.
        setActionDefinitionId(ICommandIds.CMD_OPEN_PREFERENCES);
//        setImageDescriptor(com.ats.client.Activator.getImageDescriptor("/icons/sample3.gif"));
    }



	public void run() {
		
		PreferenceManager mgr = new PreferenceManager();
		
		PreferenceNode orderNode = new PreferenceNode("orders", new BacktestOrderPage());
		PreferenceNode providerNode = new PreferenceNode("providers", new ProviderPage());
		PreferenceNode databaseNode = new PreferenceNode("database", new DatabasePage());
		
		mgr.addToRoot(orderNode);
		mgr.addToRoot(providerNode);
		mgr.addToRoot(databaseNode);
		
		PreferenceDialog dlg = new PreferenceDialog(window.getShell(), mgr);
		
		dlg.setPreferenceStore(Utils.getPreferenceStore());
		
		dlg.open();
		
		try {
			Utils.getPreferenceStore().save();
		} catch( IOException e) {
			// TODO: log error
			e.printStackTrace();
		}
	}

}
