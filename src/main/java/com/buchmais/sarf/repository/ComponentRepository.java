package com.buchmais.sarf.repository;

import com.buchmais.sarf.node.ComponentDescriptor;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.annotation.Repository;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.TypedNeo4jRepository;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

/**
 * @author Stephan Pirnbaum
 */
@Repository
public interface ComponentRepository extends TypedNeo4jRepository<ComponentDescriptor> {

    @ResultOf
    @Cypher("MATCH" +
            "  (conf:ClassificationConfiguration) " +
            "WITH" +
            "  max(conf.iteration) AS current " +
            "MATCH" +
            "  (:ClassificationConfiguration{iteration: current})-[:CONTAINS]->(:ClassificationCriterion)" +
            "    -[:CREATED]->(:ClassificationInfo)-[:MAPS]->(c:Component)" +
            "RETURN" +
            "  DISTINCT c")
    Result<ComponentDescriptor> getComponentsOfCurrentIteration();

    @ResultOf
    @Cypher("MATCH" +
            "  (conf:ClassificationConfiguration) " +
            "WITH" +
            "  max(conf.iteration) AS current " +
            "MATCH" +
            "  (:ClassificationConfiguration{iteration: current})-[:CONTAINS]->(:ClassificationCriterion)" +
            "    -[:CREATED]->(:ClassificationInfo)-[:MAPS]->(c:Component {shape: {shape}, name: {name}})" +
            "RETURN" +
            "  DISTINCT c")
    Result<ComponentDescriptor> getComponentOfCurrentIteration(@Parameter("shape") String shape, @Parameter("name") String name);
}
