package com.ats.client.runtime.views;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.ats.engine.IBHelper;
import com.ats.engine.IBWrapperAdapter;
import com.ats.platform.MessageListener;

public class MessagesView  extends ViewPart {

	public static final String ID = "com.ats.client.runtime.views.messagesView";

	private ListViewer messageList;
	
	private Priority priority = Priority.DEBUG;
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		
		IBWrapperAdapter.getWrapper().addMessageListener(new MessageListener() {
			public void error(final int id, final int errorCode, final String errorMsg) {
				addMessage("Error: " + id + ":" + errorCode + "[" + errorMsg + "]");
			}
			public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
				addMessage("News: from " + origExchange + " [" + message + "]" );
			}
		});
		
	}
	private void addMessage(final String message) {
		if( messageList != null && ! messageList.getList().isDisposed()) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					messageList.add(message);
					messageList.reveal(message);
				}
			});
		}
	}
	
	private void createActions() {
		ControlContribution logLevelControl = new ControlContribution("Log Level") {
			private Combo combo;
			protected Control createControl(Composite parent) {
				combo = new Combo(parent, SWT.READ_ONLY);
				combo.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
						Priority prio = (Priority)combo.getData(combo.getText());
						setPriority(prio);
					}
					public void widgetSelected(SelectionEvent e) {
						Priority prio = (Priority)combo.getData(combo.getText());
						setPriority(prio);
					}
				});
				Priority priorities[] = Priority.getAllPossiblePriorities();
				for( Priority prio : priorities) {
					combo.add(prio.toString());
					combo.setData(prio.toString(), prio);
				}
				combo.setText(Priority.DEBUG.toString());
				setPriority(Priority.DEBUG);
				return combo;
			}
		};
		

		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		mgr.add(logLevelControl);
	}

	
	private void setPriority(Priority prio) {
		this.priority = prio;
	}


	@Override
	public void createPartControl(Composite parent) {
		createActions();
		
		messageList = new ListViewer(parent, SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		messageList.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return ((java.util.List)inputElement).toArray();
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		messageList.setLabelProvider(new MessageProvider());
		
		Logger.getRootLogger().addAppender(new AppenderSkeleton() {
			protected void append(final LoggingEvent evt) {
				if( ! evt.getLevel().isGreaterOrEqual(priority)) {
					return;
				}
				if( getLayout() != null ) {
					addMessage(getLayout().format(evt));
				} else {
					String category = evt.getLoggerName();
					category = category.substring(category.lastIndexOf('.') + 1);
					addMessage("[" + evt.getLevel() + "][" + category + "]" + evt.getMessage());
				}
			}
			public void close() {
			}
			public boolean requiresLayout() {
				return false;
			}
		});
		
		// see if this generates some messages
		IBHelper.getInstance().requestExecutions();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	class MessageProvider extends LabelProvider {
		public String getText(Object element) {
			return element.toString();
		}
	}

}
