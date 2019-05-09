package eu.stamp.botsing.ga.landscape.metrics;

import org.evosuite.ga.Chromosome;

import java.util.List;

public class PartialInformationContent<T extends Chromosome> extends FLAMetric<T>  {

	private double epsilon;

	public PartialInformationContent(double epsilon) {
		this.name = "Partial Information Content (e=" + epsilon + ")";
		this.inputVariable = InputVariable.RandomWalk;
		this.epsilon = epsilon;
	}

	public double calculate(List<T> chromosomes) {
		StringBuffer strBuffer = new StringBuffer();
		char strElement = 'n';
		for (int i = 1; i < chromosomes.size(); ++i) {
			double diff = chromosomes.get(i).getFitness() - chromosomes.get(i-1).getFitness();
			if (diff > epsilon && strElement != '1') {
				strElement = '1';
				strBuffer.append(strElement);
			} else if (diff < -epsilon && strElement != '2') {
				strElement = '2';
				strBuffer.append(strElement);
			}
		}

		return strBuffer.length()/((double)chromosomes.size());
	}

}

