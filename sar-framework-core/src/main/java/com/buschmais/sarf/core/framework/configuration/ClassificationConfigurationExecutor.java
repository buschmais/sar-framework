package com.buschmais.sarf.core.framework.configuration;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.core.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.core.framework.repository.ComponentRepository;
import com.buschmais.sarf.core.framework.repository.TypeRepository;
import com.buschmais.sarf.core.plugin.api.ExecutedBy;
import com.buschmais.sarf.core.plugin.api.Executor;
import com.buschmais.sarf.core.plugin.api.criterion.ClassificationCriterionDescriptor;
import com.buschmais.sarf.core.plugin.api.criterion.ClassificationCriterionExecutor;
import com.buschmais.sarf.core.plugin.api.criterion.RuleBasedCriterionDescriptor;
import com.buschmais.sarf.core.plugin.chorddiagram.ChordDiagramExporter;
import com.buschmais.sarf.core.plugin.cohesion.CohesionCriterionDescriptor;
import com.buschmais.sarf.core.plugin.cohesion.CohesionCriterionExecutor;
import com.buschmais.sarf.core.plugin.treediagram.DendrogramExporter;
import com.buschmais.xo.api.CompositeObject;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.XOManager;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Executor implementation for the ClassificationConfigurationDescriptor.
 *
 * @author Stephan Pirnbaum
 */
@Service
@Lazy
@Slf4j
@RequiredArgsConstructor
public class ClassificationConfigurationExecutor implements Executor<ClassificationConfigurationDescriptor, ComponentDescriptor> {

    private final XOManager xoManager;
    private final BeanFactory beanFactory;
    private final ChordDiagramExporter chordDiagramExporter;
    private final DendrogramExporter dendrogramExporter;
    private final TypeRepository typeRepository;
    private final ComponentRepository componentRepository;

    /**
     * Executes the given {@link ClassificationConfigurationDescriptor} by executing following steps:
     *
     * <ul>
     *     <li>Executing all {@link RuleBasedCriterionDescriptor}s that were specified by the user</li>
     *     <li>Removing ambiguously assignments of types to components, i.e. types which were assigned to more than one conflicting component will be only assigned to the one with the highest probability</li>
     *     <li>Executing the {@link CohesionCriterionDescriptor} based on the results of the pre-assignment</li>
     *     <li>Intersecting the components identified by the evolutionary algorithms with those manually defined for better identification by name</li>
     * </ul>
     *
     * @param executableDescriptor The {@link ClassificationConfigurationDescriptor} to execute.
     *
     * @return The identified {@link ComponentDescriptor}s.
     *
     * @see ClassificationConfigurationExecutor#removeAmbiguities(Collection, Integer)
     * @see ClassificationConfigurationExecutor#intersectComponents(Collection)
     */
    @Override
    public Set<ComponentDescriptor> execute(ClassificationConfigurationDescriptor executableDescriptor) {
        LOGGER.info("Executing Classification");
        this.xoManager.currentTransaction().begin();
        final int iteration = executableDescriptor.getIteration();
        this.xoManager.currentTransaction().commit();
        this.xoManager.currentTransaction().begin();

        Set<RuleBasedCriterionDescriptor> ruleBasedCriteria = new HashSet<>();
        CohesionCriterionDescriptor cohesionCriterionDescriptor = null;

        Set<ClassificationCriterionDescriptor> classificationCriteria = executableDescriptor.getClassificationCriteria();
        for (ClassificationCriterionDescriptor cC : classificationCriteria) {
            if (cC instanceof RuleBasedCriterionDescriptor) {
                ruleBasedCriteria.add((RuleBasedCriterionDescriptor) cC);
            } else {
                cohesionCriterionDescriptor = (CohesionCriterionDescriptor) cC;
            }
        }

        // Step 1: Execute the user defined rules
        Set<ComponentDescriptor> components = executeRuleBaseCriteria(ruleBasedCriteria);

        // Step 2: Remove ambiguities from the result
        removeAmbiguities(components, iteration);

        // Step 3: Intersect the user defined components shape-wise
        createIntersectingComponents(components);

        if (cohesionCriterionDescriptor != null) {
            // Step 4: Execute the cohesion criterion
            Set<ComponentDescriptor> cohesionResult = executeCohesionCriterion(cohesionCriterionDescriptor, components);
            // Step 5: Merge user components with the result of the cohesion based classification
            components = mergeComponents(cohesionResult, components, iteration);
        }

        // Step 6: Export the result
        exportResults(components);
        LOGGER.info("Finished computation and export of results");
        return components;
    }

