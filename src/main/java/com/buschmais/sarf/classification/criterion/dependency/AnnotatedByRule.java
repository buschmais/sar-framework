package com.buschmais.sarf.classification.criterion.dependency;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.DatabaseHelper;
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
@XmlRootElement(name = "AnnotatedBy")
public class AnnotatedByRule extends DependencyRule<AnnotatedByRule, AnnotatedByDescriptor> {

    @Override
    Result<TypeDescriptor> getMatchingTypes(DependencyRepository repository) {
        return repository.getAllInternalTypesAnnotatedBy(this.rule);
    }

    @Override
    protected AnnotatedByDescriptor instantiateDescriptor() {
        return DatabaseHelper.xoManager.create(AnnotatedByDescriptor.class);
    }
}
