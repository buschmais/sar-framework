package com.buschmais.sarf.framework.configuration;

import com.buschmais.xo.api.annotation.Repository;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.neo4j.api.TypedNeo4jRepository;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

/**
 * @author Stephan Pirnbaum
 */
@Repository
public interface ClassificationConfigurationRepository extends TypedNeo4jRepository<ClassificationConfigurationRepository> {

    @ResultOf
    @Cypher("MATCH" +
            "  (conf:ClassificationConfiguration) " +
            "WITH" +
            "  max(conf.iteration) AS current " +
            "MATCH" +
            "  (conf:ClassificationConfiguration{iteration: current}) " +
            "RETURN" +
            "  DISTINCT conf")
    ClassificationConfigurationDescriptor getCurrentConfiguration();
}
