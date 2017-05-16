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
@EqualsAndHashCode(callSuper = true, exclude = "patternDescriptor")
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@XmlRootElement(name = "Pattern")
public class Pattern extends Rule {

    @Getter
    @XmlAttribute(name = "regEx")
    private String regEx;

    private PatternDescriptor patternDescriptor;

    public Pattern(String shape, String name, double weight, String regEx) {
        super(shape, name, weight);
        this.regEx = regEx;
    }

    public static Pattern of(PatternDescriptor patternDescriptor) {
        Pattern pattern = new Pattern(
                patternDescriptor.getShape(), patternDescriptor.getName(), patternDescriptor.getWeight(), patternDescriptor.getRegEx());
        pattern.patternDescriptor = patternDescriptor;
        return pattern;
    }

    public PatternDescriptor materialize() {
        SARFRunner.xoManager.currentTransaction().begin();
        PatternDescriptor patternDescriptor = SARFRunner.xoManager.create(PatternDescriptor.class);
        patternDescriptor.setShape(this.shape);
        patternDescriptor.setName(this.name);
        patternDescriptor.setWeight(this.weight);
        patternDescriptor.setRegEx(this.regEx);
        SARFRunner.xoManager.currentTransaction().commit();
        this.patternDescriptor = patternDescriptor;
        return patternDescriptor;
    }

    public PatternDescriptor getPatternDescriptor() {
        if (this.patternDescriptor == null) {
            materialize();
        }
        return this.patternDescriptor;
    }

    @Override
    public Set<TypeDescriptor> getMatchingTypes() {
        Set<TypeDescriptor> types = new TreeSet<>(Comparator.comparing(FullQualifiedNameDescriptor::getFullQualifiedName));
        TypeRepository repository = SARFRunner.xoManager.getRepository(TypeRepository.class);
        Result<TypeDescriptor> result = repository.getAllInternalTypesLike(this.regEx);
        for (TypeDescriptor t : result) {
            types.add(t);
            repository.getInnerClassesOf(t.getFullQualifiedName()).forEach(types::add);
        }
        return types;
    }

    @Override
    public int compareTo(Rule o) {
        int superRes = 0;
        if ((superRes = super.compareTo((Rule) o)) == 0 && this.getClass().equals(o.getClass())) {
            return this.getRegEx().compareTo(((Pattern) o).getRegEx());
        }
        return superRes;
    }
}
