package com.ats.client.runtime.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.ats.db.PlatformDAO;
import com.ats.engine.IBHelper;
import com.ats.engine.IBWrapperAdapter;
import com.ats.platform.MessageListener;

public class MessagesView  extends ViewPart {

	public static final String ID = "com.ats.client.runtime.views.messagesView";

	private List messageList;
	
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
			private void addMessage(final String message) {
				if( messageList != null && ! messageList.isDisposed()) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							messageList.add(message);
							//messageList.redraw();
						}
					});
				}
			}
		});
		
	}

	@Override
	public void createPartControl(Composite parent) {
		messageList = new List(parent, SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		
		// see if this generates some messages
		IBHelper.getInstance().requestExecutions();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}