    /**
     * Method executing all user specified {@link RuleBasedCriterionDescriptor}s.
     *
     * @param ruleBasedCriteria The {@link RuleBasedCriterionDescriptor}s to execute.
     *
     * @return The created {@link ComponentDescriptor}s.
     */
    private Set<ComponentDescriptor> executeRuleBaseCriteria(Set<RuleBasedCriterionDescriptor> ruleBasedCriteria) {
        Set<ComponentDescriptor> components = new TreeSet<>((c1, c2) -> {
            int res = 0;
            if ((res = c1.getShape().compareTo(c2.getShape())) == 0) {
                res = c1.getName().compareTo(c2.getName());
            }
            return res;
        });

        for (RuleBasedCriterionDescriptor ruleBasedCriterion : ruleBasedCriteria) {
            Class<? extends Executor> executorClass = ruleBasedCriteria.getClass().getAnnotation(ExecutedBy.class).value();
            if (executorClass.isAssignableFrom(ClassificationCriterionExecutor.class)) {
                @SuppressWarnings("unchecked")
                ClassificationCriterionExecutor<ClassificationCriterionDescriptor> executor =
                    this.beanFactory.getBean((Class<ClassificationCriterionExecutor>) executorClass);
                components.addAll(executor.execute(ruleBasedCriterion));
            }
        }

        this.xoManager.currentTransaction().commit();

        return components;
    }

    /**
     * Method identifying ambiguously assignments from {@link TypeDescriptor}s to {@link ComponentDescriptor}s. An assignment is
     * ambiguous if there is at least another assignment of the same {@link TypeDescriptor} to a {@link ComponentDescriptor} of the
     * same {@link ComponentDescriptor#getShape()}.
     *
     * The assignment with the higher probability is kept, all others are removed.
     *
     * @param components The {@link ComponentDescriptor}s to check for ambiguities.
     * @param iteration The current iteration.
     */
    private void removeAmbiguities(Collection<ComponentDescriptor> components, Integer iteration) {
        LOGGER.info("Removing Ambiguities from Pre-Partitioning");
        this.xoManager.currentTransaction().begin();
        int removed = 0;
        for (ComponentDescriptor component1 : components) {
            Long id1 = this.xoManager.getId(component1);
            for (ComponentDescriptor component2 : components) {
                Long id2 = this.xoManager.getId(component2);
                if (component1.getShape().equals(component2.getShape()) && !component1.getName().equals(component2.getName())) {
                    Result<TypeDescriptor> types = typeRepository.getTypesPreAssignedTo(id1, id2, iteration);
                    for (TypeDescriptor type : types) {
                        Double weightC1 = typeRepository.getAssignmentWeight(this.xoManager.getId(type), id1, iteration);
                        Double weightC2 = typeRepository.getAssignmentWeight(this.xoManager.getId(type), id2, iteration);
                        if (weightC1 < weightC2 || weightC2 < weightC1) {
                            LOGGER.info("\tDetected Ambiguity:");
                            LOGGER.info("\t\tFQN: " + type.getFullQualifiedName());
                            LOGGER.info("\t\tComponent 1: " + component1.getShape() + " - " + component1.getName() + " Weight: " + weightC1);
                            LOGGER.info("\t\tComponent 2: " + component2.getShape() + " - " + component2.getName() + " Weight: " + weightC2);
                            LOGGER.info("\t\tRemoving Assignment to: " + (weightC1 < weightC2 ? component1.getShape() + " - " + component1.getName()
                                : component2.getShape() + " - " + component2.getName()));
                        }
                        if (weightC1 < weightC2) {
                            typeRepository.removeAssignment(this.xoManager.getId(type), id1, iteration);
                        } else {
                            typeRepository.removeAssignment(this.xoManager.getId(type), id2, iteration);
                        }
                        removed++;
                    }
                    types.close();
                }
            }
        }
        this.xoManager.currentTransaction().commit();
        LOGGER.info("\tRemoved " + removed + " Assignments");
    }

