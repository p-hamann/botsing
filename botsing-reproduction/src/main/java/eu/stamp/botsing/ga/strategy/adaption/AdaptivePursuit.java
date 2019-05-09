package eu.stamp.botsing.ga.strategy.adaption;

import org.evosuite.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

public class AdaptivePursuit extends ConfigurationSelector {

	private List<Double> selectionProbabilities;

	private int nConfigurations;

	private double pMax;

	private double pMin;

	private double beta;

	public AdaptivePursuit(List<Configuration> configurations, double beta) {
		super(configurations);
		this.nConfigurations = configurations.size();
		this.selectionProbabilities = new ArrayList<Double>(nConfigurations);
		this.beta = beta;
		this.pMin = 0.5/configurations.size();
		this.pMax = 0.5+pMin;
	}

	public Configuration select() {
		// Search the configuration with highest reward
		int best = -1;
		for (int i = 0; i < nConfigurations; ++i) {
			if (best == -1 || configurations.get(i).getAvgReward() > configurations.get(best).getAvgReward()) {
				best = i;
			}
		}
		LOG.info("Best configuration: " + configurations.get(best).toString());

		// Determine selection probabilities
		if (selectionProbabilities.isEmpty()) {
			for (int i = 0; i < nConfigurations; ++i) {
				if (i != best) {
					selectionProbabilities.add(pMin);
				} else {
					selectionProbabilities.add(pMax);
				}
			}
		} else {
			for (int i = 0; i < nConfigurations; ++i) {
				if (i != best) {
					selectionProbabilities.set(i, selectionProbabilities.get(i)+beta*(pMin-selectionProbabilities.get(i)));
				} else {
					selectionProbabilities.set(i, selectionProbabilities.get(i)+beta*(pMax-selectionProbabilities.get(i)));
				}
			}
		}

		// Select a configuration
		double random = Randomness.nextDouble();
		double sum = 0.0;
		for (int i = 0; i < nConfigurations; ++i) {
			sum += selectionProbabilities.get(i);
			if (random < sum) {
				return configurations.get(i);
			}
		}
		return configurations.get(nConfigurations-1);
	}
}
