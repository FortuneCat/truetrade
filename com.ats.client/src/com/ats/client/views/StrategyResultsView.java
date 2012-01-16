package com.ats.client.views;

import static com.ats.engine.PositionManager.PROP_ADD_POSITION;
import static com.ats.engine.PositionManager.PROP_ADD_STRATEGY;
import static com.ats.engine.PositionManager.PROP_EXECUTION;
import static com.ats.engine.PositionManager.PROP_RESET;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.ats.engine.PositionManager;
import com.ats.engine.TradeSummary;
import com.ats.platform.Position;
import com.ats.platform.Strategy;

public class StrategyResultsView extends ViewPart implements ISelectionProvider {
	private static final Logger logger = Logger.getLogger(StrategyResultsView.class);
	
	public static final String ID = "com.ats.client.views.strategyResultsView";
//	private ListViewer viewer;
//	private List<Position> positions = new ArrayList<Position>();
	private TreeViewer treeViewer;
	
	private List<ISelectionChangedListener> selectionListeners = new ArrayList<ISelectionChangedListener>();
	
	private TreeParent root;
	private TreeParent currExecNode;
	private PositionManager posMgr = PositionManager.getInstance();
	
	
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);

		root = new TreeParent(posMgr);
		if( ! posMgr.getAllPositions().isEmpty() ) {
			// there is some existing data, so populate
			currExecNode = new TreeParent(new Date());
			root.addChild(currExecNode);
			for(Strategy strat : posMgr.getAllStrategies() ) {
				TreeParent stratNode = null;
				for(TreeObject child : currExecNode.getChildren()) {
					if(((Class)child.getObject()).equals(strat.getClass())) {
						stratNode = (TreeParent)child;
					}
				}
				if( stratNode == null ) {
					stratNode = new TreeParent(strat.getClass());
					currExecNode.addChild(stratNode);
				}
				
				TreeObject posNode = new TreeObject(posMgr.getPosition(strat));
				stratNode.addChild(posNode);
			}
		}
		
		
		PositionManager.getInstance().addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				
				if( PROP_RESET.equals(evt.getPropertyName()) ) {
					currExecNode = new TreeParent(new Date());
					root.addChild(currExecNode);
					refresh(root);
				} else if( PROP_ADD_STRATEGY.equals(evt.getPropertyName()) ) {
					// TODO: support multiple strategies?
				} else if( PROP_ADD_POSITION.equals(evt.getPropertyName()) ) {
					Position pos = (Position)evt.getNewValue(); 
					TreeParent stratNode = null;
					if( currExecNode == null ) {
						currExecNode = new TreeParent(new Date());
						root.addChild(currExecNode);
						refresh(root);
					}
					for(TreeObject child : currExecNode.getChildren()) {
						if(((Class)child.getObject()).equals(pos.getStrategy().getClass())) {
							stratNode = (TreeParent)child;
						}
					}
					if( stratNode == null ) {
						stratNode = new TreeParent(pos.getStrategy().getClass());
						currExecNode.addChild(stratNode);
					}
					stratNode.addChild(new TreeObject(pos));
					refresh(currExecNode);
				} else if( PROP_EXECUTION.equals(evt.getPropertyName()) ) {
					final Position pos = (Position)evt.getOldValue();
					refresh(currExecNode);
				}
			}
		});
		
		getSite().setSelectionProvider(this);
	}
	
	private void refresh(final Object o) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if( treeViewer != null ) {
					treeViewer.refresh(o);
				}
			}
		});
	}
	
	private void createActions() {
	}

	/**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
	public void createPartControl(Composite parent) {
		
		treeViewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		treeViewer.setContentProvider(new PositionContentProvider());
		treeViewer.setLabelProvider(new PositionLabelProvider());
		treeViewer.setInput(root);
		
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = getSelection();
				SelectionChangedEvent evt = new SelectionChangedEvent(StrategyResultsView.this, sel);
				for(ISelectionChangedListener listener : selectionListeners ) {
					listener.selectionChanged(evt);
				}
			}
		});

		createActions();

	}
	
	class PositionLabelProvider extends LabelProvider {
		public String getText(Object base) {
			Object obj = ((TreeObject)base).getObject();
			if( obj instanceof Position ) {
				Position pos = (Position)obj;
				return pos.getInstrument().getSymbol() + "<exec: " + pos.getExecutions().size() + ">";
			} else if( obj instanceof Class ) {
				return ((Class)obj).getSimpleName();
			} else {
				return obj.toString(); 
			}
		}
		public Image getImage(Object obj) {
			return null;
		}
	}
	
	class PositionContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		public Object[] getChildren(Object parent) {
			if( parent instanceof TreeParent ) {
				return ((TreeParent)parent).getChildren();
			}
			return new Object[0];
		}
		public Object[] getElements(Object parent) {
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if( child instanceof TreeObject ) {
				return ((TreeObject)child).getParent();
			}
			return null;
		}
	    public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent)parent).hasChildren();
			return false;
		}

		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.add(listener);
	}


	public ISelection getSelection() {
		ITreeSelection sel = (ITreeSelection)treeViewer.getSelection();
		final List<Object> objList = new ArrayList<Object>(sel.size());
		for(Object o : sel.toList()) {
			Object obj = ((TreeObject)o).getObject();
			if( obj instanceof Class) {
				TreeSet<TradeSummary> trades = new TreeSet<TradeSummary>();
				for(TreeObject to : ((TreeParent)o).getChildren()) {
					List<TradeSummary> currTrades = ((Position)to.getObject()).getTradeSummary(); 
					trades.addAll(currTrades);
				}
				objList.add(trades);
			} else {
				objList.add(obj);
			}
		}
		return new IStructuredSelection() {
			public boolean isEmpty() {
				return objList.size() <= 0;
			}
			public Object getFirstElement() {
				return objList.size() > 0 ? objList.get(0) : null;
			}
			public Iterator iterator() {
				return objList.iterator();
			}
			public int size() {
				return objList.size();
			}
			public Object[] toArray() {
				return objList.toArray();
			}
			public List toList() {
				return objList;
			}
		};
	}


	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.remove(listener);
	}


	public void setSelection(ISelection selection) {
		// no reversies
	}
}