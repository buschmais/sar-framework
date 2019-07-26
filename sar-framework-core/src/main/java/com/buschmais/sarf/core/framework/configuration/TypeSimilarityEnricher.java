package com.buschmais.sarf.core.framework.configuration;

import com.buschmais.sarf.core.framework.repository.TypeRepository;
import com.buschmais.xo.api.XOManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * @author Stephan Pirnbaum
 */
@Service
@Lazy
@RequiredArgsConstructor
@Slf4j
public class TypeSimilarityEnricher {

    private final XOManager xoManager;
    private final TypeRepository typeRepository;

    public void enrich() {
        LOGGER.info("Computing Similarity between Types");
        this.xoManager.currentTransaction().begin();
        typeRepository.computeTypeSimilarity();
        this.xoManager.currentTransaction().commit();
        LOGGER.info("Similarity between Types Successfully Computed");
    }
}
