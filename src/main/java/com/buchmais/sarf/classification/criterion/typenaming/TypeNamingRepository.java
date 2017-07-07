package com.buchmais.sarf.classification.criterion.typenaming;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.annotation.Repository;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

/**
 * @author Stephan Pirnbaum
 */
@Repository
public interface TypeNamingRepository {

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type:Internal) " +
            "WHERE" +
            "  t.name =~ {regEx} " +
            "RETURN t")
    Query.Result<TypeDescriptor> getAllInternalTypesByNameLike(@ResultOf.Parameter("regEx") String packageRegEx);

}
