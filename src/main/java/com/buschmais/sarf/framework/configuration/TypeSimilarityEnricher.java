package com.buschmais.sarf.framework.configuration;

import com.buschmais.sarf.DatabaseHelper;
import com.buschmais.sarf.framework.repository.TypeRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Stephan Pirnbaum
 */
public class TypeSimilarityEnricher {

    private static final Logger LOG = LogManager.getLogger(TypeSimilarityEnricher.class);

    public static void enrich() {
        LOG.info("Computing Similarity between Types");
        TypeRepository repository = DatabaseHelper.xoManager.getRepository(TypeRepository.class);
        DatabaseHelper.xoManager.currentTransaction().begin();
        repository.computeTypeSimilarity();
        DatabaseHelper.xoManager.currentTransaction().commit();
        LOG.info("Similarity between Types Successfully Computed");
    }
}
