package com.ats.client.views;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.gef.internal.InternalImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.part.ViewPart;

import com.ats.client.Activator;
import com.ats.client.dialogs.AddStrategyDialog;
import com.ats.client.dialogs.SelectInstrumentDialog;
import com.ats.client.perspectives.BacktestPerspective;
import com.ats.client.wizards.DownloadHistDataWizard;
import com.ats.client.wizards.NewStrategyWizard;
import com.ats.client.wizards.OptimizeWizard;
import com.ats.db.PlatformDAO;
import com.ats.engine.BacktestFactory;
import com.ats.engine.StrategyDefinition;
import com.ats.platform.Instrument;
import com.ats.platform.Instrument.InstrumentType;

public class StrategyView extends ViewPart implements ISelectionProvider {
	private static final Logger logger = Logger.getLogger(StrategyView.class);
	
	public static final String ID = "com.ats.client.views.strategyView";
	private TreeViewer viewer;
	private Action addStrategyAction;
	private Action addInstrumentAction;
	private Action runStrategyAction;
	private Action optimizeStrategyAction;
	private Action deleteInstrumentAction;
	private Action runtimeAction;
	private List<StrategyDefinition> strategies;
	
	private TreeParent root;
	

	
    /**
     * We will set up a dummy model to initialize tree heararchy. In real
     * code, you will connect to a real model and expose its hierarchy.
     */
    private TreeObject createDummyModel() {
    	strategies = PlatformDAO.getAllStrategyDefinitions();
    	
        root = buildTree();
        return root;
    }

	private TreeParent buildTree() {
		TreeParent root = new TreeParent("root");
		
		for(StrategyDefinition strategy : strategies ) {
			TreeParent stratParent = new TreeParent(strategy);
			root.addChild(stratParent);
			for( Instrument instrument : strategy.getInstruments() ) {
				addInstrumentNode(stratParent, instrument);
			}
		}
		return root;
	}
	
	private StrategyDefinition getSelectedStrategy() {
		TreeViewContentProvider provider = (TreeViewContentProvider)viewer.getContentProvider();
		Object sel = viewer.getSelection();
		sel = ((TreeSelection)sel).getFirstElement();
		while( sel != null && !(((TreeObject)sel).getObject() instanceof StrategyDefinition) ) {
			sel = provider.getParent(sel);
		}
		if( sel == null || !(((TreeObject)sel).getObject() instanceof StrategyDefinition) ) {
			// couldn't find selection
			return null;
		}
		StrategyDefinition strat = (StrategyDefinition)((TreeObject)sel).getObject();
		return strat;
	}

