package eu.stamp.botsing.ga.landscape.metrics;

import org.evosuite.ga.Chromosome;

import java.util.List;

public class ChangeRate<T extends Chromosome> extends FLAMetric<T>  {

	public ChangeRate() {
		this.name = "Change Rate";
		this.inputVariable = InputVariable.Fittest;
	}

	public double calculate(List<T> chromosomes) {
		double improvements = 0;
		for (int i = 1; i < chromosomes.size(); ++i) {
			if (chromosomes.get(i).getFitness() != chromosomes.get(i-1).getFitness()) {
				++improvements;
			}
		}

		return improvements/(chromosomes.size()-1);
	}

}

