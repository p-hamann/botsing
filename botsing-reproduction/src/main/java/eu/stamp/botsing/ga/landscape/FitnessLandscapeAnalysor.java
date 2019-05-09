package eu.stamp.botsing.ga.landscape;

import eu.stamp.botsing.ga.landscape.metrics.*;
import eu.stamp.botsing.ga.strategy.operators.GuidedMutation;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.operators.crossover.CrossOverFunction;
import org.evosuite.ga.operators.selection.SelectionFunction;
import org.evosuite.Properties;
import org.evosuite.utils.Randomness;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class FitnessLandscapeAnalysor<T extends Chromosome> {

	private static final Logger LOG = LoggerFactory.getLogger(FitnessLandscapeAnalysor.class);

	// a bit messy, better not hard-coded, but currently not smartly implementable
	private final boolean ENABLE_LANDMARKING_BASED_ANALYSIS = true;

	private final int LENGTH_OF_RANDOM_WALK = 10000;

	private final int AEP_NEIGHBORS = 100;


	private ChromosomeFactory<T> chromosomeFactory;

	private SelectionFunction<T> selectionFunction;

	private List<FitnessFunction<T>> fitnessFunctions;

	private GuidedMutation<T> mutation;

	private CrossOverFunction crossover;

	private List<List<T>> populations;

	private List<T> fittest;

	private List<T> randomWalk;

	private List<FLAMetric> metrics;

	private boolean enableLandmarking;

	public FitnessLandscapeAnalysor(ChromosomeFactory<T> chromosomeFactory,
			SelectionFunction<T> selectionFunction,
			List<FitnessFunction<T>> fitnessFunctions,
			GuidedMutation<T> mutation,
			CrossOverFunction crossover,
			boolean enableLandmarking) {
		this.chromosomeFactory = chromosomeFactory;
		this.selectionFunction = selectionFunction;
		this.fitnessFunctions = fitnessFunctions;
		this.mutation = mutation;
		this.crossover = crossover;
		this.enableLandmarking = enableLandmarking;
		this.populations = new ArrayList<List<T>>();
		this.fittest = new ArrayList<T>();
		this.randomWalk = new ArrayList<T>();
	}

	/**
	 *  Prints metric values for initialized metrics after the execution of the genetic algorithm
	 */
	public void printMetricValues() {
		metrics = initializeMetrics();
		for (FLAMetric metric : metrics) {
			switch(metric.getInputVariable()) {
				case Fittest:
					LOG.info(metric.getName() + ": " + metric.calculate(fittest));
					break;
				case Sample:
					LOG.info(metric.getName() + ": " + metric.calculate(generateSample()));
					break;
				case RandomWalk:
					if (randomWalk.isEmpty()) {
						LOG.info("Performing a random walk");
						performRandomWalk();
					}
					LOG.info(metric.getName() + ": " + metric.calculate(randomWalk));
					break;
			}
		}
	}

	/**
	 *  Creates files to visualize the search process and the random walk
	 */
	public void createPlotFiles() {
		try {
			File file = new File("Populations.txt");
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			for (int i = 0; i < populations.size(); ++i) {
				List<T> population = populations.get(i);
					for (int j = 0; j < population.size(); ++j) {
					bw.write((i+1) + "\t" + (j+1) + "\t" +  population.get(j).getFitness() + "\n");
				}
				bw.newLine();
			}
			bw.close();

			if (!randomWalk.isEmpty()) {
			    file = new File("RandomWalk.txt");
			    bw = new BufferedWriter(new FileWriter(file));
			    for (int i = 0; i < randomWalk.size(); ++i) {
				    bw.write(i+1 + "\t" + randomWalk.get(i).getFitness() + "\n");
			    }
			    bw.close();
			}

		} catch (IOException e) {
			LOG.info(e.toString());
		}
	}

	public void addPopulation(List<T> population) {
		populations.add(population);
	}

	public void addFittest(T fittest) {
		this.fittest.add(fittest);
	}

	private List<FLAMetric> initializeMetrics() {
		List<FLAMetric> result = new ArrayList<FLAMetric>();
		if (!fittest.isEmpty()) {
			result.add(new ChangeRate());
			result.add(new PopulationInformationContent());
		}
		if (!populations.isEmpty()) {
			result.add(new AccumulatedEscapeProbability(AEP_NEIGHBORS, fitnessFunctions, mutation, crossover));
		}
		if (enableLandmarking) {
			double epsilon = 0.0;
			for (int i = 0; i < 4; ++i) {
				result.add(new InformationContent(epsilon));
				result.add(new PartialInformationContent(epsilon));
				result.add(new DensityBasinInformation(epsilon));
				epsilon += 0.3;
			}
		}

		return result;
	}

	private List<T> generateSample() {
		List<T> sample = new ArrayList<T>();
		for (List<T> population : populations) {
			sample.add(selectionFunction.select(population));
			sample.add(selectionFunction.select(population));
		}

		return sample;
	}

	private void performRandomWalk() {
		T parent = (T)chromosomeFactory.getChromosome();
		randomWalk.add(parent);
		for (int i = 0; i < LENGTH_OF_RANDOM_WALK; ++i) {
			boolean added = false;
			while (!added) {
				try {
					T offspring = (T)parent.clone();
					if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
						T offspring2 = (T)chromosomeFactory.getChromosome();
						crossover.crossOver(offspring, offspring2);
					}
					mutation.mutateOffspring(offspring);
					for (FitnessFunction<T> fitnessFunction : fitnessFunctions) {
						fitnessFunction.getFitness(offspring);
					}
					randomWalk.add(offspring);
					parent = offspring;
					added = true;
				} catch (Exception | Error e) {
					LOG.warn("Botsing was unsuccessful in generating the next individual of the random walk. cause: {}",e.getMessage());
				}
			}
		}
	}

}
