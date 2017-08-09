package com.buschmais.sarf.classification.criterion.typenaming;

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
public interface TypeNamingRepository {

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type:Internal) " +
            "WHERE" +
            "  t.name =~ {regEx} " +
            "RETURN t")
    Result<TypeDescriptor> getAllInternalTypesByNameLike(@Parameter("regEx") String typeRegEx);

}
