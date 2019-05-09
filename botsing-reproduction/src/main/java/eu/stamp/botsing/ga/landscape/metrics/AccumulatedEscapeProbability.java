package eu.stamp.botsing.ga.landscape.metrics;

import eu.stamp.botsing.ga.strategy.operators.GuidedMutation;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.operators.crossover.CrossOverFunction;
import org.evosuite.Properties;
import org.evosuite.utils.Randomness;

import java.util.List;

public class AccumulatedEscapeProbability<T extends Chromosome> extends FLAMetric<T>  {

	private int neighbors;

	private List<FitnessFunction<T>> fitnessFunctions;

	private GuidedMutation<T> mutation;

	private CrossOverFunction crossover;

	public AccumulatedEscapeProbability(int neighbors,
			List<FitnessFunction<T>> fitnessFunctions,
			GuidedMutation<T> mutation,
			CrossOverFunction crossover) {
		this.name = "Accumulated Escape Probability";
		this.inputVariable = InputVariable.Sample;
		this.neighbors = neighbors;
		this.fitnessFunctions = fitnessFunctions;
		this.mutation = mutation;
		this.crossover = crossover;
	}

    /**
     * Expects for all even i chromosomes.get(i) as sample elements
     * with chromosomes.get(i+1) as potential crossover partner for i
     */
	public double calculate(List<T> chromosomes) {
		double accEscProb = 0.0;
		int samples = 0;
		for (int i = 0; i < chromosomes.size(); i += 2) {
			T parent = chromosomes.get(i);
			double fitness = parent.getFitness();
			int better = 0;
			for (int j = 0; j < this.neighbors; ++j) {
				boolean added = false;
				while (!added) {
					try {
						T offspring = (T)parent.clone();
						if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
							T offspring2 = (T)chromosomes.get(i+1).clone();
							crossover.crossOver(offspring, offspring2);
						}
						mutation.mutateOffspring(offspring);
						for (FitnessFunction<T> fitnessFunction : fitnessFunctions) {
							fitnessFunction.getFitness(offspring);
						}
						if (offspring.getFitness() < fitness) {
							++better;
						}
						added = true;
					} catch (Exception | Error e) {
						LOG.warn("AEP: Botsing was unsuccessful in generating the next neighbour for sample " + j + ". cause: {}",e.getMessage());
					}
				}
			}
			accEscProb += better/(double)this.neighbors;
			++samples;
		}
		return accEscProb/samples;
	}

}