    /**
     * Intersect the {@link ComponentDescriptor}s which resulted from the execution of the user rules. Intersecting components is done
     * by taking the {@link ComponentDescriptor#getShape()} and the contained {@link TypeDescriptor}s into account. If one
     * {@link TypeDescriptor} is part of multiple {@link ComponentDescriptor}s, then these {@link ComponentDescriptor}s
     * will be intersected.
     *
     * @param components The {@link ComponentDescriptor}s to intersect.
     *
     * @return The intersected {@link ComponentDescriptor}s.
     */
    private Set<ComponentDescriptor> createIntersectingComponents(Collection<ComponentDescriptor> components) {
        ArrayListMultimap<Collection<Long>, Long> inverse = intersectComponents(components);
        Set<ComponentDescriptor> result = new HashSet<>();
        this.xoManager.currentTransaction().begin();
        List<Long> ids = new ArrayList<>();
        for (Collection<Long> componentIds : inverse.keys().elementSet()) {
            if (componentIds.size() > 1) {
                ComponentDescriptor componentDescriptor = this.xoManager.create(ComponentDescriptor.class);
                componentDescriptor.setShape("Component");
                componentDescriptor.setName("");
                ids.add(this.xoManager.getId(componentDescriptor));
                for (Long componentId : componentIds) {
                    ComponentDescriptor containing = this.xoManager.findById(ComponentDescriptor.class, componentId);
                    containing.getContainedComponents().add(componentDescriptor);
                    componentDescriptor.setName(componentDescriptor.getName() + " - " + containing.getName());
                    result.add(containing);
                    ids.add(componentId);

                }
                for (Long typeId : inverse.get(componentIds)) {
                    componentDescriptor.getContainedTypes().add(this.xoManager.findById(TypeDescriptor.class, typeId));
                }
            } else if (componentIds.size() == 1) {
                Long id = componentIds.iterator().next();
                ComponentDescriptor singleComponent = this.xoManager.findById(ComponentDescriptor.class, id);
                ids.add(id);
                for (Long typeId : inverse.get(componentIds)) {
                    singleComponent.getContainedTypes().add(this.xoManager.findById(TypeDescriptor.class, typeId));
                }
                result.add(singleComponent);
            }
        }
        this.componentRepository.computeCouplingBetweenComponents(ids.stream().mapToLong(l -> l).toArray());
        this.xoManager.currentTransaction().commit();
        return result;
    }

