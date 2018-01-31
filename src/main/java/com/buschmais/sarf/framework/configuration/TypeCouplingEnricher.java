package com.buschmais.sarf.framework.configuration;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.framework.repository.MetricRepository;
import com.buschmais.sarf.framework.repository.TypeRepository;
import com.buschmais.xo.api.Query.Result;
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
public class TypeCouplingEnricher {

    private static final Logger LOG = LogManager.getLogger(TypeCouplingEnricher.class);

    private XOManager xoManager;
    private TypeSimilarityEnricher typeSimilarityEnricher;

    private MetricRepository mR;
    private TypeRepository tR;

    @Autowired
    public TypeCouplingEnricher(XOManager xoManager, TypeSimilarityEnricher typeSimilarityEnricher){
        this.xoManager = xoManager;
        this.typeSimilarityEnricher = typeSimilarityEnricher;
        this.mR = this.xoManager.getRepository(MetricRepository.class);
        this.tR = this.xoManager.getRepository(TypeRepository.class);
    }

    public void enrich() {
        LOG.info("Computing Coupling between Types");
        this.xoManager.currentTransaction().begin();
        try (Result<TypeDescriptor> descriptors = this.tR.getAllInternalTypes())
        {
            int i = 0;
            for (TypeDescriptor t1 : descriptors) {
                if (i % 100 == 0) System.out.println(i);
                i++;
                final Long id1 = this.xoManager.getId(t1);
                try (Result<TypeDescriptor> dependencies = this.tR.getInternalDependencies(id1)) {
                    for (TypeDescriptor t2 : dependencies) {
                        final Long id2 = this.xoManager.getId(t2);
                        Double coupling = computeCoupling(id1, id2);
                        if (coupling > 0) {
                            this.mR.setCoupling(id1, id2, coupling);
                        }
                    }
                }
            }
        }
        LOG.info("Coupling between Types Successfully Computed");
        this.xoManager.currentTransaction().commit();
        this.typeSimilarityEnricher.enrich();
    }

    private Double computeCoupling(Long id1, Long id2) {
        Double totalWeight =
                WeightConstants.INVOKES_WEIGHT +
                WeightConstants.INVOKES_STATIC_WEIGHT +
                WeightConstants.EXTENDS_WEIGHT +
                WeightConstants.IMPLEMENTS_WEIGHT +
                WeightConstants.RETURNS_WEIGHT +
                WeightConstants.PARAMETER_WEIGHT +
                WeightConstants.READS_WEIGHT +
                WeightConstants.READS_STATIC_WEIGHT +
                WeightConstants.WRITES_WEIGHT +
                WeightConstants.WRITES_STATIC_WEIGHT +
                WeightConstants.COMPOSES_WEIGHT +
                WeightConstants.INNER_CLASSES_WEIGHT +
                WeightConstants.DEPENDS_ON_WEIGHT +
                WeightConstants.INVOKES_ABSTRACT_WEIGHT;

        Double weightedCoupling =
                WeightConstants.INVOKES_WEIGHT * computeCouplingInvokes(id1, id2) +
                WeightConstants.INVOKES_STATIC_WEIGHT * computeCouplingInvokesStatic(id1, id2) +
                WeightConstants.EXTENDS_WEIGHT * computeCouplingExtends(id1, id2) +
                WeightConstants.IMPLEMENTS_WEIGHT * computeCouplingImplements(id1, id2) +
                WeightConstants.RETURNS_WEIGHT * computeCouplingReturns(id1, id2) +
                WeightConstants.PARAMETER_WEIGHT * computeCouplingParameterized(id1, id2) +
                WeightConstants.READS_WEIGHT * computeCouplingReads(id1, id2) +
                WeightConstants.READS_STATIC_WEIGHT * computeCouplingReadsStatic(id1, id2) +
                WeightConstants.WRITES_WEIGHT * computeCouplingWrites(id1, id2) +
                WeightConstants.WRITES_STATIC_WEIGHT * computeCouplingWritesStatic(id1, id2) +
                WeightConstants.COMPOSES_WEIGHT * computeCouplingComposes(id1, id2) +
                WeightConstants.INNER_CLASSES_WEIGHT * computeCouplingDeclaresInnerClass(id1, id2) +
                WeightConstants.DEPENDS_ON_WEIGHT * computeSimpleDependsOn(id1, id2) +
                WeightConstants.INVOKES_ABSTRACT_WEIGHT * computeCouplingInvokesAbstract(id1, id2);

        Double res = weightedCoupling / totalWeight;
        return Double.isNaN(res) ? 0 : res;
    }

