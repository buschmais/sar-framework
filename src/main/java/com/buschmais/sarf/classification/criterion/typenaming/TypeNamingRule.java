package com.buschmais.sarf.classification.criterion.typenaming;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.SARFRunner;
import com.buschmais.sarf.classification.criterion.Rule;
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
public class TypeNamingRule extends Rule<TypeNamingRuleDescriptor> {

    public TypeNamingRule(String shape, String name, double weight, String rule) {
        super(shape, name, weight, rule);
    }

    public static TypeNamingRule of(TypeNamingRuleDescriptor typeNamingRuleDescriptor) {
        TypeNamingRule pattern = new TypeNamingRule(
                typeNamingRuleDescriptor.getShape(),
                typeNamingRuleDescriptor.getName(),
                typeNamingRuleDescriptor.getWeight(),
                typeNamingRuleDescriptor.getRule());
        pattern.descriptor = typeNamingRuleDescriptor;
        return pattern;
    }

    @Override
    public Set<TypeDescriptor> getMatchingTypes() {
        Set<TypeDescriptor> types = new TreeSet<>(Comparator.comparing(FullQualifiedNameDescriptor::getFullQualifiedName));
        TypeNamingRepository repository = SARFRunner.xoManager.getRepository(TypeNamingRepository.class);
        Query.Result<TypeDescriptor> result = repository.getAllInternalTypesByNameLike(this.rule);
        for (TypeDescriptor t : result) {
            types.add(t);
            // FIXME: 07.07.2017 Cannot create an instance of a single abstract type [interface com.buschmais.xo.api.CompositeObject] types.addAll(t.getDeclaredInnerClasses());
        }
        return types;
    }

    @Override
    protected TypeNamingRuleDescriptor instantiateDescriptor() {
        return SARFRunner.xoManager.create(TypeNamingRuleDescriptor.class);
    }

    @Override
    public Class<TypeNamingCriterion> getAssociateCriterion() {
        return TypeNamingCriterion.class;
    }
}
