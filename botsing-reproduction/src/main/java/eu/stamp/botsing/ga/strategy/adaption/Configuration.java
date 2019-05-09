package eu.stamp.botsing.ga.strategy.adaption;

import org.evosuite.ga.operators.crossover.CrossOverFunction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Configuration {

	private int populationSize;

	private int eliteSize;

	protected CrossOverFunction crossoverFunction;

	private double crossoverRate;

	private boolean keepOffspring;

	protected LinkedList<Double> rewards;

	protected double avgReward;

	protected List<Double> allRewards;

	protected int improvements = 0;

	public Configuration(int populationSize,
			int eliteSize,
			CrossOverFunction crossoverFunction,
			double crossoverRate,
			boolean keepOffspring,
			double initAvgReward) {
		this.crossoverFunction = crossoverFunction;
		this.crossoverRate = crossoverRate;
		this.populationSize = populationSize;
		this.eliteSize = eliteSize;
		this.keepOffspring = keepOffspring;
		this.avgReward = initAvgReward;
		this.rewards = new LinkedList<Double>();
		this.allRewards = new ArrayList<Double>(200);
	}

	public void updateAvgReward() {
		double sum = 0.0;
		for (Double reward : rewards) {
			sum += reward;
		}
		avgReward = sum/rewards.size();
	}

	protected void addReward(double reward) {
		// Update sliding window
		if (rewards.size() > populationSize/2) {
			rewards.removeFirst();
		}
		rewards.add(reward);
		allRewards.add(reward);
	}

	public void incrementImprovements() {
		improvements++;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public int getEliteSize() {
		return eliteSize;
	}

	public CrossOverFunction getCrossoverFunction() {
		return crossoverFunction;
	}

	public double getCrossoverRate() {
		return crossoverRate;
	}

	public double getAvgReward() {
		return avgReward;
	}

	public boolean getKeepOffspring() {
		return keepOffspring;
	}

	public List<Double> getAllRewards() {
		return allRewards;
	}

	public int getImprovements() {
		return improvements;
	}

	public String toString() {
		return "(" + populationSize
				+ ", " + eliteSize
				+ ", " + crossoverFunction.getClass().getSimpleName()
				+ ", " + crossoverRate
				+ ", " + keepOffspring + ")";
	}

}
