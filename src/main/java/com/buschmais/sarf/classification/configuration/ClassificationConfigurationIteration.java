package com.buschmais.sarf.classification.configuration;

/**
 * @author Stephan Pirnbaum
 */
public class ClassificationConfigurationIteration extends ClassificationConfiguration {
    ClassificationConfigurationIteration(Integer iteration) {
        super(iteration);
    }

    @Override
    public ClassificationConfiguration of(ClassificationConfigurationDescriptor classificationConfigurationDescriptor) {
        return null;
    }
}
