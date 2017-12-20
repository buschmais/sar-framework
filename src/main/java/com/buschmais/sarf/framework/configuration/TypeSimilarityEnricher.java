package com.buschmais.sarf.framework.configuration;

import com.buschmais.sarf.framework.repository.TypeRepository;
import com.buschmais.xo.api.XOManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * @author Stephan Pirnbaum
 */
@Service
@Lazy
public class TypeSimilarityEnricher {

    private static final Logger LOG = LogManager.getLogger(TypeSimilarityEnricher.class);

    private XOManager xoManager;

    @Autowired
    public TypeSimilarityEnricher(XOManager xoManager) {
        this.xoManager = xoManager;
    }

    public void enrich() {
        LOG.info("Computing Similarity between Types");
        TypeRepository repository = this.xoManager.getRepository(TypeRepository.class);
        this.xoManager.currentTransaction().begin();
        repository.computeTypeSimilarity();
        this.xoManager.currentTransaction().commit();
        LOG.info("Similarity between Types Successfully Computed");
    }
}
