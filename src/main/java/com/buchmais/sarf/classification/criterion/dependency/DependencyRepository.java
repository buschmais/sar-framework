package com.buchmais.sarf.classification.criterion.dependency;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query;
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
    Query.Result<TypeDescriptor> getAllInternalTypesDependingOn(@ResultOf.Parameter("dependency") String dependencyRegEx);
}
