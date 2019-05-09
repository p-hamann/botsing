package eu.stamp.botsing.ga.landscape.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class FLAMetric<T> {

	protected static final Logger LOG = LoggerFactory.getLogger(FLAMetric.class);

	public enum InputVariable {
		Fittest,
		RandomWalk,
		Sample;
	}

	protected String name;

	protected InputVariable inputVariable;

	public String getName() {
		return name;
	}

	public InputVariable getInputVariable() {
		return inputVariable;
	}

	public abstract double calculate(List<T> t);

}

