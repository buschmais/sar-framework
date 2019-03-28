package com.buschmais.sarf.framework;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
@Lazy
@Slf4j
public class BenchmarkRunner implements AbstractRunner {

    @Override
    public Double run(URL configUrl) {
        LOGGER.error("Benchmark functionality currently not available");
        return null;
        /*
        ClassificationConfigurationDescriptor classificationConfigurationDescriptor = readConfiguration(benchmarkUrl);
        this.setUpData();
        this.activeClassificationConfiguration.materialize();
        Set<ComponentDescriptor> reference = this.activeClassificationConfiguration.execute();
        MoJoCalculator.reference = reference;
        CohesionCriterion cohesionCriterion = new CohesionCriterion();
        Set<ComponentDescriptor> comp = cohesionCriterion.classify(2, null,
                this.activeClassificationConfiguration.getGenerations(), this.activeClassificationConfiguration.getPopulationSize(),
                false, this.activeClassificationConfiguration.getOptimization() == ClassificationConfiguration.Optimization.SIMILARITY);

        try {
            DatabaseHelper.xoManager.currentTransaction().begin();
            MoJoCalculator moJoCalculator1 = new MoJoCalculator(reference, comp);
            MoJoCalculator moJoCalculator2 = new MoJoCalculator(comp, reference);
            MoJoCalculator moJoFmCalculator = new MoJoCalculator(comp, reference);
            MoJoCalculator moJoPlusCalculator1 = new MoJoCalculator(reference, comp);
            MoJoCalculator moJoPlusCalculator2 = new MoJoCalculator(comp, reference);
            Long mojoCompRef = moJoCalculator1.mojo();
            Long mojoRefComp = moJoCalculator2.mojo();
            Long mojo = Math.min(mojoCompRef, mojoRefComp);
            Double mojoFm = moJoFmCalculator.mojofm();
            Long mojoPlusCompRef = moJoPlusCalculator1.mojoplus();
            Long mojoPlusRefComp = moJoPlusCalculator2.mojoplus();
            Long mojoPlus = Math.min(mojoPlusCompRef, mojoPlusRefComp);
            TypeRepository typeRepository = DatabaseHelper.xoManager.getRepository(TypeRepository.class);
            Long typeCount = typeRepository.countAllInternalTypes();
            try (FileWriter fW = new FileWriter("Result_Benchmark_" + System.currentTimeMillis())) {
                BufferedWriter bW = new BufferedWriter(fW);
                PrintWriter pW = new PrintWriter(bW);
                pW.println(WeightConstants.stringify());
                pW.println("MoJo Quality: " + (100 - (100. * mojo / typeCount)) + " %");
                pW.println("MoJoFM Quality: " + mojoFm + " %");
                pW.println("MoJo Plus Quality: " + (100 - (100. * mojoPlus / typeCount)) + " %");
                StringBuilder formatted = new StringBuilder();
                ActiveClassificationConfiguration.prettyPrint(comp, "", formatted);
                pW.println(formatted.toString());
                DatabaseHelper.xoManager.currentTransaction().commit();
                pW.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return mojoFm;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return Double.MIN_VALUE;
        }
*/
    }

}
