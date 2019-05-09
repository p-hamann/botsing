package eu.stamp.botsing.ga.strategy.adaption;

import eu.stamp.botsing.ga.strategy.operators.GuidedMutation;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.utils.Randomness;

import java.util.List;

public class Evolvability<T extends Chromosome> extends CreditAssigner<T> {

	private List<FitnessFunction<T>> fitnessFunctions;

	private GuidedMutation<T> mutation;

	public Evolvability(List<FitnessFunction<T>> fitnessFunctions, GuidedMutation<T> mutation) {
		this.fitnessFunctions = fitnessFunctions;
		this.mutation = mutation;
	}

	public void assign(Configuration configuration, List<T> chromosomes) {
		int upperBound = chromosomes.size()-1;
		int nSolutionPairs = configuration.getPopulationSize()/7;
		for (int c = 0; c < nSolutionPairs; ++c) {
			T parent1 = (T)chromosomes.get(Randomness.nextInt(upperBound)).clone();
			T parent2 = (T)chromosomes.get(Randomness.nextInt(upperBound)).clone();
			int better = 0;
			double fitnessSum = 0.0;

			// Choose better parent as individual of interest
			T t1;
			T t2;
			if (parent1.getFitness() < parent2.getFitness()) {
				t1 = parent1;
				t2 = parent2;
			} else {
				t1 = parent2;
				t2 = parent1;
			}
			double parentFitness = t1.getFitness();

			// Sample the neighborhood for individual of interest and calculate evolvability
			for (int i = 0; i < 7; ++i) {
				boolean success = false;
				while (!success) {
					try {
						T offspring1 = (T)t1.clone();
						if (Randomness.nextDouble() <= configuration.getCrossoverRate()) {
							T offspring2 = (T)t2.clone();
							configuration.crossoverFunction.crossOver(offspring1, offspring2);
						}
						mutation.mutateOffspring(offspring1);

						for (FitnessFunction<T> fitnessFunction : fitnessFunctions) {
				            fitnessFunction.getFitness(offspring1);
				        }
						if (offspring1.getFitness() < parentFitness) {
							++better;
						}
						fitnessSum += offspring1.getFitness();
						success = true;
					} catch (Exception | Error e) {
						LOG.warn("Botsing was unsuccessful in generating neighbor. cause: {}",e.getMessage());
					}
				}
			}

			// 42 = 6*7, 7 for the average, 6 for normalization
			configuration.addReward((better/7.0 + (1-(fitnessSum/42.0)))/2.0);
		}
	}
}
