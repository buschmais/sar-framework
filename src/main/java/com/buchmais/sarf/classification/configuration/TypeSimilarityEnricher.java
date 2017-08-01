package com.buchmais.sarf.classification.configuration;

import com.buchmais.sarf.SARFRunner;
import com.buchmais.sarf.repository.TypeRepository;

/**
 * @author Stephan Pirnbaum
 */
public class TypeSimilarityEnricher {

    public static void enrich() {
        TypeRepository repository = SARFRunner.xoManager.getRepository(TypeRepository.class);
        SARFRunner.xoManager.currentTransaction().begin();
        repository.computeTypeSimilarity();
        SARFRunner.xoManager.currentTransaction().commit();
    }
}
