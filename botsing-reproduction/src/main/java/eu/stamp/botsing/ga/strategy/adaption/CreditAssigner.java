package eu.stamp.botsing.ga.strategy.adaption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class CreditAssigner<T> {

	protected static final Logger LOG = LoggerFactory.getLogger(CreditAssigner.class);

	public abstract void assign(Configuration configuration, List<T> chromosomes);
}
