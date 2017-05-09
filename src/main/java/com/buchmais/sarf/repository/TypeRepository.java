package com.buchmais.sarf.repository;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.annotation.Repository;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.neo4j.api.TypedNeo4jRepository;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

/**
 * Created by steph on 04.05.2017.
 */
@Repository
public interface TypeRepository extends TypedNeo4jRepository<TypeDescriptor> {

    @ResultOf
    @Cypher("MATCH (t:Type) RETURN t")
    Result<TypeDescriptor> getAllInternalTypes();

    @ResultOf
    @Cypher("MATCH " +
            "  (t:Type) " +
            "WHERE " +
            "  t.fqn =~ {regEx} " +
            "RETURN t")
    Result<TypeDescriptor> getAllTypesInPackageLike(@ResultOf.Parameter("regEx") String packageRegEx);
}
