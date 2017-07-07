package com.buchmais.sarf.classification.criterion.dependency;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.annotation.Repository;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

/**
 * @author Stephan Pirnbaum
 */
@Repository
public interface DependencyRepository {
    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type:Internal)-[:DEPENDS_ON]->(d:Type) " +
            "WHERE" +
            "  d.fqn =~ {dependency}" +
            "RETURN" +
            "  DISTINCT t")
    Result<TypeDescriptor> getAllInternalTypesDependingOn(@ResultOf.Parameter("dependency") String dependencyRegEx);

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type:Internal)-[:EXTENDS]->(d:Type) " +
            "WHERE" +
            "  d.fqn =~ {dependency}" +
            "RETURN" +
            "  DISTINCT t")
    Result<TypeDescriptor> getAllInternalTypesExtending(@ResultOf.Parameter("dependency") String dependencyRegEx);

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type:Internal)-[:IMPLEMENTS]->(d:Type) " +
            "WHERE" +
            "  d.fqn =~ {dependency}" +
            "RETURN" +
            "  DISTINCT t")
    Result<TypeDescriptor> getAllInternalTypesImplementing(@ResultOf.Parameter("dependency") String dependencyRegEx);

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type:Internal)-[:ANNOTATED_BY]->(:Annotation)-[:OF_TYPE]->(d:Type) " +
            "WHERE" +
            "  d.fqn =~ {dependency}" +
            "RETURN" +
            "  DISTINCT t")
    Result<TypeDescriptor> getAllInternalTypesAnnotatedBy(@ResultOf.Parameter("dependency") String dependencyRegEx);



}
