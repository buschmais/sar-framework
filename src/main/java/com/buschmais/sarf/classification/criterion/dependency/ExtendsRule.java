package com.buschmais.sarf.classification.criterion.dependency;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.SARFRunner;
import com.buschmais.xo.api.Query.Result;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Stephan Pirnbaum
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
@XmlRootElement(name = "Extends")
public class ExtendsRule extends DependencyRule<ExtendsRule, ExtendsDescriptor> {

    @Override
    Result<TypeDescriptor> getMatchingTypes(DependencyRepository repository) {
        return repository.getAllInternalTypesExtending(this.rule);
    }

    @Override
    protected ExtendsDescriptor instantiateDescriptor() {
        return SARFRunner.xoManager.create(ExtendsDescriptor.class);
    }
}
