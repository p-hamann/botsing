package eu.stamp.botsing.ga.landscape.metrics;

import org.evosuite.ga.Chromosome;

import java.util.List;

public class DensityBasinInformation<T extends Chromosome> extends FLAMetric<T>  {

	private double epsilon;

	public DensityBasinInformation(double epsilon) {
		this.name = "Density-basin Information (e=" + epsilon + ")";
		this.inputVariable = InputVariable.RandomWalk;
		this.epsilon = epsilon;
	}

	public double calculate(List<T> chromosomes) {
		int steps = chromosomes.size()-1;
		char[] string = new char[steps];
		for (int i = 1; i <= steps; ++i) {
			double diff = chromosomes.get(i).getFitness() - chromosomes.get(i-1).getFitness();
			if (Math.abs(diff) <= epsilon) {
				string[i-1] = '0';
			} else if (diff > epsilon) {
				string[i-1] = '1';
			} else {
				string[i-1] = '2';
			}
		}

		int n00 = 0;
		int n11 = 0;
		int n22 = 0;

		for (int i = 1; i < steps; ++i) {
			if (string[i] == string[i-1]) {
				switch(string[i]) {
					case '0': n00++; break;
					case '1': n11++; break;
					case '2': n22++; break;
				}
			}
		}

		double p00 = n00/(double)steps;
		double p11 = n11/(double)steps;
		double p22 = n22/(double)steps;

		double h00 = (p00 == 0.0) ? 0.0 : -(p00*Math.log(p00)/Math.log(3.0));
		double h11 = (p11 == 0.0) ? 0.0 : -(p11*Math.log(p11)/Math.log(3.0));
		double h22 = (p22 == 0.0) ? 0.0 : -(p22*Math.log(p22)/Math.log(3.0));

		return h00+h11+h22;
	}

}

