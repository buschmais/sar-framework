package com.buchmais.sarf.classification.configuration.logic;

import com.buchmais.sarf.classification.configuration.data.node.ClassificationConfigurationDescriptor;

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
