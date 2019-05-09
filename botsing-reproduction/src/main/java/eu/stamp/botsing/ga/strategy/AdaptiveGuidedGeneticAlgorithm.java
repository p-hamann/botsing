package eu.stamp.botsing.ga.strategy;

import eu.stamp.botsing.CrashProperties;
import eu.stamp.botsing.ga.landscape.FitnessLandscapeAnalysor;
import eu.stamp.botsing.ga.strategy.adaption.*;
import eu.stamp.botsing.ga.strategy.operators.GuidedMutation;
import eu.stamp.botsing.ga.strategy.operators.GuidedSinglePointCrossover;
import eu.stamp.botsing.ga.strategy.operators.GuidedTwoPointCrossover;
import org.evosuite.Properties;
import org.evosuite.ga.*;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.operators.crossover.CrossOverFunction;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class AdaptiveGuidedGeneticAlgorithm<T extends Chromosome> extends GeneticAlgorithm<T> {

    private static final Logger LOG = LoggerFactory.getLogger(GuidedGeneticAlgorithm.class);

    protected ReplacementFunction replacementFunction = new FitnessReplacementFunction();

    private GuidedMutation<T> mutation;

    private int populationSize;

    private int eliteSize;

    private FitnessLandscapeAnalysor landscape;

    private boolean enableFla;

    private boolean keepOffspring;

    private boolean adapt;

    private Configuration currentConfiguration;

    private List<Configuration> configurations;

    private CreditAssigner assigner;

    private ConfigurationSelector selector;

    public AdaptiveGuidedGeneticAlgorithm(ChromosomeFactory<T> factory) {
        super(factory);
        this.crossoverFunction = new GuidedSinglePointCrossover();
        this.mutation = new GuidedMutation<>();
        if (CrashProperties.enable_fla) {
            this.landscape = new FitnessLandscapeAnalysor(chromosomeFactory, selectionFunction, fitnessFunctions, mutation, crossoverFunction, false);
            this.enableFla = true;
        } else {
            this.enableFla = false;
        }

        try {
            this.populationSize =  CrashProperties.getInstance().getIntValue("population");
            this.eliteSize = CrashProperties.getInstance().getIntValue("elite");
        } catch (IllegalAccessException e) {
            LOG.error("Illegal access during initialization", e);
        } catch (Properties.NoSuchParameterException e) {
            LOG.error("Parameter not found during initialization", e);
        }
        this.adapt = false;
        this.keepOffspring = false;
        this.configurations = initializeConfigurations();
        this.assigner = new Evolvability(fitnessFunctions, mutation);
        this.selector = new AdaptivePursuit(configurations, 0.8);
    }

    @Override
    public void generateSolution() {
        currentIteration = 0;

        // generate initial population
        LOG.info("Initializing the first population with size of {} individuals",this.populationSize);
        Boolean initilized = false;
        while (!initilized){
            try {
                initializePopulation();
                if (enableFla) {
                    landscape.addFittest(getBestIndividual());
                }
                initilized=true;
            }catch (Exception |Error e){
                LOG.warn("Botsing was unsuccessful in generating the initial population. cause: {}",e.getMessage());
            }
        }

        int starvationCounter = 0;
        double bestFitness = getBestFitness();
        double lastBestFitness = bestFitness;
        LOG.info("Best fitness in the initial population is: {}", bestFitness);
        long finalPT = getPassingTime();
        reportNewBestFF(lastBestFitness,finalPT);
        this.notifyIteration();
        LOG.info("Starting evolution");
        int generationCounter = 1;
        int tempCounter = 0;
        int generatedIndividuals = 50;
        while (!isFinished()){
        	if (adapt) {
        		changeCurrentConfiguration(selector.select());
        	} else {
        		if (starvationCounter == 2) {
        			adapt = true;
        		}
        	}

            // Create next generation
            Boolean newGen = false;
            while (!newGen){
                try{
                    evolve();
                    sortPopulation();
                    if (enableFla) {
                        landscape.addFittest(getBestIndividual());
                    }
                    newGen=true;
                }catch (Error | Exception e){
                    LOG.warn("Botsing was unsuccessful in generating new generation. cause: {}",e.getMessage());
                }
            }


            generationCounter++;
            bestFitness = getBestFitness();
            generatedIndividuals += populationSize;
            LOG.info("Best fitness in the current population: {} | {}", bestFitness,generatedIndividuals);

            // Check for starvation
            if (Double.compare(bestFitness, lastBestFitness) == 0) {
                starvationCounter++;
            } else {
                LOG.debug("Reset starvationCounter after {} iterations", starvationCounter);
                starvationCounter = 0;
                lastBestFitness = bestFitness;
                finalPT = getPassingTime();
                reportNewBestFF(lastBestFitness,finalPT);
                currentConfiguration.incrementImprovements();
            }

            ++tempCounter;
            updateSecondaryCriterion(starvationCounter);

            LOG.debug("Current iteration: {}", currentIteration);
            this.notifyIteration();
        }
        evaluateConfigurations();
        LOG.info("The search process is finished.");
        LOG.info("Number of gens: " + generationCounter);
        reportNewBestFF(lastBestFitness,finalPT);
        if (enableFla) {
            landscape.printMetricValues();
            landscape.createPlotFiles();
        }
    }

    private void reportNewBestFF(double lastBestFitness, long finalPT) {
        if(Properties.STOPPING_CONDITION == Properties.StoppingCondition.MAXTIME){
            LOG.info("Best fitness in the final population is: {}. PT: {} seconds", lastBestFitness,finalPT);
        }else{
            LOG.info("Best fitness in the final population is: {}. FE: {} ", lastBestFitness,finalPT);
        }

    }

    @Override
    protected void evolve() {
        // Elitism
        LOG.debug("Selection");
        List<T> newGeneration = new ArrayList<T>(elitism());

        while (newGeneration.size() < this.populationSize && !isFinished()) {
            LOG.debug("Generating offspring");
            T parent1 = selectionFunction.select(population);
            T parent2 = selectionFunction.select(population);
            T offspring1 = (T) parent1.clone();
            T offspring2 = (T) parent2.clone();
            // Crossover
            if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
                try {
                    crossoverFunction.crossOver(offspring1, offspring2);
                } catch (ConstructionFailedException e) {
                    e.printStackTrace();
                }
            }

            // Mutation
            this.mutation.mutateOffspring(offspring1);
            notifyMutation(offspring1);
            this.mutation.mutateOffspring(offspring2);
            notifyMutation(offspring2);

            //calculate fitness
            calculateFitness(offspring1);
            calculateFitness(offspring2);

            // If and only if one of the offsprings is not worse than the best parent, we replace parents by offsprings.
            if (keepOffspring || keepOffspring(parent1, parent2, offspring1, offspring2)) {
                LOG.debug("Replace parents");

                // Reject offspring straight away if it's too long
                int rejected = 0;
                if (isTooLong(offspring1) || offspring1.size() == 0) {
                    rejected++;
                } else {
                    newGeneration.add(offspring1);
                }

                if (isTooLong(offspring2) || offspring2.size() == 0) {
                    rejected++;
                } else {
                    newGeneration.add(offspring2);
                }

                if (rejected == 1) {
                    newGeneration.add(Randomness.choice(parent1, parent2));
                }else if (rejected == 2) {
                    newGeneration.add(parent1);
                    newGeneration.add(parent2);
                }
            } else {
                LOG.debug("Keep parents");
                newGeneration.add(parent1);
                newGeneration.add(parent2);
            }
        }

        population = newGeneration;
        if (enableFla) {
            landscape.addPopulation(population);
        }
        // archive
        updateFitnessFunctionsAndValues();
        if (adapt) {
        	assigner.assign(currentConfiguration, population);
        	currentConfiguration.updateAvgReward();
        }

        currentIteration++;
    }

    protected List<T>  elitism() {
        List<T> elite = new ArrayList<T>();
        LOG.debug("Cloning the best individuals to next generation");
        for (int i = 0; i < eliteSize; i++) {
            elite.add(population.get(i));
        }
        return elite;
    }

    @Override
    public void initializePopulation() {
        if (!population.isEmpty()) {
            return;
        }

        // Generate Initial Population
        generatePopulation(this.populationSize);

        LOG.debug("Initializing the population.");
        // Calculate fitness functions
        calculateFitness();
        // Sort individuals
        sortPopulation();
        assert!population.isEmpty() : "Could not create any test";
    }

    protected void sortPopulation() {
        LOG.debug("Sort current population.");
        if (fitnessFunctions.get(0).isMaximizationFunction()) {
            Collections.sort(population, Collections.reverseOrder());
        } else {
            Collections.sort(population);
        }
    }

    protected void calculateFitness() {
        LOG.debug("Calculating fitness for " + population.size() + " individuals");
        Iterator<T> iterator = population.iterator();
        while (iterator.hasNext()) {
            T c = iterator.next();
            if (isFinished()) {
                if (c.isChanged()){
                    iterator.remove();
                }
            } else {
                calculateFitness(c);
            }
        }
    }

    protected void calculateFitness(T chromosome){
        for (FitnessFunction<T> fitnessFunction : fitnessFunctions) {
            notifyEvaluation(chromosome);
            double value = fitnessFunction.getFitness(chromosome);
        }
    }

    private void generatePopulation(int populationSize) {
        LOG.debug("Creating random population");
        for (int i = 0; i < populationSize; i++) {
            T individual;
            individual = chromosomeFactory.getChromosome();
            for (FitnessFunction<?> fitnessFunction : this.fitnessFunctions) {
                individual.addFitness(fitnessFunction);
            }

            population.add(individual);

            if (isFinished()){
                break;
            }
        }
    }

    public long getPassingTime(){
        for (StoppingCondition c : stoppingConditions) {

            if(c.getClass().getName().contains("MaxFitnessEvaluationsStoppingCondition") || c.getClass().getName().contains("MaxTimeStoppingCondition")){
                return c.getCurrentValue();
            }
        }
        return 0;
    }

    public boolean isFinished() {
        for (StoppingCondition c : stoppingConditions) {
            LOG.debug("Current value of stopping condition "+ c.getClass().toString()+": "+c.getCurrentValue());

            // logger.error(c + " "+ c.getCurrentValue());
            if (c.isFinished()){
                LOG.info(c.toString());
                return true;
            }
        }
        return false;
    }

    private double getBestFitness() {
        T bestIndividual = getBestIndividual();
        for (FitnessFunction<T> ff : fitnessFunctions) {
            ff.getFitness(bestIndividual);
        }
        return bestIndividual.getFitness();
    }

    public T getBestIndividual() {
        if (population.isEmpty()) {
            return this.chromosomeFactory.getChromosome();
        }

        // Assume population is sorted
        return population.get(0);
    }

    protected boolean keepOffspring(Chromosome parent1, Chromosome parent2, Chromosome offspring1,
                                    Chromosome offspring2) {
        return replacementFunction.keepOffspring(parent1, parent2, offspring1, offspring2);
    }

    private List<Configuration> initializeConfigurations() {
    	List<Configuration> result = new ArrayList<Configuration>();
    	Configuration configuration = new Configuration(populationSize, eliteSize, crossoverFunction, Properties.CROSSOVER_RATE, keepOffspring, 1.0);
    	this.currentConfiguration = configuration;
    	result.add(configuration);

    	CrossOverFunction twoPointCrossoverFunction = new GuidedTwoPointCrossover();
    	result.add(new Configuration(populationSize, eliteSize, crossoverFunction, 0.60, false, 0.25));
    	result.add(new Configuration(populationSize, eliteSize+24, crossoverFunction, 0.75, true, 0.75));
    	result.add(new Configuration(populationSize+25, eliteSize+36, crossoverFunction, 0.80, true, 0.5));
    	result.add(new Configuration(populationSize+25, eliteSize+36, twoPointCrossoverFunction, 0.80, true, 0.25));

    	return result;
    }

    private void changeCurrentConfiguration(Configuration configuration) {
    	if (!configuration.equals(currentConfiguration)) {
    		populationSize = configuration.getPopulationSize();
    		eliteSize = configuration.getEliteSize();
    		crossoverFunction = configuration.getCrossoverFunction();
    		Properties.CROSSOVER_RATE = configuration.getCrossoverRate();
    		keepOffspring = configuration.getKeepOffspring();

    		currentConfiguration = configuration;
    	}
    	LOG.info("Using configuration: " + configuration.toString());
    }

    private void evaluateConfigurations() {
    	for (int i = 0; i < configurations.size(); ++i) {
    		Configuration conf = configurations.get(i);
    		double sum = 0.0;
    		for (Double d : conf.getAllRewards()) {
    			sum += d;
    		}
    		LOG.info("Overall average reward and improvements of configuration " + conf.toString() + ": " + sum/conf.getAllRewards().size() + " | " + conf.getImprovements());
    	}
    }
}
