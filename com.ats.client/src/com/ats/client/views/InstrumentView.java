package com.ats.client.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.ats.client.dialogs.AddContractDialog;
import com.ats.client.wizards.DownloadHistDataWizard;
import com.ats.client.wizards.ImportDataWizard;
import com.ats.db.PlatformDAO;
import com.ats.platform.Instrument;
import com.ats.platform.Instrument.InstrumentType;

public class InstrumentView extends ViewPart implements ISelectionProvider {
	private static final Logger logger = Logger.getLogger(InstrumentView.class);
	public static final String ID = "com.ats.client.views.instrumentView";
	private TreeViewer viewer;
	
	private Action addContractAction;
	private Action importDataAction;
	private Action downloadDataAction;
	private Action deleteAction;
	
	private List<Instrument> instruments;
	private List<ISelectionChangedListener> selListeners = new ArrayList<ISelectionChangedListener>();
	
	private TreeParent root;
	
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		

		instruments = PlatformDAO.getAllInstruments();
		
		getSite().setSelectionProvider(this);
	}

	private void createActions() {
		
		addContractAction = new Action() {
			public void run() {
//				IStructuredSelection structSel = (IStructuredSelection)viewer.getSelection();
//				
//				TreeObject sel = (TreeObject)structSel.getFirstElement();
                AddContractDialog dlg = new AddContractDialog(viewer.getTree().getShell());
                if (dlg.open() == Dialog.OK) {
                	final Instrument instrument = dlg.getInstrument();
                	PlatformDAO.insertInstrument(instrument);
            		Display.getDefault().asyncExec(new Runnable() {
            			public void run() {
            				buildTree();
            				addInstrumentNode(root, instrument);
//            				if (viewer == null) {
//            					return;
//            				}
            				viewer.refresh(root);
            				fireSelectionChanged(new SelectionChangedEvent(InstrumentView.this, null));
            			}
            		});

                }
			}
		};
		addContractAction.setText("Add Instrument...");
		
		deleteAction = new Action() {
			public void run() {
				List<Instrument> selInstr = getSelectedInstruments();
				if( selInstr.size() > 0 ) {
					String message = "Are you sure you wish to delete the\n"
						+ "following instruments:\n";
					for( Instrument instr : selInstr ) {
						message += "\n    " + instr.getSymbol();
					}
					boolean confirm = MessageDialog.openQuestion(viewer
							.getTree().getShell(), "Confirm Delete", message);
					if( confirm ) {
						// delete
						for(Instrument instr : selInstr ) {
							try {
								PlatformDAO.deleteInstrument(instr);
							} catch( Exception e ) {
								logger.error("Could not delete instrument: " + instr.getSymbol());
							}
						}
						// could locate each individual node and delete it, but this is sure a whole
						// lot easier, and there are no slip-ups
						viewer.setInput(buildTree());
						
						// notify listeners that the input has changed
        				fireSelectionChanged(new SelectionChangedEvent(InstrumentView.this, null));
					}
				}
			}

		};
		deleteAction.setText("Delete Instrument...");
		
		importDataAction = new Action() {
			public void run() {
				ImportDataWizard wiz = new ImportDataWizard();
				List<Instrument> insts = getSelectedInstruments();
				if( insts.size() > 0 ) {
					wiz.setInstrument(insts.get(0));
				}
				WizardDialog dlg = new WizardDialog(viewer.getTree().getShell(), wiz);
				dlg.open();
				fireSelectionChanged(new SelectionChangedEvent(InstrumentView.this, viewer.getSelection()));
			}
		};
		importDataAction.setText("Import data...");
		
		downloadDataAction = new Action() {
			public void run() {
				DownloadHistDataWizard wiz = new DownloadHistDataWizard();
				wiz.setSelectedInstruments(getSelectedInstruments());
				WizardDialog dlg = new WizardDialog(viewer.getTree().getShell(), wiz);
				dlg.open();
				fireSelectionChanged(new SelectionChangedEvent(InstrumentView.this, viewer.getSelection()));
			}
		};
		downloadDataAction.setText("Download historical data...");
		

        MenuManager menuMgr = new MenuManager("#popupMenu", "popupMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager menuManager)
            {
            	menuManager.add(addContractAction);
            	menuManager.add(deleteAction);
            	menuManager.add(new Separator());
            	menuManager.add(importDataAction);
            	menuManager.add(downloadDataAction);
            }
        });
        viewer.getControl().setMenu(menuMgr.createContextMenu(viewer.getControl()));
	}

	private List<Instrument> getSelectedInstruments() {
		IStructuredSelection structSel = (IStructuredSelection)viewer.getSelection();
		List<Instrument> selInstr = new ArrayList<Instrument>();
		Iterator iter = structSel.iterator();
		while( iter.hasNext() ) {
			TreeObject sel = (TreeObject)iter.next();
			if( sel.getObject() instanceof Instrument ) {
				selInstr.add((Instrument)sel.getObject());
			}
		}
		return selInstr;
	}

	/**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
	public void createPartControl(Composite parent) {
		
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(new TreeViewContentProvider());
		viewer.setLabelProvider(new TreeViewLabelProvider());
		viewer.setInput(buildTree());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				fireSelectionChanged(event);
			}
		});
		
		createActions();
	}
	
	private void fireSelectionChanged(SelectionChangedEvent event) {
		synchronized(selListeners) {
			for(ISelectionChangedListener l : selListeners) {
				l.selectionChanged(event);
			}
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	private TreeParent buildTree() {
		root = new TreeParent("root");
		instruments = PlatformDAO.getAllInstruments();
		
		for( Instrument instrument : instruments ) {
			addInstrumentNode(root, instrument);
		}
		return root;
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

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		synchronized(selListeners) {
			//viewer.addSelectionChangedListener(listener);
			selListeners.add(listener);
		}
	}


	public ISelection getSelection() {
		return viewer.getSelection();
	}


	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		synchronized(selListeners) {
			//viewer.addSelectionChangedListener(listener);
			selListeners.remove(listener);
		}
	}


	public void setSelection(ISelection selection) {
		// no reversies
	}

}