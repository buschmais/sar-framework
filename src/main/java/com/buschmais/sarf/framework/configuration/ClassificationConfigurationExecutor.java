package com.buschmais.sarf.framework.configuration;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.SARFRunner;
import com.buschmais.sarf.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.framework.repository.ComponentRepository;
import com.buschmais.sarf.framework.repository.TypeRepository;
import com.buschmais.sarf.plugin.api.ExecutedBy;
import com.buschmais.sarf.plugin.api.Executor;
import com.buschmais.sarf.plugin.api.criterion.ClassificationCriterionDescriptor;
import com.buschmais.sarf.plugin.api.criterion.ClassificationCriterionExecutor;
import com.buschmais.sarf.plugin.api.criterion.RuleBasedCriterionDescriptor;
import com.buschmais.sarf.plugin.chorddiagram.ChordDiagramExporter;
import com.buschmais.sarf.plugin.cohesion.CohesionCriterionDescriptor;
import com.buschmais.sarf.plugin.cohesion.CohesionCriterionExecutor;
import com.buschmais.xo.api.CompositeObject;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.XOManager;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Stephan Pirnbaum
 */
@Service
@Lazy
public class ClassificationConfigurationExecutor implements Executor<ClassificationConfigurationDescriptor, ComponentDescriptor> {

    private Logger LOG = LogManager.getLogger(ClassificationConfigurationExecutor.class);

    private XOManager xoManager;
    private BeanFactory beanFactory;
    private ChordDiagramExporter chordDiagramExporter;

    @Autowired
    public ClassificationConfigurationExecutor(XOManager xoManager, BeanFactory beanFactory, ChordDiagramExporter chordDiagramExporter) {
        this.xoManager = xoManager;
        this.beanFactory = beanFactory;
        this.chordDiagramExporter = chordDiagramExporter;
    }

    @Override
    public Set<ComponentDescriptor> execute(ClassificationConfigurationDescriptor executableDescriptor) {
        LOG.info("Executing Classification");
        this.xoManager.currentTransaction().begin();
        final int iteration = executableDescriptor.getIteration();
        this.xoManager.currentTransaction().commit();
        Set<ComponentDescriptor> components = new TreeSet<>((c1, c2) -> {
            int res = 0;
            if ((res = c1.getShape().compareTo(c2.getShape())) == 0) {
                res = c1.getName().compareTo(c2.getName());
            }
            return res;
        });
        CohesionCriterionDescriptor cohesionCriterionDescriptor = null;
        this.xoManager.currentTransaction().begin();
        Set<ClassificationCriterionDescriptor> classificationCriteria = executableDescriptor.getClassificationCriteria();
        for (ClassificationCriterionDescriptor cC : classificationCriteria) {
            if (cC instanceof RuleBasedCriterionDescriptor) {
                Class<? extends Executor> executorClass = cC.getClass().getAnnotation(ExecutedBy.class).value();
                if (executorClass.isAssignableFrom(ClassificationCriterionExecutor.class)) {
                    @SuppressWarnings("unchecked")
                    ClassificationCriterionExecutor<ClassificationCriterionDescriptor> executor =
                        this.beanFactory.getBean((Class<ClassificationCriterionExecutor>) executorClass);
                    components.addAll(executor.execute(cC));
                }
            } else {
                cohesionCriterionDescriptor = (CohesionCriterionDescriptor) cC;
            }
        }
        this.xoManager.currentTransaction().commit();
        removeAmbiguities(components, iteration);

        //combine(components);
        Set<ComponentDescriptor> cohesionResult = null;
        if (cohesionCriterionDescriptor != null)

        {
            CohesionCriterionExecutor cohesionCriterionExecutor = this.beanFactory.getBean(CohesionCriterionExecutor.class);
            cohesionResult = cohesionCriterionExecutor.execute(cohesionCriterionDescriptor);
            // match with manual classification
            components = cohesionResult;
        } else

        {
            components = createIntersectingComponents(components);
        }
        //finalize(components);

        this.xoManager.currentTransaction().

            begin();
        LOG.info("Pretty Printing the Result");

        exportResults(components);
        this.xoManager.currentTransaction().commit();
        return components;
    }