	private void addInstrumentNode(TreeParent parent, Instrument instrument) {
		InstrumentType type = instrument.getInstrumentType();
		TreeParent typeNode = (TreeParent)parent.getChild(type);
		if( typeNode == null ) {
			typeNode = new TreeParent(type);
			parent.addChild(typeNode);
		}
		TreeObject objNode = typeNode.getChild(instrument);
		if( objNode == null ) {
			objNode = new TreeObject(instrument);
			typeNode.addChild(objNode);
		}
	}
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		
		getSite().setSelectionProvider(this);
		
	}
	
	private void createActions() {

		addInstrumentAction = new Action() {
			public void run() {
				IStructuredSelection structSel = (IStructuredSelection)viewer.getSelection();
				
				TreeObject sel = (TreeObject)structSel.getFirstElement();
				while( sel != null && ! ( sel.getObject() instanceof StrategyDefinition ) ) {
					sel = sel.getParent();
				}
				if( sel == null ) {
					logger.debug("Could not locate StrategyDefinition parent");
					return;
				}
				final StrategyDefinition stratDef = (StrategyDefinition)sel.getObject();
				final TreeParent stratDefNode = (TreeParent)sel; 

				SelectInstrumentDialog dlg = new SelectInstrumentDialog(viewer.getTree().getShell());
				if( dlg.open() == Dialog.OK ) {
					final List<Instrument> selInstr = dlg.getSelectedInstruments();
					if( selInstr == null ) {
						return;
					}
					for( Instrument instr : selInstr ) {
						stratDef.addInstrument(instr);
						PlatformDAO.addInstrumentToStrategy(stratDef, instr);
					}
					
					Display.getCurrent().asyncExec(new Runnable() {
						public void run() {
							for( Instrument instr : selInstr ) {
								addInstrumentNode(stratDefNode, instr);
							}
							viewer.refresh(stratDefNode);
						}
					});
				}
			}
		};
		addInstrumentAction.setText("Add instrument...");
		
		addStrategyAction = new Action() {
			public void run() {
				NewStrategyWizard wiz = new NewStrategyWizard();
				WizardDialog dlg = new WizardDialog(viewer.getControl().getShell(), wiz);
				if( dlg.open() == WizardDialog.OK ) {
                	final StrategyDefinition strategy = wiz.getStrategyDefinition();
                	if( strategy == null ) {
                		return;
                	}
                	PlatformDAO.insertStrategyDefinition(strategy);
            		Display.getDefault().asyncExec(new Runnable() {
            			public void run() {
            				strategies.add(strategy);
            				if (viewer == null) {
            					return;
            				}
            				TreeParent stratParent = new TreeParent(strategy);
            				root.addChild(stratParent);
            				viewer.refresh(root);
            			}
            		});
				}

			}
		};
		addStrategyAction.setText("Add strategy...");
		
		deleteInstrumentAction = new Action() {
			public void run() {
				//TreeViewContentProvider provider = (TreeViewContentProvider)viewer.getContentProvider();
				TreeSelection treeSel = (TreeSelection)viewer.getSelection();
				Iterator it = treeSel.iterator();
				while( it.hasNext() ) {
					TreeObject sel = (TreeObject)it.next();
					if( sel.getObject() instanceof Instrument ) {
						Instrument instr = (Instrument)sel.getObject();
						StrategyDefinition stratDef = (StrategyDefinition)sel.getParent().getParent().getObject();
						stratDef.removeInstrument(instr);
						PlatformDAO.deleteInstrumentFromStrategy(stratDef, instr);
						TreeParent parent = sel.getParent(); 
						parent.removeChild(sel);
						if( ! parent.hasChildren() ) {
							TreeParent tmp = parent.getParent(); 
							tmp.removeChild(parent);
							parent = tmp;
						}
						final TreeParent toRefresh = parent;
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								viewer.refresh(toRefresh);
							}
						});
					}
				}
			}
		};
		deleteInstrumentAction.setText("Delete instruments");

		runtimeAction = new Action() {
			public void run() {
				TreeSelection treeSel = (TreeSelection)viewer.getSelection();
				if( treeSel != null && treeSel.size() > 0 ) {
					final TreeObject obj = (TreeObject)treeSel.getFirstElement();
					if( obj.getObject() instanceof StrategyDefinition ) {
						StrategyDefinition strat = (StrategyDefinition)obj.getObject();
						if( strat == null ) {
							return;
						}
						strat.setRuntime(!strat.isRuntime());
						PlatformDAO.updateStrategyDefinition(strat);
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								viewer.refresh(obj);
							}
						});
					}
				}
			}
		};
        runtimeAction.setText("Add to/Remove from runtime");

		
		runStrategyAction = new Action() {
			public void run() {
				StrategyDefinition strat = getSelectedStrategy();
				if( strat == null ) {
					return;
				}
				BacktestFactory.runBacktest(strat);
				try {
					// go to the backtest perspective
					PlatformUI.getWorkbench().showPerspective(BacktestPerspective.ID, 
							PlatformUI.getWorkbench().getActiveWorkbenchWindow());
				} catch (WorkbenchException e) {
					logger.debug("Could not change to the backtest perspective", e);
				}
			}
		};
        runStrategyAction.setText("Run strategy...");
        runStrategyAction.setImageDescriptor(Activator.getImageDescriptor("/icons/tsuiterun.gif"));

		optimizeStrategyAction = new Action() {
			public void run() {
				try {
					StrategyDefinition strat = getSelectedStrategy();
					if( strat == null ) {
						return;
					}
					OptimizeWizard wiz = new OptimizeWizard();
					WizardDialog dlg = new WizardDialog(viewer.getControl().getShell(), wiz);
					if( dlg.open() == WizardDialog.OK ) {
						// TODO: optimize!
					}
				} catch( Exception e) {
					logger.error("Could not open optimize wizard", e);
				}
			}
		};
        optimizeStrategyAction.setText("Optimize strategy...");

        MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager menuManager)
            {
            	menuManager.add(addStrategyAction);
            	menuManager.add(new Separator());
            	menuManager.add(addInstrumentAction);
            	menuManager.add(deleteInstrumentAction);
            	menuManager.add(runtimeAction);
            	menuManager.add(new Separator());
            	menuManager.add(optimizeStrategyAction);
            	menuManager.add(runStrategyAction);
            }
        });
        viewer.getTree().setMenu(menuMgr.createContextMenu(viewer.getTree()));

		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		mgr.add(runStrategyAction);
	}

	/**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
	public void createPartControl(Composite parent) {
		
		viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(new TreeViewContentProvider());
		viewer.setLabelProvider(new TreeViewLabelProvider());
		viewer.setInput(createDummyModel());

		createActions();

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		viewer.addSelectionChangedListener(listener);
	}


	public ISelection getSelection() {
		return viewer.getSelection();
	}


	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		viewer.removeSelectionChangedListener(listener);
	}


	public void setSelection(ISelection selection) {
		// no reversies
	}
}