package com.buschmais.sarf.framework.configuration;

import com.buschmais.sarf.framework.repository.TypeRepository;
import com.buschmais.xo.api.XOManager;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * @author Stephan Pirnbaum
 */
@Service
@Lazy
@RequiredArgsConstructor
public class TypeSimilarityEnricher {

    private static final Logger LOG = LogManager.getLogger(TypeSimilarityEnricher.class);

    private final XOManager xoManager;
    private final TypeRepository typeRepository;

    public void enrich() {
        LOG.info("Computing Similarity between Types");
        this.xoManager.currentTransaction().begin();
        typeRepository.computeTypeSimilarity();
        this.xoManager.currentTransaction().commit();
        LOG.info("Similarity between Types Successfully Computed");
    }
}
