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

    @ResultOf
    @Cypher("MATCH\n" +
            "  (t1:Type:Internal),\n" +
            "  (t2:Type:Internal)\n" +
            "WHERE \n" +
            "  t1 <> t2 AND ID(t1) > ID(t2)\n" +
            "WITH\n" +
            "  t1, t2\n" +
            "OPTIONAL MATCH\n" +
            "  (t1)-[c1:COUPLES]-(d1:Type:Internal)-[c2:COUPLES]-(t2)\n" +
            "WHERE\n" +
            "  d1 <> t1 AND d1 <> t2\n" +
            "WITH\n" +
            "  t1, t2, sum(c1.coupling) + sum(c2.coupling) AS intersection\n" +
            "OPTIONAL MATCH\n" +
            "  (t1)-[c1:COUPLES]-(d1:Type:Internal)\n" +
            "WHERE \n" +
            "  d1 <> t2\n" +
            "WITH \n" +
            "  t1, t2, intersection, sum(c1.coupling) AS t1Coup\n" +
            "OPTIONAL MATCH\n" +
            "  (t2)-[c2:COUPLES]-(d2:Type:Internal)\n" +
            "WHERE\n" +
            "  d2 <> t1\n" +
            "WITH \n" +
            "  t1, t2, t1Coup, sum(c2.coupling) AS t2Coup, intersection\n" +
            "MERGE\n" +
            "  (t1)-[:IS_SIMILAR_TO{similarity:(intersection / (t1Coup + t2Coup + 0.00001))}]-(t2)")
    void computeTypeSimilarity();
}
