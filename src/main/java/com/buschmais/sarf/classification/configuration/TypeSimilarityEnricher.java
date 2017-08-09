package com.buschmais.sarf.classification.configuration;

import com.buschmais.sarf.SARFRunner;
import com.buschmais.sarf.repository.TypeRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Stephan Pirnbaum
 */
public class TypeSimilarityEnricher {

    private static final Logger LOG = LogManager.getLogger(TypeSimilarityEnricher.class);

    public static void enrich() {
        LOG.info("Computing Similarity between Types");
        TypeRepository repository = SARFRunner.xoManager.getRepository(TypeRepository.class);
        SARFRunner.xoManager.currentTransaction().begin();
        repository.computeTypeSimilarity();
        SARFRunner.xoManager.currentTransaction().commit();
        LOG.info("Similarity between Types Successfully Computed");
    }
}
