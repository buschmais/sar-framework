package com.buchmais.sarf.classification.criterion.dependency;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.criterion.logic.Rule;
import com.buchmais.sarf.classification.criterion.logic.RuleBasedCriterion;
import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query.Result;
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
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
@XmlRootElement(name = "Dependency")
public class DependencyRule<R extends DependencyRule, T extends DependencyDescriptor> extends Rule<T> {

    public DependencyRule(String shape, String name, double weight, String rule) {
        super(shape, name, weight, rule);
    }


    @Override
    public Set<TypeDescriptor> getMatchingTypes() {
        Set<TypeDescriptor> types = new TreeSet<>(Comparator.comparing(FullQualifiedNameDescriptor::getFullQualifiedName));
        DependencyRepository repository = SARFRunner.xoManager.getRepository(DependencyRepository.class);
        Result<TypeDescriptor> result = getMatchingTypes(repository);
        for (TypeDescriptor t : result) {
            types.add(t);
            // FIXME: 07.07.2017 Cannot create an instance of a single abstract type [interface com.buschmais.xo.api.CompositeObject] types.addAll(t.getDeclaredInnerClasses());
        }
        return types;
    }

    Result<TypeDescriptor> getMatchingTypes(DependencyRepository repository) {
        return repository.getAllInternalTypesDependingOn(this.rule);
    }

    @Override
    protected T instantiateDescriptor() {
        return (T) SARFRunner.xoManager.create(DependencyDescriptor.class);
    }

    public static DependencyRule of(DependencyDescriptor dependencyDescriptor) {
        DependencyRule dependencyRule = new DependencyRule(
                dependencyDescriptor.getShape(), dependencyDescriptor.getName(), dependencyDescriptor.getWeight(), dependencyDescriptor.getRule());
        dependencyRule.descriptor = dependencyDescriptor;
        return dependencyRule;
    }

    @Override
    public Class<? extends RuleBasedCriterion> getAssociateCriterion() {
        return DependencyCriterion.class;
    }
}
