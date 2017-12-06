package com.buschmais.sarf.plugin.packagenaming;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.DatabaseHelper;
import com.buschmais.sarf.plugin.api.Rule;
import com.buschmais.sarf.plugin.api.RuleBasedCriterion;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@XmlRootElement(name = "Package")
public class PackageNamingRule extends Rule<PackageNamingRuleDescriptor> {



    public PackageNamingRule(String shape, String name, double weight, String rule) {
        super(shape, name, weight, rule);
    }

    public static PackageNamingRule of(PackageNamingRuleDescriptor packageNamingRuleDescriptor) {
        PackageNamingRule pattern = new PackageNamingRule(
                packageNamingRuleDescriptor.getShape(), packageNamingRuleDescriptor.getName(), packageNamingRuleDescriptor.getWeight(), packageNamingRuleDescriptor.getRule());
        pattern.descriptor = packageNamingRuleDescriptor;
        return pattern;
    }

    @Override
    public Set<TypeDescriptor> getMatchingTypes() {
        Set<TypeDescriptor> types = new TreeSet<>(Comparator.comparing(FullQualifiedNameDescriptor::getFullQualifiedName));
        PackageNamingRepository repository = DatabaseHelper.xoManager.getRepository(PackageNamingRepository.class);
        Result<TypeDescriptor> result = repository.getAllInternalTypesInPackageLike(this.rule);
        for (TypeDescriptor t : result) {
            types.add(t);
        }
        return types;
    }

    @Override
    protected PackageNamingRuleDescriptor instantiateDescriptor() {
        return DatabaseHelper.xoManager.create(PackageNamingRuleDescriptor.class);
    }

    @Override
    public Class<? extends RuleBasedCriterion> getAssociateCriterion() {
        return PackageNamingCriterion.class;
    }
}
