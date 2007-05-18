package com.ats.client.tools;

import java.util.List;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.IGeneConstraintChecker;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DoubleGene;
import org.jgap.impl.IntegerGene;
import org.jgap.impl.NumberGene;

import com.ats.client.wizards.ParamValues;
import com.ats.engine.StrategyDefinition;

@SuppressWarnings("serial")
public class ParamValuesChromosome extends Chromosome {
	
	private List<ParamValues> values;
	private StrategyDefinition stratDef;
	
	public ParamValuesChromosome(Configuration config, Gene[] genes, StrategyDefinition stratDef, List<ParamValues> values) 
			throws InvalidConfigurationException {
		super(config, genes);
		
		this.stratDef = stratDef;
		this.values = values;
		
		setConstraintChecker(new IGeneConstraintChecker() {
			public boolean verify(Gene gene, Object alleleVal, IChromosome ichromo, int geneIndex) {
				ParamValuesChromosome chromo = (ParamValuesChromosome)ichromo;
				ParamValues pv = chromo.values.get(geneIndex);
				if( ((IntegerGene)gene).getAllele() == null ) {
					return false;
				}
				int gval = ((IntegerGene)gene).intValue();
				return 0 <= gval && gval <= pv.numTrials;
			}
		});
	}
	
	public List<ParamValues> getParamValues() {
		return values;
	}
	public StrategyDefinition getStrategyDefinition() {
		return this.stratDef;
	}
}