    private Double computeCouplingInvokesAbstract(Long id1, Long id2) {
        Double res = (double) mR.countInvokesAbstract(id1, id2) / mR.countAllInvokesExternalAbstract(id1);
        return Double.isNaN(res) ? 0 : res;
    }

    private Double computeCouplingInvokes(Long id1, Long id2) {
        Double res = (double) mR.countInvokes(id1, id2) / mR.countAllInvokesExternal(id1);
        return Double.isNaN(res) ? 0 : res;
    }

    private Double computeCouplingInvokesStatic(Long id1, Long id2) {
        Double res = (double) mR.countInvokesStatic(id1, id2) / mR.countAllInvokesExternalStatic(id1);
        return Double.isNaN(res) ? 0 : res;
    }

    private Double computeCouplingExtends(Long id1, Long id2) {
        return mR.typeExtends(id1, id2) ? 1d : 0d;
    }

    private Double computeCouplingImplements(Long id1, Long id2) {
        MetricRepository mR = this.xoManager.getRepository(MetricRepository.class);
        return mR.typeImplements(id1, id2) ? 1d : 0d;
    }

    private Double computeCouplingReturns(Long id1, Long id2) {
        // todo generics
        Double res = (double) mR.countReturns(id1, id2) / mR.countMethods(id1);
        return Double.isNaN(res) ? 0 : res;
    }

    private Double computeCouplingParameterized(Long id1, Long id2) {
        // todo generics
        Double res = (double) mR.countParameterized(id1, id2) / mR.countMethods(id1);
        return Double.isNaN(res) ? 0 : res;
    }

    private Double computeCouplingReads(Long id1, Long id2) {
        final Long readsT1T2 = mR.countReads(id1, id2);
        Double res = ((double) readsT1T2 * readsT1T2) / (mR.countReadsExternal(id1) * mR.countReadByExternal(id2)); // TODO: 07.07.2017 correct?
        return Double.isNaN(res) ? 0 : res;
    }

    private Double computeCouplingReadsStatic(Long id1, Long id2) {
        final Long readsT1T2 = mR.countReadsStatic(id1, id2);
        Double res = ((double) readsT1T2 * readsT1T2) / (mR.countReadsStaticExternal(id1) * mR.countReadByExternalStatic(id2));
        return Double.isNaN(res) ? 0 : res;
    }

    private Double computeCouplingWrites(Long id1, Long id2) {
        final Long writesT1T2 = mR.countWrites(id1, id2);
        Double res = ((double) writesT1T2 * writesT1T2) / (mR.countWritesExternal(id1) * mR.countWrittenByExternal(id2));
        return Double.isNaN(res) ? 0 : res;
    }

    private Double computeCouplingWritesStatic(Long id1, Long id2) {
        final Long writesT1T2 = mR.countWritesStatic(id1, id2);
        Double res = ((double) writesT1T2 * writesT1T2) / (mR.countWritesStaticExternal(id1) * mR.countWrittenByExternalStatic(id2));
        return Double.isNaN(res) ? 0 : res;
    }

    private Double computeCouplingComposes(Long id1, Long id2) {
        return (mR.typeComposes(id1, id2) ? 1d : 0d);
    }

    private Double computeCouplingDeclaresInnerClass(Long id1, Long id2) {
        return mR.declaresInnerClass(id1, id2) ? 1d : 0d;
    }

    private Double computeSimpleDependsOn(Long id1, Long id2) {
        return mR.dependsOn(id1, id2) ? 1d : 0d;
    }
}
