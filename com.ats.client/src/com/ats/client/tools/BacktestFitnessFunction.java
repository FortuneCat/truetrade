package com.ats.client.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Display;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.impl.DoubleGene;
import org.jgap.impl.IntegerGene;
import org.jgap.impl.NumberGene;

import com.ats.client.views.OptimizationView;
import com.ats.client.wizards.ParamValues;
import com.ats.engine.PositionManager;
import com.ats.engine.StrategyDefinition;
import com.ats.engine.backtesting.BacktestFactory;
import com.ats.engine.backtesting.BacktestListener;
import com.ats.utils.StrategyAnalyzer;
import com.ats.utils.TradeStats;

@SuppressWarnings("serial")
public class BacktestFitnessFunction extends FitnessFunction {
	
	private List<ParamValues> paramValues;
	private StrategyDefinition stratDef;
	private OptimizationView view;
	private double netProfitOffset;
	
	private Map<Map<String, Number>, Double> pastResults = new HashMap<Map<String,Number>, Double>(); 

	public BacktestFitnessFunction(OptimizationView view, StrategyDefinition stratDef, List<ParamValues> values, double netProfitOffset) {
		this.stratDef = stratDef;
		this.paramValues = values;
		this.view = view;
		this.netProfitOffset = netProfitOffset;
	}

	@Override
	protected synchronized double evaluate(IChromosome chromo) {
		// check for an existing result
		final Map<String, Number> key = new HashMap<String, Number>();
		for( int i = 0; i < paramValues.size(); i++ ) {
			key.put(paramValues.get(i).paramName, (Number)((NumberGene)chromo.getGene(i)).getAllele());
		}
		if( pastResults.containsKey(key)) {
			return pastResults.get(key).doubleValue();
		}
		
		// each gene represents the trial number
		for( int i = 0; i < paramValues.size(); i++ ) {
			IntegerGene gene = (IntegerGene)chromo.getGene(i);
			ParamValues val = paramValues.get(i);
			if( val.initVal instanceof Integer ) {
				stratDef.setParameter(paramValues.get(i).paramName, 
						gene.intValue() * val.stepSize.intValue() + val.start.intValue() );
			} else {
				stratDef.setParameter(paramValues.get(i).paramName, 
						gene.intValue() * val.stepSize.doubleValue() + val.start.doubleValue() );
			}
		}
		
		new BacktestFactory().runBacktest(stratDef, new BacktestListener(){
			public void testComplete() {
				TradeStats stats = StrategyAnalyzer.calculateTradeStats(PositionManager.getInstance().getAllTrades());
				double result = stats.getTotalNet() + netProfitOffset;
				if( result < 0 ) {
					// JGAP can only handle positive results
					result = 0;
				}
				pastResults.put(key, result);
				if( view != null ) {
					view.addOptimizationTrial(stratDef.getParameterValues(), stats);
				}
			}
		});
		
		while( ! pastResults.containsKey(key) ) {
			try {
				Thread.sleep(300);
			} catch( Exception e) { }
		}
		return pastResults.get(key);
	}

}
