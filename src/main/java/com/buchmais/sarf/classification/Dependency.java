package com.buchmais.sarf.classification;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.node.DependencyDescriptor;
import com.buchmais.sarf.repository.TypeRepository;
import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Stephan Pirnbaum
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@XmlRootElement(name = "Dependency")
public class Dependency extends Rule<DependencyDescriptor> {

    @Getter
    @XmlAttribute(name = "dependency")
    private String dependency;

    public Dependency(String shape, String name, double weight, String dependency) {
        super(shape, name, weight);
        this.dependency = dependency;
    }

    @Override
    protected DependencyDescriptor materialize() {
        DependencyDescriptor dependencyDescriptor = SARFRunner.xoManager.create(DependencyDescriptor.class);
        dependencyDescriptor.setShape(this.shape);
        dependencyDescriptor.setName(this.name);
        dependencyDescriptor.setWeight(this.weight);
        dependencyDescriptor.setDependency(this.dependency);
        this.descriptor = dependencyDescriptor;
        return dependencyDescriptor;
    }

    @Override
    public Set<TypeDescriptor> getMatchingTypes() {
        Set<TypeDescriptor> types = new TreeSet<>(Comparator.comparing(FullQualifiedNameDescriptor::getFullQualifiedName));
        TypeRepository repository = SARFRunner.xoManager.getRepository(TypeRepository.class);
        Query.Result<TypeDescriptor> result = repository.getAllInternalTypesDependingOn(this.dependency);
        for (TypeDescriptor t : result) {
            types.add(t);
            repository.getInnerClassesOf(t.getFullQualifiedName()).forEach(types::add);
        }
        return types;
    }

    public static Dependency of(DependencyDescriptor dependencyDescriptor) {
        Dependency dependency = new Dependency(
                dependencyDescriptor.getShape(), dependencyDescriptor.getName(), dependencyDescriptor.getWeight(), dependencyDescriptor.getDependency());
        dependency.descriptor = dependencyDescriptor;
        return dependency;
    }
}
