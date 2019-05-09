package eu.stamp.botsing.ga.strategy.adaption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class ConfigurationSelector {

	protected static final Logger LOG = LoggerFactory.getLogger(ConfigurationSelector.class);

	protected List<Configuration> configurations;

	public ConfigurationSelector(List<Configuration> configurations) {
		this.configurations = configurations;
	}

	public abstract Configuration select();

}
