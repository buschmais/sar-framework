package com.buchmais.sarf.classification;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.node.PatternDescriptor;
import com.buchmais.sarf.repository.TypeRepository;
import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query.Result;
import lombok.*;

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
@XmlRootElement(name = "Pattern")
public class Pattern extends Rule<PatternDescriptor> {



    public Pattern(String shape, String name, double weight, String rule) {
        super(shape, name, weight, rule);
    }

    public static Pattern of(PatternDescriptor patternDescriptor) {
        Pattern pattern = new Pattern(
                patternDescriptor.getShape(), patternDescriptor.getName(), patternDescriptor.getWeight(), patternDescriptor.getRule());
        pattern.descriptor = patternDescriptor;
        return pattern;
    }

    @Override
    public Set<TypeDescriptor> getMatchingTypes() {
        Set<TypeDescriptor> types = new TreeSet<>(Comparator.comparing(FullQualifiedNameDescriptor::getFullQualifiedName));
        TypeRepository repository = SARFRunner.xoManager.getRepository(TypeRepository.class);
        Result<TypeDescriptor> result = repository.getAllInternalTypesLike(this.rule);
        for (TypeDescriptor t : result) {
            types.add(t);
            repository.getInnerClassesOf(t.getFullQualifiedName()).forEach(types::add);
        }
        return types;
    }

    @Override
    PatternDescriptor instantiateDescriptor() {
        return SARFRunner.xoManager.create(PatternDescriptor.class);
    }
}