    private ArrayListMultimap<Collection<Long>, Long> intersectComponents(Collection<ComponentDescriptor> components) {
        LOGGER.info("Creating Intersecting Components");
        this.xoManager.currentTransaction().begin();
        // Type ID -> Component IDs
        Map<Long, Collection<Long>> typeToComponents = new HashMap<>();
        Set<String> shapes = components.stream().map(ComponentDescriptor::getShape).collect(Collectors.toSet());
        List<TypeDescriptor> types = new ArrayList<>();
        try (Result<TypeDescriptor> descriptors = this.typeRepository.getAllInternalTypes()) {
            for (TypeDescriptor t : descriptors) {
                types.add(t);
            }
        }
        long[] ids = types.stream().mapToLong(t -> this.xoManager.getId(t)).sorted().toArray();
        for (Long typeId : ids) {
            for (String shape : shapes) {
                Long bestCId = componentRepository.getBestComponentForShape(components.stream().mapToLong(c -> this.xoManager.getId(c)).toArray(),
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
        ArrayListMultimap<Collection<Long>, Long> inverse = Multimaps.invertFrom(Multimaps.forMap(typeToComponents), ArrayListMultimap.create());
        this.xoManager.currentTransaction().commit();
        return inverse;
    }

    /**
     * Execute the {@link CohesionCriterionDescriptor} with the given components as the start partitioning. If no start components are specified,
     * the package structure will be used.
     *
     * @param cohesionCriterionDescriptor The {@link CohesionCriterionDescriptor} to execute.
     * @param components The {@link ComponentDescriptor}s providing information for the start generation.
     */
    private Set<ComponentDescriptor> executeCohesionCriterion(CohesionCriterionDescriptor cohesionCriterionDescriptor, Set<ComponentDescriptor> components) {
        Set<ComponentDescriptor> cohesionResult;
        CohesionCriterionExecutor cohesionCriterionExecutor = this.beanFactory.getBean(CohesionCriterionExecutor.class);
        if (CollectionUtils.isNotEmpty(components)) {
            cohesionResult = cohesionCriterionExecutor.execute(cohesionCriterionDescriptor, components);
        } else {
            cohesionResult = cohesionCriterionExecutor.execute(cohesionCriterionDescriptor);
        }
        return cohesionResult;
    }


    private Set<ComponentDescriptor> mergeComponents(Set<ComponentDescriptor> cohesionResult, Set<ComponentDescriptor> userResult, Integer iteration) {
        //so we have several solutions, time to make one out of them :)
        // 2 types of identified components exist:
        // -user-defined ones
        // -self-created ones (start with a hash # sign)
        //
        for (ComponentDescriptor userComponent : userResult) {
            for (ComponentDescriptor cohesionComponent : cohesionResult) {
                this.xoManager.currentTransaction().begin();
                LOGGER.debug("Trying to merge user component: {}:{} with cohesion component: {}:{}",
                    userComponent.getShape(), userComponent.getName(), cohesionComponent.getShape(), cohesionComponent.getName());
                Double tversky = computeTverskyIndex(componentRepository, userComponent, cohesionComponent, iteration);
                if (tversky > 0.75) {
                    LOGGER.info("Merging user component {}:{} with cohesion component: {}:{} (Tversky Index: {})",
                        userComponent.getShape(), userComponent.getName(), cohesionComponent.getShape(), cohesionComponent.getName(), tversky);
                    cohesionComponent.setShape(userComponent.getShape());
                    cohesionComponent.setName(userComponent.getName());
                }
                this.xoManager.currentTransaction().commit();

                // check for contained components
                mergeComponents(cohesionComponent.getContainedComponents(), userResult, iteration);
            }
        }
        return cohesionResult;
    }

    private Double computeTverskyIndex(ComponentRepository componentRepository, ComponentDescriptor userComponent, ComponentDescriptor cohesionComponent, Integer iteration) {
        double jaccard = componentRepository.computeJaccardSimilarity(
            cohesionComponent.getShape(), cohesionComponent.getName(),
            userComponent.getShape(), userComponent.getName(),
            iteration);
        Long intersection = componentRepository.computeComponentIntersectionCardinality(
            cohesionComponent.getShape(), cohesionComponent.getName(),
            userComponent.getShape(), userComponent.getName(),
            iteration);
        Long cohesionComponentCardinality = componentRepository.computeComponentCardinality(cohesionComponent.getShape(), cohesionComponent.getName(), iteration);
        Long userComponentCardinality = componentRepository.computeComponentCardinality(userComponent.getShape(), userComponent.getName(), iteration);
        Long ofCD1InCD2 = componentRepository.computeComplementCardinality(cohesionComponent.getShape(), cohesionComponent.getName(), userComponent.getShape(), userComponent.getName(), iteration);
        Long ofCD2InCD1 = componentRepository.computeComplementCardinality(userComponent.getShape(), userComponent.getName(), cohesionComponent.getShape(), cohesionComponent.getName(), iteration);
        double alpha = 0.8;
        double beta = 0.6;
        Double tversky = intersection.doubleValue() / (intersection + beta * (alpha * Math.min(ofCD2InCD1, ofCD1InCD2) + (1 - alpha) * Math.max(ofCD2InCD1, ofCD1InCD2)));
        LOGGER.debug("Jaccard: {}", jaccard);
        LOGGER.debug("Cardinality 1: {}", cohesionComponentCardinality);
        LOGGER.debug("Cardinality 2: {}", userComponentCardinality);
        LOGGER.debug("Intersection: {}", intersection);
        LOGGER.debug("Tversky: {}", + tversky);
        return tversky;
    }

    private void exportResults(Set<ComponentDescriptor> components) {
        this.xoManager.currentTransaction().begin();
        LOGGER.info("Pretty Printing the Result");
        DateTimeFormatter resultFileFormatter = new DateTimeFormatterBuilder()
            .appendLiteral("Result_")
            .appendValue(ChronoField.YEAR, 4)
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral('_')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .appendLiteral(".zip")
            .toFormatter();

        try {
            FileOutputStream fileOutputStream =
                new FileOutputStream(LocalDateTime.now().format(resultFileFormatter));
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
            // add resource files
            List<String> resources = Arrays
                .asList("circle-packing.html", "circle-packing-convert.js", "d3.min.js", "chord-diagram.html",
                    "chord-jsonMapper.js", "chord-jsonScript.js", "chord-style.css",
                    "dendrogram-interactive.html", "dendrogram-interactive.js",
                    "dendrogram-radial.html", "dendrogram-radial.js", "index.html");
            for (String resource : resources) {
                ZipEntry entry = new ZipEntry(resource);
                InputStream in = ClassificationConfigurationExecutor.class.getClassLoader().getResourceAsStream(resource);
                zipOutputStream.putNextEntry(entry);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = in.read(bytes)) >= 0) {
                    zipOutputStream.write(bytes, 0, length);
                }
                in.close();
            }
            // write formatted text
            ZipEntry entry = new ZipEntry("decomposition.txt");
            zipOutputStream.putNextEntry(entry);
            StringBuilder formatted = new StringBuilder();
            prettyPrint(components, "", formatted);
            zipOutputStream.write(formatted.toString().getBytes());
            // write json
            entry = new ZipEntry("sarf.json");
            Result<Map> result
                = componentRepository.getDecomposition(components.stream().mapToLong(c -> this.xoManager.getId(c)).toArray());
            zipOutputStream.putNextEntry(entry);
            formatted = new StringBuilder();
            zipOutputStream.write(("[\n").getBytes());
            String entries = StreamSupport.stream(result.spliterator(), false)
                .map(this::formatEntry)
                .reduce((s1, s2) -> s1 + ",\n" + s2).get();
            zipOutputStream.write(entries.getBytes());
            zipOutputStream.write(("\n]").getBytes());
            zipOutputStream.write(formatted.toString().getBytes());
            // write chord
            entry = new ZipEntry("chord-data.json");
            zipOutputStream.putNextEntry(entry);
            zipOutputStream.write(this.chordDiagramExporter.export(components).getBytes());
            // write tree diagram data
            entry = new ZipEntry("dendrogram-data.json");
            zipOutputStream.putNextEntry(entry);
            zipOutputStream.write(this.dendrogramExporter.export(components).getBytes());
            // close streams
            zipOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.xoManager.currentTransaction().commit();
    }

    private void prettyPrint(Collection<ComponentDescriptor> components, String indentation, StringBuilder builder) throws IOException {
        for (ComponentDescriptor component : components) {
            builder.append(indentation + " " + component.getName() + " " + Arrays.toString(component.getTopWords()) + "\n");
            Result<Result.CompositeRowObject> res = this.xoManager.createQuery("MATCH (c) WHERE ID(c) = " + this.xoManager.getId(component) + " " +
                "OPTIONAL MATCH (c)-[:CONTAINS]->(e) RETURN e").execute(); // TODO: 05.07.2017 Improve !!!
            Set<ComponentDescriptor> componentDescriptors = new HashSet<>();
            for (Result.CompositeRowObject r : res) {
                try {
                    ComponentDescriptor c = r.get("e", ComponentDescriptor.class);
                    if (c != null) {
                        componentDescriptors.add(c);
                    }
                } catch (ClassCastException e) {
                    TypeDescriptor t = r.get("e", TypeDescriptor.class);
                    if (t != null) {
                        builder.append(indentation + "\t" + t.getFullQualifiedName() + "\n");
                    }
                }
            }
            res.close();
            prettyPrint(componentDescriptors, indentation + "\t", builder);
        }
    }

    private String formatEntry(Map m) {
        StringBuilder formatted = new StringBuilder();
        formatted.append("\t{\n");
        formatted.append("\t\t\"entry\" : [\n");
        formatted.append("\t\t\t{\n");
        ComponentDescriptor c = (ComponentDescriptor) m.get("c");
        formatted.append("\t\t\t\t\"shape\": \"" + c.getShape() + "\",\n");
        formatted.append("\t\t\t\t\"name\": \"" + c.getName() + "\",\n");
        formatted.append("\t\t\t\t\"topWords\": [" +
            Arrays.stream(c.getTopWords()).map(s -> "\"" + s + "\"").reduce((s1, s2) -> s1 + ", " + s2).get() + "]\n"
        );
        formatted.append("\t\t\t},\n");
        CompositeObject cont = (CompositeObject) m.get("cont");
        if (cont == null) {
            formatted.append("\t\t\tnull\n");
        } else if (m.get("c1") instanceof ComponentDescriptor) {
            c = (ComponentDescriptor) m.get("c1");
            formatted.append("\t\t\t{\n")
                .append("\t\t\t\t\"shape\": \"").append(c.getShape()).append("\",\n")
                .append("\t\t\t\t\"name\": \"").append(c.getName()).append("\",\n")
                .append("\t\t\t\t\"topWords\": [").append(Arrays.stream(c.getTopWords()).map(s -> "\"" + s + "\"").reduce((s1, s2) -> s1 + ", " + s2).get()).append("]\n")
                .append("\t\t\t}\n");
        } else {
            TypeDescriptor t = (TypeDescriptor) m.get("c1");
            formatted.append("\t\t\t{\n");
            formatted.append("\t\t\t\t\"id\": \"" + this.xoManager.getId(t) + "\",\n");
            formatted.append("\t\t\t\t\"fqn\": \"" + t.getFullQualifiedName() + "\",\n");
            formatted.append("\t\t\t\t\"name\": \"" + t.getName() + "\"\n");
            formatted.append("\t\t\t}\n");
        }
        formatted.append("\t\t]\n\t}\n");
        return formatted.toString();
    }
}
