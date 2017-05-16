package com.buchmais.sarf.classification.configuration;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.criterion.ClassificationCriterion;
import com.buchmais.sarf.node.ClassificationConfigurationDescriptor;
import com.buchmais.sarf.node.ComponentDescriptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

/**
 * @author Stephan Pirnbaum
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@XmlRootElement(name = "Configuration")
public class ActiveClassificationConfiguration extends ClassificationConfiguration {

    private static ActiveClassificationConfiguration instance;

    private ActiveClassificationConfiguration(Integer iteration) {
        super(iteration);
    }

    public static ActiveClassificationConfiguration getInstance() {
        if (ActiveClassificationConfiguration.instance == null) {
            ActiveClassificationConfiguration.instance = new ActiveClassificationConfiguration(1);
        }
        return ActiveClassificationConfiguration.instance;
    }

    public boolean addClassificationCriterion(ClassificationCriterion classificationCriterion) {
        return this.classificationCriteria.add(classificationCriterion);
    }

    @Override
    public ClassificationConfiguration of(ClassificationConfigurationDescriptor classificationConfigurationDescriptor) { //todo static - factory?
        if (ActiveClassificationConfiguration.instance != null) {
            throw new IllegalStateException("Existing Singleton can't be overwritten");
        } else {
            ActiveClassificationConfiguration.instance = new ActiveClassificationConfiguration(classificationConfigurationDescriptor.getIteration());
            classificationConfigurationDescriptor.getClassificationCriteria().forEach(c -> {
                //ActiveClassificationConfiguration.instance.addClassificationCriterion(ClassificationCriterion.of(c)); todo
            });

        }
        return ActiveClassificationConfiguration.instance;
    }

    public ClassificationConfigurationIteration prepareNextIteration() {
        ClassificationConfigurationIteration iteration = new ClassificationConfigurationIteration(this.iteration);
        iteration.addAllClassificationCriterion(this.classificationCriteria);
        this.classificationCriteria = null;
        this.iteration++;
        return iteration;
    }

    public ActiveClassificationConfiguration rollback(ClassificationConfigurationIteration iteration) {
        this.iteration = iteration.iteration;
        this.classificationCriteria = iteration.classificationCriteria;
        return this;
    }

    public void execute() {
        for (ClassificationCriterion cC : this.classificationCriteria) {
            Set<ComponentDescriptor> res = cC.classify();
            SARFRunner.xoManager.currentTransaction().begin();
            res.forEach(c -> System.out.println(c.getShape() + " " + c.getName()));
            SARFRunner.xoManager.currentTransaction().commit();
        }
    }
}
