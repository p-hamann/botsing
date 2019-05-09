package eu.stamp.botsing.ga.landscape.metrics;

import org.evosuite.ga.Chromosome;

import java.util.List;

public class PopulationInformationContent<T extends Chromosome> extends FLAMetric<T>  {

	public PopulationInformationContent() {
		this.name = "Population Information Content";
		this.inputVariable = InputVariable.Fittest;
	}

	public double calculate(List<T> chromosomes) {
		StringBuffer binaryBuffer = new StringBuffer();
		for (int i = 1; i < chromosomes.size(); ++i) {
			if (chromosomes.get(i).getFitness() == chromosomes.get(i-1).getFitness()) {
				binaryBuffer.append('0');
			} else {
				binaryBuffer.append('1');
			}
		}

		String binary = binaryBuffer.toString();
		double p01 = ((binary.length()-binary.replace("01","").length())/2.0)/(chromosomes.size()-1);
		double p10 = ((binary.length()-binary.replace("10","").length())/2.0)/(chromosomes.size()-1);
		double h01 = (p01 == 0.0) ? 0.0 : -(p01*Math.log(p01)/Math.log(2.0));
		double h10 = (p10 == 0.0) ? 0.0 : -(p10*Math.log(p10)/Math.log(2.0));

		return h01+h10;
	}

}

