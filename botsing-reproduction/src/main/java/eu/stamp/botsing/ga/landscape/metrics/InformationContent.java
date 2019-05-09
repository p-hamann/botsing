package eu.stamp.botsing.ga.landscape.metrics;

import org.evosuite.ga.Chromosome;

import java.util.List;

public class InformationContent<T extends Chromosome> extends FLAMetric<T>  {

	private double epsilon;

	public InformationContent(double epsilon) {
		this.name = "Information Content (e=" + epsilon + ")";
		this.inputVariable = InputVariable.RandomWalk;
		this.epsilon = epsilon;
	}

	public double calculate(List<T> chromosomes) {
		int steps = chromosomes.size()-1;
		StringBuffer strBuffer = new StringBuffer();
		for (int i = 1; i <= steps; ++i) {
			double diff = chromosomes.get(i).getFitness() - chromosomes.get(i-1).getFitness();
			if (Math.abs(diff) <= epsilon) {
				strBuffer.append('0');
			} else if (diff > epsilon) {
				strBuffer.append('1');
			} else {
				strBuffer.append('2');
			}
		}
		String str = strBuffer.toString();

		double p01 = ((str.length()-str.replace("01","").length())/2.0)/steps;
		double p10 = ((str.length()-str.replace("10","").length())/2.0)/steps;
		double p02 = ((str.length()-str.replace("02","").length())/2.0)/steps;
		double p20 = ((str.length()-str.replace("20","").length())/2.0)/steps;
		double p12 = ((str.length()-str.replace("12","").length())/2.0)/steps;
		double p21 = ((str.length()-str.replace("21","").length())/2.0)/steps;

		double h01 = (p01 == 0.0) ? 0.0 : -(p01*Math.log(p01)/Math.log(6.0));
		double h10 = (p10 == 0.0) ? 0.0 : -(p10*Math.log(p10)/Math.log(6.0));
		double h02 = (p02 == 0.0) ? 0.0 : -(p02*Math.log(p02)/Math.log(6.0));
		double h20 = (p20 == 0.0) ? 0.0 : -(p20*Math.log(p20)/Math.log(6.0));
		double h12 = (p12 == 0.0) ? 0.0 : -(p12*Math.log(p12)/Math.log(6.0));
		double h21 = (p21 == 0.0) ? 0.0 : -(p21*Math.log(p21)/Math.log(6.0));

		return h01+h10+h02+h20+h12+h21;
	}

}

