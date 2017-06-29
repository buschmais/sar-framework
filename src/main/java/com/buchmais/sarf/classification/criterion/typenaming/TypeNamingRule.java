package com.buchmais.sarf.classification.criterion.typenaming;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.classification.criterion.Rule;
import com.buchmais.sarf.node.PatternDescriptor;
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
@XmlRootElement(name = "Name")
public class TypeNamingRule extends Rule<PatternDescriptor> {

    public TypeNamingRule(String shape, String name, double weight, String rule) {
        super(shape, name, weight, rule);
    }

    public static TypeNamingRule of(PatternDescriptor patternDescriptor) {
        TypeNamingRule pattern = new TypeNamingRule(
                patternDescriptor.getShape(), patternDescriptor.getName(), patternDescriptor.getWeight(), patternDescriptor.getRule());
        pattern.descriptor = patternDescriptor;
        return pattern;
    }

    @Override
    public Set<TypeDescriptor> getMatchingTypes() {
        Set<TypeDescriptor> types = new TreeSet<>(Comparator.comparing(FullQualifiedNameDescriptor::getFullQualifiedName));
        TypeRepository repository = SARFRunner.xoManager.getRepository(TypeRepository.class);
        Query.Result<TypeDescriptor> result = repository.getAllInternalTypesLike(this.rule);
        for (TypeDescriptor t : result) {
            types.add(t);
            types.addAll(t.getDeclaredInnerClasses());
        }
        return types;
    }

    @Override
    protected PatternDescriptor instantiateDescriptor() {
        return SARFRunner.xoManager.create(PatternDescriptor.class);
    }
}
