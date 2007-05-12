package com.ats.client.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import com.ats.client.views.OptimizationView;

public class OptimizeAction extends Action implements ISelectionListener,
		IWorkbenchAction {
	
	public static final String ID = "com.ats.client.actions.optimizeAction";
	
	private static final Logger logger = Logger.getLogger(OptimizeAction.class);
	private static int id = 1;
	private final IWorkbenchWindow window;
	
	public OptimizeAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText("Optimize");
		setToolTipText("Optimize Strategy");
	}

	public void run() {
		try {
			window.getActivePage().showView(OptimizationView.ID, Integer.toString(id++), IWorkbenchPage.VIEW_ACTIVATE);
		} catch (PartInitException e) {
			logger.error("Could not instantiate optimizer view", e);
		}

//		OptimizeWizard wiz = new OptimizeWizard();
//		WizardDialog dlg = new WizardDialog(window.getShell(), wiz);
//		if( dlg.open() == WizardDialog.OK ) {
//			OptimizationInput input = new OptimizationInput(wiz.getStrategyDefinition(), wiz.getParamValues());
//			IWorkbenchPage page = window.getActivePage();
//			try {
//				page.openEditor(input, OptimizationEditor.ID);
//			} catch( PartInitException e) {
//				logger.error("Could not open optimization editor", e);
//			}
//		}
	}
	
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// TODO check for selection of a strategy

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

}
