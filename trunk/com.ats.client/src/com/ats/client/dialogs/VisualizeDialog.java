package com.ats.client.dialogs;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Paint;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.ats.client.views.OptTrial;

public class VisualizeDialog extends Dialog {
	
	private List<OptTrial> trials;
	private String xparam;
	private String yparam;

	public VisualizeDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Visualize Optimization");
		shell.setSize(500, 500);
//		shell.setMaximized(true);
	}
	

	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite content = new Composite(parent, SWT.EMBEDDED);  // EMBEDDED to support JFreeChart
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
        gridLayout.numColumns = 1;
        content.setLayout(gridLayout);
//		content.setLayout(new FillLayout());
        
		Frame chartFrame = SWT_AWT.new_Frame(content);
		ChartPanel jfreeChartPanel = createChart();
		chartFrame.add(jfreeChartPanel);

		content.setSize(450, 450);

		
		return content;
	}

	private ChartPanel createChart() {
		
		final XYSeries trialSeries = new XYSeries("Results");
		for(OptTrial trial : trials ) {
			Number x = trial.paramVals.get(xparam);
			Number y = trial.paramVals.get(yparam);
			trialSeries.add(x, y);
		}
		XYSeriesCollection trialData = new XYSeriesCollection(trialSeries);
		JFreeChart chart = ChartFactory.createXYLineChart(
				"",
				xparam,
				yparam,
				trialData,
				PlotOrientation.VERTICAL,
				true,
				false,
				false);

		XYPlot plot = (XYPlot)chart.getPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer() {
			@Override
			public Paint getItemPaint(int row, int col) {
				return getPaint(row, col);
			}
			@Override
			public Paint getItemFillPaint(int row, int col) {
				return getPaint(row, col);
			}
			private Paint getPaint(int row, int col) {
				Number x = trialSeries.getX(row);
				Number y = trialSeries.getY(col);
				for( OptTrial trial : trials ) {
					if( trial.paramVals.get(xparam).equals(x)
							&& trial.paramVals.get(yparam).equals(y) ) {
						// have a hit
						return Color.red;
					}
				}
				return Color.blue;
			}
		};
		renderer.setSeriesLinesVisible(0, false);
		renderer.setSeriesShapesVisible(0, true);
		plot.setRenderer(renderer);
		
		ChartPanel chartPanel = new ChartPanel(chart);
		return chartPanel;
	}

	public void setTrials(List<OptTrial> trials) {
		this.trials = trials;
	}

	public void setXparam(String xparam) {
		this.xparam = xparam;
	}

	public void setYparam(String yparam) {
		this.yparam = yparam;
	}

}
