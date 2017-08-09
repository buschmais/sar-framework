package com.buschmais.sarf.classification.configuration;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Stephan Pirnbaum
 */
public class ConfigurationHistory {

    private Map<Integer, ClassificationConfigurationIteration> history;

    private static ConfigurationHistory instance;

    private ConfigurationHistory() {
        this.history = new TreeMap<>();
    }

    public static ConfigurationHistory getInstance() {
        if (ConfigurationHistory.instance == null) {
            ConfigurationHistory.instance = new ConfigurationHistory();
        }
        return ConfigurationHistory.instance;
    }

    public boolean addIteration(ClassificationConfigurationIteration iteration) {
        return this.history.putIfAbsent(iteration.getIteration(), iteration) == null;
    }

    public ClassificationConfigurationIteration findByIteration(Integer iteration) {
        return this.history.get(iteration);
    }
}
