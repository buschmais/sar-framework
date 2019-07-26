package com.buschmais.sarf.core.plugin.packagenaming;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.annotation.Repository;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

import static com.buschmais.xo.api.annotation.ResultOf.Parameter;

/**
 * @author Stephan Pirnbaum
 */
@Repository
public interface PackageNamingRepository {

    @ResultOf
    @Cypher("MATCH" +
            "  (p:Package)-[:CONTAINS]->(t:Type:Internal) " +
            "WHERE" +
            "  p.fqn =~ {regEx} " +
            "RETURN " +
            "  DISTINCT t")
    Result<TypeDescriptor> getAllInternalTypesInPackageLike(@Parameter("regEx") String packageRegEx);
}
