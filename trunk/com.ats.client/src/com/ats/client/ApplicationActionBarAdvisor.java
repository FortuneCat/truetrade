package com.ats.client;

import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import com.ats.client.actions.OpenPreferencesAction;
import com.ats.client.actions.OptimizeAction;
import com.ats.client.preferences.BacktestOrderPage;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of the
 * actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    // Actions - important to allocate these only in makeActions, and then use them
    // in the fill methods.  This ensures that the actions aren't recreated
    // when fillActionBars is called with FILL_PROXY.
    private IWorkbenchAction exitAction;
    private IWorkbenchAction aboutAction;
    private Action preferencesAction;
    private IWorkbenchAction helpAction;
    private IWorkbenchAction optimizeAction;
//    private IWorkbenchAction newWindowAction;
//    private Action messagePopupAction;
    

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }
    
    protected void makeActions(final IWorkbenchWindow window) {
        // Creates the actions and registers them.
        // Registering is needed to ensure that key bindings work.
        // The corresponding commands keybindings are defined in the plugin.xml file.
        // Registering also provides automatic disposal of the actions when
        // the window is closed.

        exitAction = ActionFactory.QUIT.create(window);
        register(exitAction);
        
        aboutAction = ActionFactory.ABOUT.create(window);
        register(aboutAction);

        preferencesAction = new OpenPreferencesAction("Preferences...", window);
       	register(preferencesAction);
       	
       	helpAction = ActionFactory.HELP_CONTENTS.create(window);
       	register(helpAction);
       	
       	optimizeAction = new OptimizeAction(window);
       	register(optimizeAction);
        
//        newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(window);
//        register(newWindowAction);
//        
//        
//        messagePopupAction = new MessagePopupAction("Open Message", window);
//        register(messagePopupAction);
    }
    
    protected void fillMenuBar(IMenuManager menuBar) {
        MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
        MenuManager windowMenu = new MenuManager("&Window", IWorkbenchActionConstants.M_WINDOW );
        MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
        
        menuBar.add(fileMenu);
        menuBar.add(windowMenu);
        // Add a group marker indicating where action set menus will appear.
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(helpMenu);
        
        // File
//        fileMenu.add(newWindowAction);
//        fileMenu.add(new Separator());
//        fileMenu.add(messagePopupAction);
//        fileMenu.add(new Separator());
        fileMenu.add(exitAction);
        
        // Window
        windowMenu.add(optimizeAction);
        windowMenu.add(preferencesAction);
        
        // Help
        helpMenu.add(helpAction);
        helpMenu.add(aboutAction);
    }
    
    protected void fillCoolBar(ICoolBarManager coolBar) {
        IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
        coolBar.add(new ToolBarContributionItem(toolbar, "main"));   
    }
}