    private void removeAmbiguities(Collection<ComponentDescriptor> components, Integer iteration) {
        LOG.info("Removing Ambiguities from Pre-Partitioning");
        this.xoManager.currentTransaction().begin();
        TypeRepository typeRepository = this.xoManager.getRepository(TypeRepository.class);
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
                        if (weightC1 - weightC2 < 1 || weightC2 - weightC1 > 1) { // TODO: 04.07.2017 change threshold for productive usage
                            LOG.info("\tDetected Ambiguity:");
                            LOG.info("\t\tFQN: " + type.getFullQualifiedName());
                            LOG.info("\t\tComponent 1: " + component1.getShape() + " - " + component1.getName() + " Weight: " + weightC1);
                            LOG.info("\t\tComponent 2: " + component2.getShape() + " - " + component2.getName() + " Weight: " + weightC2);
                            LOG.info("\t\tRemoving Assignment to: " + (weightC1 < weightC2 ? component1.getShape() + " - " + component1.getName()
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
        LOG.info("\tRemoved " + removed + " Assignments");
    }

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
        this.xoManager.getRepository(ComponentRepository.class).computeCouplingBetweenComponents(ids.stream().mapToLong(l -> l).toArray());
        this.xoManager.currentTransaction().commit();
        return result;
    }

    /**
     * @param components
     * @return Mapping from component id (ranges from 0 to the number of identified components) to type ids (actual ids from the graph database)
     */
    private Map<Long, Collection<Long>> identifyIntersectingComponents(Collection<ComponentDescriptor> components) {
        Map<Long, Collection<Long>> componentMappings = new HashMap<>();
        Long componentId = 0L;
        ArrayListMultimap<Collection<Long>, Long> inverse = intersectComponents(components);
        for (Collection<Long> componentIds : inverse.keys().elementSet()) {
            componentMappings.put(componentId, Sets.newHashSet(inverse.get(componentIds)));
            componentId++;
        }
        return componentMappings;
    }

    private ArrayListMultimap<Collection<Long>, Long> intersectComponents(Collection<ComponentDescriptor> components) {
        LOG.info("Creating Intersecting Components");
        this.xoManager.currentTransaction().begin();
        ComponentRepository componentRepository = this.xoManager.getRepository(ComponentRepository.class);
        // Type ID -> Component IDs
        Map<Long, Collection<Long>> typeToComponents = new HashMap<>();
        Set<String> shapes = components.stream().map(ComponentDescriptor::getShape).collect(Collectors.toSet());
        List<TypeDescriptor> types = new ArrayList<>();
        try (Result<TypeDescriptor> descriptors = this.xoManager.getRepository(TypeRepository.class).getAllInternalTypes()) {
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

    public void exportResults(Set<ComponentDescriptor> components) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("Result_" + System.currentTimeMillis() + ".zip");
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
            // add resource files
            List<String> resources = Arrays.asList("circle-packing.html", "circle-packing-convert.js", "d3.min.js", "chord-diagram.html", "chord-jsonMapper.js", "chord-jsonScript.js", "chord-style.css");
            for (String resource : resources) {
                ZipEntry entry = new ZipEntry(resource);
                InputStream in = SARFRunner.class.getClassLoader().getResourceAsStream(resource);
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
            ComponentRepository repository = this.xoManager.getRepository(ComponentRepository.class);
            Result<Map> result
                = repository.getDecomposition(components.stream().mapToLong(c -> this.xoManager.getId(c)).toArray());
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
            zipOutputStream.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void prettyPrint(Collection<ComponentDescriptor> components, String indentation, StringBuilder builder) throws IOException {
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

    public String formatEntry(Map m) {
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
