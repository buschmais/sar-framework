package com.buchmais.sarf.classification.configuration;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.criterion.ClassificationCriterion;
import com.buchmais.sarf.node.ClassificationConfigurationDescriptor;
import com.buchmais.sarf.node.ComponentDescriptor;
import com.buchmais.sarf.repository.ComponentRepository;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;
import java.util.TreeSet;

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
        Set<ComponentDescriptor> components = new TreeSet<>((c1, c2) -> {
            int res = 0;
            if ((res = c1.getShape().compareTo(c2.getShape())) == 0) {
                res = c1.getName().compareTo(c2.getName());
            }
            return res;
        });
        for (ClassificationCriterion cC : this.classificationCriteria) {
            Set<ComponentDescriptor> res = cC.classify(this.iteration);
            SARFRunner.xoManager.currentTransaction().begin();
            res.forEach(c -> System.out.println(c.getShape() + " " + c.getName()));
            components.addAll(res);
            SARFRunner.xoManager.currentTransaction().commit();
        }
        combine(components);
    }

    public void combine(Set<ComponentDescriptor> components) {
        //so we have several solutions, time to make one out of them :)
        // 2 types of identified components exist:
        // -user-defined ones
        // -self-created ones (start with a hash # sign)
        //
        ComponentRepository componentRepository = SARFRunner.xoManager.getRepository(ComponentRepository.class);
        for (ComponentDescriptor cD1 : components) {
            for (ComponentDescriptor cD2 : components) {
                if (cD1 != cD2) {
                    SARFRunner.xoManager.currentTransaction().begin();
                    System.out.println("Jaccard of: " + cD1.getShape() + " " + cD1.getName() + " With: " + cD2.getShape() + " " + cD2.getName());
                    double jaccard = componentRepository.computeJaccardSimilarity(
                            cD1.getShape(), cD1.getName(),
                            cD2.getShape(), cD2.getName(),
                            this.iteration);
                    System.out.println(jaccard);
                    SARFRunner.xoManager.currentTransaction().commit();
                }
            }
        }
    }
}
