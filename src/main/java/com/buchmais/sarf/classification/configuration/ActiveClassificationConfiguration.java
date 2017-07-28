package com.buchmais.sarf.classification.configuration;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.criterion.ClassificationCriterion;
import com.buchmais.sarf.classification.criterion.Rule;
import com.buchmais.sarf.classification.criterion.RuleBasedCriterion;
import com.buchmais.sarf.classification.criterion.cohesion.CohesionCriterion;
import com.buchmais.sarf.metamodel.Component;
import com.buchmais.sarf.metamodel.ComponentDescriptor;
import com.buchmais.sarf.repository.ComponentRepository;
import com.buchmais.sarf.repository.TypeRepository;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Stephan Pirnbaum
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@XmlRootElement(name = "Configuration")
public class ActiveClassificationConfiguration extends ClassificationConfiguration {

    private static final Logger LOG = LogManager.getLogger(ActiveClassificationConfiguration.class);

    private static ActiveClassificationConfiguration instance;

    public ActiveClassificationConfiguration(Integer iteration) {
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
            ActiveClassificationConfiguration.instance.setDecomposition(Decomposition.valueOf(classificationConfigurationDescriptor.getDecomposition()));
            ActiveClassificationConfiguration.instance.setOptimization(Optimization.valueOf(classificationConfigurationDescriptor.getOptimization()));
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

    public Set<ComponentDescriptor> execute() {
        LOG.info("Executing Classification");
        Set<ComponentDescriptor> components = new TreeSet<>((c1, c2) -> {
            int res = 0;
            if ((res = c1.getShape().compareTo(c2.getShape())) == 0) {
                res = c1.getName().compareTo(c2.getName());
            }
            return res;
        });
        CohesionCriterion cohesionCriterion  = null;
        for (ClassificationCriterion cC : this.classificationCriteria) {
            if (!(cC instanceof CohesionCriterion)) {
                Set<ComponentDescriptor> res = cC.classify(this.iteration);
                SARFRunner.xoManager.currentTransaction().begin();
                components.addAll(res);
                SARFRunner.xoManager.currentTransaction().commit();
            } else {
                cohesionCriterion = (CohesionCriterion) cC;
            }
        }
        removeAmbiguities(components);
        //combine(components);
        Set<ComponentDescriptor> cohesionResult = null;
        if (cohesionCriterion != null) {
            cohesionResult = cohesionCriterion.classify(this.iteration, identifyIntersectingComponents(components),
                    getDecomposition() == Decomposition.DEEP, getOptimization() == Optimization.SIMILARITY);
            // match with manual classification
            components = cohesionResult;
        } else {
            components = createIntersectingComponents(components);
        }
        //finalize(components);

        SARFRunner.xoManager.currentTransaction().begin();
        LOG.info("Pretty Printing the Result");
        try (FileWriter fW = new FileWriter("Result_" + System.currentTimeMillis())) {
            BufferedWriter bW = new BufferedWriter(fW);
            PrintWriter pW = new PrintWriter(bW);
            prettyPrint(components, "", pW);
            pW.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SARFRunner.xoManager.currentTransaction().commit();
        return components;
    }

    public static void prettyPrint(Set<ComponentDescriptor> components, String indentation , PrintWriter pW) {
        for (ComponentDescriptor component : components) {
            pW.println(indentation + " " + component.getName() + " " + Arrays.toString(component.getTopWords()));
            Result<CompositeRowObject > res = SARFRunner.xoManager.createQuery("MATCH (c) WHERE ID(c) = " + SARFRunner.xoManager.getId(component) + " " +
                    "OPTIONAL MATCH (c)-[:CONTAINS]->(e) RETURN e").execute(); // TODO: 05.07.2017 Improve !!!
            Set<ComponentDescriptor> componentDescriptors = new HashSet<>();
            for (CompositeRowObject r : res) {
                try {
                    ComponentDescriptor c = r.get("e", ComponentDescriptor.class);
                    if (c != null) {
                        componentDescriptors.add(c); // 19269, 19285, 19292
                    }
                } catch (ClassCastException e) {
                    TypeDescriptor t = r.get("e", TypeDescriptor.class);
                    if (t != null) {
                        pW.println(indentation + "\t" + t.getName());
                    }
                }
            }
            res.close();
            prettyPrint(componentDescriptors, indentation + "\t", pW);
        }
      }

    private void removeAmbiguities(Set<ComponentDescriptor> components) {
        LOG.info("Removing Ambiguities from Pre-Partitioning");
        SARFRunner.xoManager.currentTransaction().begin();
        TypeRepository typeRepository = SARFRunner.xoManager.getRepository(TypeRepository.class);
        int removed = 0;
        for (ComponentDescriptor component1 : components) {
            Long id1 = SARFRunner.xoManager.getId(component1);
            for (ComponentDescriptor component2 : components) {
                Long id2 = SARFRunner.xoManager.getId(component2);
                if (component1.getShape().equals(component2.getShape()) && !component1.getName().equals(component2.getName())) {
                    Result<TypeDescriptor> types = typeRepository.getTypesPreAssignedTo(id1, id2, this.iteration);
                    for (TypeDescriptor type : types) {
                        Double weightC1 = typeRepository.getAssignmentWeight(SARFRunner.xoManager.getId(type), id1, this.iteration);
                        Double weightC2 = typeRepository.getAssignmentWeight(SARFRunner.xoManager.getId(type), id2, this.iteration);
                        if (weightC1 - weightC2 < 1 || weightC2 - weightC1 > 1) { // TODO: 04.07.2017 change threshold for productive usage
                            LOG.info("\tDetected Ambiguity:");
                            LOG.info("\t\tFQN: " + type.getFullQualifiedName());
                            LOG.info("\t\tComponent 1: " + component1.getShape() + " - " + component1.getName() + " Weight: " + weightC1);
                            LOG.info("\t\tComponent 2: " + component2.getShape() + " - " + component2.getName() + " Weight: " + weightC2);
                            LOG.info("\t\tRemoving Assignment to: " + (weightC1 < weightC2 ? component1.getShape() + " - " + component1.getName()
                                                                                              : component2.getShape() + " - " + component2.getName()));
                        }
                        if (weightC1 < weightC2) {
                            typeRepository.removeAssignment(SARFRunner.xoManager.getId(type), id1, this.iteration);
                        } else {
                            typeRepository.removeAssignment(SARFRunner.xoManager.getId(type), id2, this.iteration);
                        }
                        removed++;
                    }
                    types.close();
                }
            }
        }
        SARFRunner.xoManager.currentTransaction().commit();
        LOG.info("\tRemoved " + removed + " Assignments");
    }

    private Set<ComponentDescriptor> createIntersectingComponents(Set<ComponentDescriptor> components) {
        ArrayListMultimap<Set<Long>, Long> inverse = intersectComponents(components);
        Set<ComponentDescriptor> result = new HashSet<>();
        SARFRunner.xoManager.currentTransaction().begin();
        List<Long> ids = new ArrayList<>();
        for (Set<Long> componentIds : inverse.keys().elementSet()) {
            if (componentIds.size() > 1) {
                ComponentDescriptor componentDescriptor = SARFRunner.xoManager.create(ComponentDescriptor.class);
                componentDescriptor.setShape("Component");
                componentDescriptor.setName("");
                ids.add(SARFRunner.xoManager.getId(componentDescriptor));
                for (Long componentId : componentIds) {
                    ComponentDescriptor containing = SARFRunner.xoManager.findById(ComponentDescriptor.class, componentId);
                    containing.getContainedComponents().add(componentDescriptor);
                    componentDescriptor.setName(componentDescriptor.getName() + " - " + containing.getName());
                    result.add(containing);
                    ids.add(componentId);

                }
                for (Long typeId : inverse.get(componentIds)) {
                    componentDescriptor.getContainedTypes().add(SARFRunner.xoManager.findById(TypeDescriptor.class, typeId));
                }
            } else if (componentIds.size() == 1){
                Long id = componentIds.iterator().next();
                ComponentDescriptor singleComponent = SARFRunner.xoManager.findById(ComponentDescriptor.class, id);
                ids.add(id);
                for (Long typeId : inverse.get(componentIds)) {
                    singleComponent.getContainedTypes().add(SARFRunner.xoManager.findById(TypeDescriptor.class, typeId));
                }
                result.add(singleComponent);
            }
        }
        SARFRunner.xoManager.getRepository(ComponentRepository.class).computeCouplingBetweenComponents(ids.stream().mapToLong(l -> l).toArray());
        SARFRunner.xoManager.currentTransaction().commit();
        return result;
    }

    /**
     *
     * @param components
     * @return Mapping from component id (ranges from 0 to the number of identified components) to type ids (actual ids from the graph database)
     */
    private Map<Long, Set<Long>> identifyIntersectingComponents(Set<ComponentDescriptor> components) {
        Map<Long, Set<Long>> componentMappings = new HashMap<>();
        Long componentId = 0L;
        ArrayListMultimap<Set<Long>, Long> inverse = intersectComponents(components);
        for (Set<Long> componentIds : inverse.keys().elementSet()) {
            componentMappings.put(componentId, Sets.newHashSet(inverse.get(componentIds)));
            componentId++;
        }
        return componentMappings;
    }

    /**
     * Merges the shapes and names specified by the user as far as possible into the results of the automatic recovery
     * @param prePartitioning
     * @param partitioning
     * @return
     */
    private Set<ComponentDescriptor> mergeResults(Set<ComponentDescriptor> prePartitioning, Set<ComponentDescriptor> partitioning) {
        return  null;
    }

    private ArrayListMultimap<Set<Long>, Long> intersectComponents(Set<ComponentDescriptor> components) {
        LOG.info("Creating Intersecting Components");
        SARFRunner.xoManager.currentTransaction().begin();
        ComponentRepository componentRepository = SARFRunner.xoManager.getRepository(ComponentRepository.class);
        // Type ID -> Component IDs
        Map<Long, Set<Long>> typeToComponents = new HashMap<>();
        Set<String> shapes = components.stream().map(ComponentDescriptor::getShape).collect(Collectors.toSet());
        List<TypeDescriptor> types = new ArrayList<>();
        try (Result<TypeDescriptor> descriptors = SARFRunner.xoManager.getRepository(TypeRepository.class).getAllInternalTypes()) {
            for (TypeDescriptor t : descriptors) {
                types.add(t);
            }
        }
        long[] ids = types.stream().mapToLong(t -> SARFRunner.xoManager.getId(t)).sorted().toArray();
        for (Long typeId : ids) {
            for (String shape : shapes) {
                Long bestCId = componentRepository.getBestComponentForShape(components.stream().mapToLong(c -> SARFRunner.xoManager.getId(c)).toArray(),
                        shape, typeId);
                if (bestCId != null) {
                    typeToComponents.merge(
                            typeId,
                            Sets.newHashSet(bestCId),
                            (c1, c2) -> {
                                c1.addAll(c2);
                                return c1;
                            }
                    );
                }
            }
        }
        // Create Intersecting Components and assign the types to them
        ArrayListMultimap<Set<Long>, Long> inverse = Multimaps.invertFrom(Multimaps.forMap(typeToComponents), ArrayListMultimap.create());
        SARFRunner.xoManager.currentTransaction().commit();
        return inverse;
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
                SARFRunner.xoManager.currentTransaction().begin();
                if (cD1 != cD2 && (cD1.getShape().equals(cD2.getShape()))) {




                    System.out.println(cD1.getShape() + " " + cD1.getName() + " With: " + cD2.getShape() + " " + cD2.getName());
                    double jaccard = componentRepository.computeJaccardSimilarity(
                            cD1.getShape(), cD1.getName(),
                            cD2.getShape(), cD2.getName(),
                            this.iteration);
                    Long intersection = componentRepository.computeComponentIntersectionCardinality(
                            cD1.getShape(), cD1.getName(),
                            cD2.getShape(), cD2.getName(),
                            this.iteration);
                    Long cardinality1 = componentRepository.computeComponentCardinality(cD1.getShape(), cD1.getName(), this.iteration);
                    Long cardinality2 = componentRepository.computeComponentCardinality(cD2.getShape(), cD2.getName(), this.iteration);
                    Long ofCD1InCD2 = componentRepository.computeComplementCardinality(cD1.getShape(), cD1.getName(), cD2.getShape(), cD2.getName(), iteration);
                    Long ofCD2InCD1 = componentRepository.computeComplementCardinality(cD2.getShape(), cD2.getName(), cD1.getShape(), cD1.getName(), iteration);
                    double alpha = 0.8;
                    double beta = 0.6;
                    Double tversky = intersection.doubleValue() / (intersection + beta * (alpha * Math.min(ofCD2InCD1, ofCD1InCD2) + (1 - alpha) * Math.max(ofCD2InCD1, ofCD1InCD2)));
                    /*System.out.println("Jaccard: " + jaccard);
                    System.out.println("Cardinality 1: " + cardinality1);
                    System.out.println("Cardinality 2: " + cardinality2);
                    System.out.println("Intersection: " + intersection);
                    System.out.println("Tversky: " + tversky);*/
                }
                SARFRunner.xoManager.currentTransaction().commit();
            }
        }
    }

    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        Set<Rule> rules = flatten(this.model);
        Map<Class<? extends RuleBasedCriterion>, RuleBasedCriterion> criteriaMapping = new HashMap<>();
        for (Rule<?> r : rules) {
            try {
                criteriaMapping.merge(r.getAssociateCriterion(), r.getAssociateCriterion().newInstance().addRule(r), (c1, c2) -> {
                    c1.addRule(r);
                    return c1;
                });
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        this.classificationCriteria = Sets.newTreeSet(criteriaMapping.values());
    }

    private Set<Rule> flatten(Set<Component> components) {
        Set<Rule> rules = new TreeSet<>();
        for (Component component : components) {
            if (component.getIdentifyingRules() != null) {
                rules.addAll(component.getIdentifyingRules());
            }
            if (component.getContainedComponents() != null) {
                rules.addAll(flatten(component.getContainedComponents()));
            }
        }
        return rules;
    }
}
