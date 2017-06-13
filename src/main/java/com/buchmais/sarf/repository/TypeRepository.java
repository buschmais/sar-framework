package com.buchmais.sarf.repository;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.annotation.Repository;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.TypedNeo4jRepository;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

/**
 * Created by steph on 04.05.2017.
 */
@Repository
public interface TypeRepository extends TypedNeo4jRepository<TypeDescriptor> {

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type) " +
            "WHERE" +
            "  t.fqn STARTS WITH {basePackage} " +
            "SET" +
            "  t:Internal " +
            "RETURN" +
            "  t")
    Result<TypeDescriptor> markAllInternalTypes(@Parameter("basePackage") String basePackage);

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type:Internal) " +
            "RETURN t")
    Result<TypeDescriptor> getAllInternalTypes();

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type:Internal) " +
            "WHERE" +
            "  t.fqn =~ {regEx} " +
            "RETURN t")
    Result<TypeDescriptor> getAllInternalTypesLike(@Parameter("regEx") String packageRegEx);

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type{fqn:{fqn}})-[:DECLARES*]->(inner:Type) " +
            "RETURN" +
            "  inner")
    Result<TypeDescriptor> getInnerClassesOf(@Parameter("fqn") String fqn);

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type:Internal)-[:DEPENDS_ON]->(d:Type) " +
            "WHERE" +
            "  d.fqn =~ {dependency}" +
            "RETURN" +
            "  DISTINCT t")
    Result<TypeDescriptor> getAllInternalTypesDependingOn(@Parameter("dependency") String dependency);

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type:Internal)-[:DEPENDS_ON]->(d:Type:Internal) " +
            "WHERE" +
            "  ID(t) = {t} " +
            "RETURN" +
            "  DISTINCT d")
    Result<TypeDescriptor> getInternalDependencies(@Parameter("t") Long t);
}
