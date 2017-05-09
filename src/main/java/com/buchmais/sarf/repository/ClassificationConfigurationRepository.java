package com.buchmais.sarf.repository;

import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.neo4j.api.TypedNeo4jRepository;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

/**
 * @author Stephan Pirnbaum
 */
public interface ClassificationConfigurationRepository extends TypedNeo4jRepository<ClassificationConfigurationRepository> {

    @ResultOf
    @Cypher("MATCH" +
            "  (conf:ClassificationConfiguration) " +
            "WITH" +
            "  max(conf.iteration) AS current" +
            "MATCH" +
            "  (conf:ClassificationConfiguration{iteration: current}) " +
            "RETURN" +
            "  DISTINCT conf")
    ClassificationConfigurationRepository getCurrentConfiguration();
}
