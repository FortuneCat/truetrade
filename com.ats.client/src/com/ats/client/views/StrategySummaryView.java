package com.ats.client.views;

import static com.ats.utils.Utils.currencyForm;
import static com.ats.utils.Utils.doubleDecForm;

import java.util.Collection;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.ats.engine.TradeSummary;
import com.ats.platform.Position;
import com.ats.utils.Utils;

public class StrategySummaryView extends ViewPart  {
	public static final String ID = "com.ats.client.views.strategySummaryView";
	
	private Position position;
	
	private Browser browser;

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		
		ISelectionListener listener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				if( part == StrategySummaryView.this || !( selection instanceof IStructuredSelection) ) {
					// don't listen to my own or unstructured messages
					return;
				}
				setSelection(selection);
			}

		};
		
		getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(StrategyResultsView.ID, listener);
	}
	private void setSelection(ISelection selection) {
		IStructuredSelection sel = (IStructuredSelection)selection;
		Object obj = sel.getFirstElement();
		if( obj instanceof Position ) {
			setTrades(((Position)obj).getTradeSummary());
		} else if( obj instanceof Collection) {
			setTrades((Collection<TradeSummary>)obj); 
		} else {
			setTrades(null);
		}
	}
	
	@Override
	public void createPartControl(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
        gridLayout.numColumns = 1;
        content.setLayout(gridLayout);
        
        browser = new Browser(content, SWT.NONE);
        
        GridData data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;
        browser.setLayoutData(data);
	}
	
	private void setTrades(Collection <TradeSummary> trades) {
		browser.setText("");
		if( trades == null || trades.size() <= 0) {
			return;
		}
		
		double grossProfit=0, netProfit = 0, grossLoss=0, netLoss = 0, grossUnrealized=0;
		int numTrades=0, numShares=0, numWinners=0, numLosers=0;
		double largestWinner=0, largestLoser=0;
		int numConsecWinners=0, numConsecLosers=0, maxConsecWinners=0, maxConsecLosers=0;
		double maxDrawdown = 0;
		int maxShares = 0;
		for(TradeSummary ts : trades) {
			maxShares = Math.max(ts.getTotalBuyQty(), maxShares);
			maxShares = Math.max(ts.getTotalSellQty(), maxShares);
			maxDrawdown = Math.max(maxDrawdown, ts.getMaxDrawdown());
			numTrades++;
			numShares += (ts.getTotalBuyQty() + ts.getTotalSellQty());
			grossUnrealized += ts.getUnrealizedProfit();
			if( ts.getRealizedPnL() > 0 ) {
				grossProfit += ts.getRealizedPnL();
				netProfit += ts.getRealizedPnL();
				numWinners++;
				largestWinner = Math.max(ts.getRealizedNetPnL(), largestWinner);
				numConsecLosers = 0;
				numConsecWinners++;
				maxConsecWinners = Math.max(maxConsecWinners, numConsecWinners);
			} else {
				grossLoss += ts.getRealizedPnL();
				netLoss += ts.getRealizedNetPnL();
				numLosers++;
				largestLoser = Math.min(ts.getRealizedNetPnL(), largestLoser);
				numConsecWinners = 0;
				numConsecLosers++;
				maxConsecLosers = Math.max(maxConsecLosers, numConsecLosers);
			}
		}
		
		// calc comission
//		double comission = 0.0;
//		if( Utils.getPreferenceStore().getBoolean(Utils.COMMISSION_SHARE) ) {
//			comission = numShares * Utils.getPreferenceStore().getDouble(Utils.COMMISSION_SHARE_VALUE);
//		} else if( Utils.getPreferenceStore().getBoolean(Utils.COMMISSION_ORDER) ) {
//			comission = numTrades * Utils.getPreferenceStore().getDouble(Utils.COMMISSION_ORDER_VALUE);
//		}
		
		double totalGross = grossProfit + grossLoss;
		double totalNet = netProfit + netLoss;
		final StringBuffer text = new StringBuffer();
		text.append("<html><head></head><body>");
		text.append("<table border=\"1\" style=\"font-family:verdana;font-size:70%;\" cellpadding=\"4\"><tr>");
		
		text.append("<td align=\"left\">Total gross realized profit</td><td align=\"right\"><b>$" + currencyForm.format(totalGross) + "</b></td>");
		text.append("<td align=\"left\">Total gross unrealized profit</td><td align=\"right\"><b>$" + currencyForm.format(grossUnrealized) + "</b></td>");
		text.append("</tr>\n<tr>");
		text.append("<td align=\"left\">Total net profit</td><td align=\"right\"><b>$" + currencyForm.format(totalNet) + "</b></td>");
		text.append("<td align=\"left\">Total comissions</td><td align=\"right\"><b>$" + currencyForm.format(totalGross - totalNet) + "</b></td>");
		text.append("</tr>\n<tr>");
		text.append("<td align=\"left\">Gross profits</td><td align=\"right\"><b>$" + currencyForm.format(grossProfit) + "</b></td>");
		text.append("<td align=\"left\">Gross losers</td><td align=\"right\"><b>$" + currencyForm.format(grossLoss) + "</b></td>");
		text.append("</tr>\n<tr>");
		text.append("</tr><td>&nbsp;</td><tr>");
		text.append("<td align=\"left\">Total trades</td><td align=\"right\"><b>" + numTrades + "</b></td>");
		text.append("<td align=\"left\">Total shares</td><td align=\"right\"><b>" + numShares + "</b></td>");
		text.append("</tr>\n<tr>");
		text.append("<td align=\"left\">Number winning trades</td><td align=\"right\"><b>" + numWinners + "</b></td>");
		text.append("<td align=\"left\">Number losing trades</td><td align=\"right\"><b>" + numLosers + "</b></td>");
		text.append("</tr>\n<tr>");
		text.append("<td align=\"left\">Largest winning trade</td><td align=\"right\"><b>$" + currencyForm.format(largestWinner) + "</b></td>");
		text.append("<td align=\"left\">Largest losing trade</td><td align=\"right\"><b>$" + currencyForm.format(largestLoser) + "</b></td>");
		text.append("</tr>\n<tr>");
		double avgWinner = grossProfit / numWinners;
		double avgLoser = grossLoss / numLosers;
		text.append("<td align=\"left\">Average winning trade</td><td align=\"right\"><b>$" + currencyForm.format(avgWinner) + "</b></td>");
		text.append("<td align=\"left\">Average losing trade</td><td align=\"right\"><b>$" + currencyForm.format(avgLoser) + "</b></td>");
		text.append("</tr>\n<tr>");
		text.append("<td align=\"left\">Ratio avg win/avg loss</td><td align=\"right\"><b>" + doubleDecForm.format(Math.abs(avgWinner / avgLoser)) + "</b></td>");
		text.append("<td align=\"left\">Avg trade (win & loss)</td><td align=\"right\"><b>$" + currencyForm.format(totalNet/numTrades) + "</b></td>");
		text.append("</tr>\n<tr>");
		text.append("<td align=\"left\">Max consec. winners</td><td align=\"right\"><b>" + maxConsecWinners + "</b></td>");
		text.append("<td align=\"left\">Max consec. losers</td><td align=\"right\"><b>" + maxConsecLosers + "</b></td>");
		text.append("</tr>\n<tr>");
		text.append("<td align=\"left\">Avg # bars in winners</td><td align=\"right\"><b>" + "&nbsp;" + "</b></td>");
		text.append("<td align=\"left\">Avg # bars in losers</td><td align=\"right\"><b>" + "&nbsp;" + "</b></td>");
		text.append("</tr>\n<tr>");
		text.append("</tr><td>&nbsp;</td><tr>");
		text.append("<td align=\"left\">Profit factor</td><td align=\"right\"><b>" + "&nbsp;" + "</b></td>");
		text.append("</tr>\n<tr>");
		text.append("<td align=\"left\">Max per-trade drawdown</td><td align=\"right\"><b>$" + currencyForm.format(maxDrawdown) + "</b></td>");
		text.append("<td align=\"left\">Account size required</td><td align=\"right\"><b>" + "&nbsp;" + "</b></td>");
		text.append("</tr>\n<tr>");
		text.append("<td align=\"left\">Return on account</td><td align=\"right\"><b>" + "&nbsp;" + "</b></td>");
		text.append("<td align=\"left\">Max # contracts held</td><td align=\"right\"><b>" + maxShares + "</b></td>");
		text.append("</tr>\n");
		text.append("</table>");
		text.append("</body></html>");
		browser.setText(text.toString());
	}

	
	public void setFocus() {
		ISelection selection = getSite().getWorkbenchWindow()
				.getSelectionService().getSelection(StrategyResultsView.ID);
		setSelection(selection);
	}

}
