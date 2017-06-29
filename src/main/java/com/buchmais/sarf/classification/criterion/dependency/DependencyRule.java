package com.buchmais.sarf.classification.criterion.dependency;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.criterion.Rule;
import com.buchmais.sarf.node.DependencyDescriptor;
import com.buchmais.sarf.repository.TypeRepository;
import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
public class DependencyRule extends Rule<DependencyDescriptor> {

    public DependencyRule(String shape, String name, double weight, String rule) {
        super(shape, name, weight, rule);
    }


    @Override
    public Set<TypeDescriptor> getMatchingTypes() {
        Set<TypeDescriptor> types = new TreeSet<>(Comparator.comparing(FullQualifiedNameDescriptor::getFullQualifiedName));
        TypeRepository repository = SARFRunner.xoManager.getRepository(TypeRepository.class);
        Query.Result<TypeDescriptor> result = repository.getAllInternalTypesDependingOn(this.rule);
        for (TypeDescriptor t : result) {
            types.add(t);
            repository.getInnerClassesOf(t.getFullQualifiedName()).forEach(types::add);
        }
        return types;
    }

    @Override
    protected DependencyDescriptor instantiateDescriptor() {
        return SARFRunner.xoManager.create(DependencyDescriptor.class);
    }

    public static DependencyRule of(DependencyDescriptor dependencyDescriptor) {
        DependencyRule dependencyRule = new DependencyRule(
                dependencyDescriptor.getShape(), dependencyDescriptor.getName(), dependencyDescriptor.getWeight(), dependencyDescriptor.getRule());
        dependencyRule.descriptor = dependencyDescriptor;
        return dependencyRule;
    }
}
