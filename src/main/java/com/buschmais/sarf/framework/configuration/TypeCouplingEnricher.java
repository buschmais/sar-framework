package com.buschmais.sarf.framework.configuration;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.DatabaseHelper;
import com.buschmais.sarf.framework.repository.MetricRepository;
import com.buschmais.sarf.framework.repository.TypeRepository;
import com.buschmais.xo.api.Query.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Stephan Pirnbaum
 */
public class TypeCouplingEnricher {

    private static final Logger LOG = LogManager.getLogger(TypeCouplingEnricher.class);

    public static void enrich() {
        LOG.info("Computing Coupling between Types");
        DatabaseHelper.xoManager.currentTransaction().begin();
        MetricRepository mR = DatabaseHelper.xoManager.getRepository(MetricRepository.class);
        try (Result<TypeDescriptor> descriptors = DatabaseHelper.xoManager.getRepository(TypeRepository.class).getAllInternalTypes())
        {
            for (TypeDescriptor t1 : descriptors) {
                final Long id1 = DatabaseHelper.xoManager.getId(t1);
                try (Result<TypeDescriptor> dependencies = DatabaseHelper.xoManager.getRepository(TypeRepository.class).getInternalDependencies(id1)) {
                    for (TypeDescriptor t2 : dependencies) {
                        final Long id2 = DatabaseHelper.xoManager.getId(t2);
                        Double coupling = computeCoupling(id1, id2);
                        if (coupling > 0) {
                            mR.setCoupling(id1, id2, coupling);
                        }
                    }
                }
            }
        }
        LOG.info("Coupling between Types Successfully Computed");
        DatabaseHelper.xoManager.currentTransaction().commit();
        TypeSimilarityEnricher.enrich();
    }

    private static Double computeCoupling(Long id1, Long id2) {
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

        /*System.out.println("1 " + computeCouplingInvokes(id1, id2));
        System.out.println("2 " + computeCouplingInvokesStatic(id1, id2));
        System.out.println("3 " + computeCouplingExtends(id1, id2));
        System.out.println("4 " + computeCouplingImplements(id1, id2));
        System.out.println("5 " + computeCouplingReturns(id1, id2));
        System.out.println("6 " + computeCouplingParameterized(id1, id2));
        System.out.println("7 " + computeCouplingReads(id1, id2));
        System.out.println("8 " + computeCouplingReadsStatic(id1, id2));
        System.out.println("9 " + computeCouplingWrites(id1, id2));
        System.out.println("10 " + computeCouplingWritesStatic(id1, id2));*/
        Double res = weightedCoupling / totalWeight;
        return Double.isNaN(res) ? 0 : res;
    }

    private static Double computeCouplingInvokesAbstract(Long id1, Long id2) {
        MetricRepository mR = DatabaseHelper.xoManager.getRepository(MetricRepository.class);
        Double res = (double) mR.countInvokesAbstract(id1, id2) / mR.countAllInvokesExternalAbstract(id1);
        return Double.isNaN(res) ? 0 : res;
    }

    private static Double computeCouplingInvokes(Long id1, Long id2) {
        MetricRepository mR = DatabaseHelper.xoManager.getRepository(MetricRepository.class);
        Double res = (double) mR.countInvokes(id1, id2) / mR.countAllInvokesExternal(id1);
        return Double.isNaN(res) ? 0 : res;
    }

    private static Double computeCouplingInvokesStatic(Long id1, Long id2) {
        MetricRepository mR = DatabaseHelper.xoManager.getRepository(MetricRepository.class);
        Double res = (double) mR.countInvokesStatic(id1, id2) / mR.countAllInvokesExternalStatic(id1);
        return Double.isNaN(res) ? 0 : res;
    }

    private static Double computeCouplingExtends(Long id1, Long id2) {
        MetricRepository mR = DatabaseHelper.xoManager.getRepository(MetricRepository.class);
        return mR.typeExtends(id1, id2) ? 1d : 0d;
    }

    private static Double computeCouplingImplements(Long id1, Long id2) {
        MetricRepository mR = DatabaseHelper.xoManager.getRepository(MetricRepository.class);
        return mR.typeImplements(id1, id2) ? 1d : 0d;
    }

    private static Double computeCouplingReturns(Long id1, Long id2) {
        MetricRepository mR = DatabaseHelper.xoManager.getRepository(MetricRepository.class);
        // todo generics
        Double res = (double) mR.countReturns(id1, id2) / mR.countMethods(id1);
        return Double.isNaN(res) ? 0 : res;
    }

    private static Double computeCouplingParameterized(Long id1, Long id2) {
        MetricRepository mR = DatabaseHelper.xoManager.getRepository(MetricRepository.class);
        // todo generics
        Double res = (double) mR.countParameterized(id1, id2) / mR.countMethods(id1);
        return Double.isNaN(res) ? 0 : res;
    }

    private static Double computeCouplingReads(Long id1, Long id2) {
        MetricRepository mR = DatabaseHelper.xoManager.getRepository(MetricRepository.class);
        final Long readsT1T2 = mR.countReads(id1, id2);
        Double res = ((double) readsT1T2 * readsT1T2) / (mR.countReadsExternal(id1) * mR.countReadByExternal(id2)); // TODO: 07.07.2017 correct?
        return Double.isNaN(res) ? 0 : res;
    }

    private static Double computeCouplingReadsStatic(Long id1, Long id2) {
        MetricRepository mR = DatabaseHelper.xoManager.getRepository(MetricRepository.class);
        final Long readsT1T2 = mR.countReadsStatic(id1, id2);
        Double res = ((double) readsT1T2 * readsT1T2) / (mR.countReadsStaticExternal(id1) * mR.countReadByExternalStatic(id2));
        return Double.isNaN(res) ? 0 : res;
    }

    private static Double computeCouplingWrites(Long id1, Long id2) {
        MetricRepository mR = DatabaseHelper.xoManager.getRepository(MetricRepository.class);
        final Long writesT1T2 = mR.countWrites(id1, id2);
        Double res = ((double) writesT1T2 * writesT1T2) / (mR.countWritesExternal(id1) * mR.countWrittenByExternal(id2));
        return Double.isNaN(res) ? 0 : res;
    }

    private static Double computeCouplingWritesStatic(Long id1, Long id2) {
        MetricRepository mR = DatabaseHelper.xoManager.getRepository(MetricRepository.class);
        final Long writesT1T2 = mR.countWritesStatic(id1, id2);
        Double res = ((double) writesT1T2 * writesT1T2) / (mR.countWritesStaticExternal(id1) * mR.countWrittenByExternalStatic(id2));
        return Double.isNaN(res) ? 0 : res;
    }

    private static Double computeCouplingComposes(Long id1, Long id2) {
        MetricRepository mR = DatabaseHelper.xoManager.getRepository(MetricRepository.class);
        return (mR.typeComposes(id1, id2) ? 1d : 0d);
    }

    private static Double computeCouplingDeclaresInnerClass(Long id1, Long id2) {
        MetricRepository mR = DatabaseHelper.xoManager.getRepository(MetricRepository.class);
        return mR.declaresInnerClass(id1, id2) ? 1d : 0d;
    }

    private static Double computeSimpleDependsOn(Long id1, Long id2) {
        MetricRepository mR = DatabaseHelper.xoManager.getRepository(MetricRepository.class);
        return mR.dependsOn(id1, id2) ? 1d : 0d;
    }
}
