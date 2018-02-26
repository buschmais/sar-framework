package com.buschmais.sarf.framework.configuration;

import com.buschmais.sarf.framework.repository.MetricRepository;
import com.buschmais.sarf.framework.repository.TypeRepository;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.XOManager;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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
        Map<TypeCoupling, TypeCoupling> couplings = new HashMap<>();
        try (Result<Map> couplingAbstract = this.mR.computeCouplingInvokesAbstract()) {
            addCouplings(couplingAbstract, couplings, WeightConstants.INVOKES_ABSTRACT_WEIGHT);
        }
        try (Result<Map> couplingInvokes = this.mR.computeCouplingInvokes()) {
            addCouplings(couplingInvokes, couplings, WeightConstants.INVOKES_WEIGHT);
        }
        try (Result<Map> couplingInvokesStatic = this.mR.computeCouplingInvokesStatic()) {
            addCouplings(couplingInvokesStatic, couplings, WeightConstants.INVOKES_STATIC_WEIGHT);
        }
        try (Result<Map> couplingExtends = this.mR.computeCouplingExtends()) {
            addCouplings(couplingExtends, couplings, WeightConstants.EXTENDS_WEIGHT);
        }
        try (Result<Map> couplingImplements = this.mR.computeCouplingImplements()) {
            addCouplings(couplingImplements, couplings, WeightConstants.IMPLEMENTS_WEIGHT);
        }
        try (Result<Map> couplingReturns = this.mR.computeCouplingReturns()) {
            addCouplings(couplingReturns, couplings, WeightConstants.RETURNS_WEIGHT);
        }
        try (Result<Map> couplingParameters = this.mR.computeCouplingParameterized()) {
            addCouplings(couplingParameters, couplings, WeightConstants.PARAMETER_WEIGHT);
        }
        try (Result<Map> couplingComposes = this.mR.computeCouplingComposes()) {
            addCouplings(couplingComposes, couplings, WeightConstants.COMPOSES_WEIGHT);
        }
        try (Result<Map> couplingInnerClass = this.mR.computeCouplingDeclaresInnerClass()) {
            addCouplings(couplingInnerClass, couplings, WeightConstants.INNER_CLASSES_WEIGHT);
        }
        try (Result<Map> couplingDependsOn = this.mR.computeCouplingDependsOn()) {
            addCouplings(couplingDependsOn, couplings, WeightConstants.DEPENDS_ON_WEIGHT);
        }
        try (Result<Map> couplingReads = this.mR.computeCouplingReads()) {
            addCouplings(couplingReads, couplings, WeightConstants.READS_WEIGHT);
        }
        try (Result<Map> couplingReadsStatic = this.mR.computeCouplingReadsStatic()) {
            addCouplings(couplingReadsStatic, couplings, WeightConstants.READS_STATIC_WEIGHT);
        }
        try (Result<Map> couplingWrites = this.mR.computeCouplingWrites()) {
            addCouplings(couplingWrites, couplings, WeightConstants.WRITES_WEIGHT);
        }
        try (Result<Map> couplingWritesStatic = this.mR.computeCouplingWritesStatic()) {
            addCouplings(couplingWritesStatic, couplings, WeightConstants.WRITES_STATIC_WEIGHT);
        }
        normalizeCouplings(couplings);
        for (TypeCoupling coupling : couplings.values()) {
            this.mR.setCoupling(coupling.source, coupling.target, coupling.coupling);
        }
        LOG.info("Coupling between Types Successfully Computed");
        this.xoManager.currentTransaction().commit();
        this.typeSimilarityEnricher.enrich();
    }

    private void addCouplings(Result<Map> result, Map<TypeCoupling, TypeCoupling> couplings, Double weight) {
        for (Map coupl : result) {
            long source = (long) coupl.get("source");
            double coupling = (double) coupl.get("coupling") * weight;
            long target = (long) coupl.get("target");
            couplings.putIfAbsent(new TypeCoupling(source, target), new TypeCoupling(source, target));
            couplings.get(new TypeCoupling(source, target)).addCoupling(coupling);
        }
    }

    private void normalizeCouplings(Map<TypeCoupling, TypeCoupling> couplings) {
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
        couplings.forEach((k, v) -> v.normalize(totalWeight));
    }

    @EqualsAndHashCode(of = {"source", "target"})
    @RequiredArgsConstructor
    private class TypeCoupling implements Comparable<TypeCoupling> {

        private final long source;
        private double coupling = 0d;
        private final long target;

        @Override
        public int compareTo(TypeCoupling o) {
            if (this.source == o.source) {
                return Long.compare(this.target, o.target);
            }
            return Long.compare(this.source, o.source);
        }

        void addCoupling(Double coupling) {
            this.coupling += coupling;
        }

        void normalize(Double divisor) {
            this.coupling /= divisor;
        }
    }
}
