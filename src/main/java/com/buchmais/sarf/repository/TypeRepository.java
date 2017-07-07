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
            "  count(t)")
    Long markAllInternalTypes(@Parameter("basePackage") String basePackage);

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type:Internal) " +
            "RETURN t")
    Result<TypeDescriptor> getAllInternalTypes();

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type:Internal) " +
            "RETURN count(t)")
    Long countAllInternalTypes();

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type{fqn:{fqn}})-[:DECLARES*]->(inner:Type) " +
            "RETURN" +
            "  inner")
    Result<TypeDescriptor> getInnerClassesOf(@Parameter("fqn") String fqn);

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type:Internal)-[:DEPENDS_ON]->(d:Type:Internal) " +
            "WHERE" +
            "  ID(t) = {t} " +
            "RETURN" +
            "  DISTINCT d")
    Result<TypeDescriptor> getInternalDependencies(@Parameter("t") Long t);

    @ResultOf
    @Cypher("MATCH" +
            "  (p:Package)-[:CONTAINS]->(t:Type) " +
            "WHERE" +
            "  ID(t) = {t} " +
            "RETURN" +
            "  p.fqn")
    String getPackageName(@Parameter("t") Long t);

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type:Internal)<-[:CLASSIFIES]-(:ClassificationInfo{iteration:{i}})-[:MAPS]->(c1:Component:SARF) " +
            "WHERE" +
            "  ID(c1) = {c1} " +
            "WITH" +
            "  t " +
            "MATCH" +
            "  (t)<-[:CLASSIFIES]-(:ClassificationInfo{iteration:{i}})-[:MAPS]->(c2:Component:SARF) " +
            "WHERE" +
            "  ID(c2) = {c2} " +
            "RETURN" +
            "  DISTINCT t")
    Result<TypeDescriptor> getTypesPreAssignedTo(@Parameter("c1") Long c1, @Parameter("c2") Long c2, @Parameter("i") Integer iteration);

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type:Internal)<-[:CLASSIFIES]-(i:ClassificationInfo{iteration:{i}})-[:MAPS]->(c:Component:SARF) " +
            "WHERE" +
            "  ID(t) = {t} AND ID(c) = {c} " +
            "RETURN" +
            "  MAX(i.weight)")
    Double getAssignmentWeight(@Parameter("t") Long type, @Parameter("c") Long c, @Parameter("i") Integer iteration);

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type:Internal)<-[:CLASSIFIES]-(i:ClassificationInfo{iteration:{i}})-[:MAPS]->(c:Component:SARF) " +
            "WHERE" +
            "  ID(t) = {t} AND ID(c) = {c} " +
            "DETACH DELETE" +
            "  i")
    void removeAssignment(@Parameter("t") Long type, @Parameter("c") Long c, @Parameter("i") Integer iteration);
}
