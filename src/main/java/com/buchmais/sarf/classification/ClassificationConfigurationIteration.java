package com.buchmais.sarf.classification;

import com.buchmais.sarf.node.ClassificationConfigurationDescriptor;

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
