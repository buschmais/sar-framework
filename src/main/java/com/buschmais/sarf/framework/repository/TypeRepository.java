package com.buschmais.sarf.framework.repository;

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
            "  (a:Artifact)-[:CONTAINS*]->(p:Package)-[:CONTAINS]->(t:Type) " +
            "WHERE" +
            "  a.fileName =~ {artifact}" +
            "    AND" +
            "  p.fqn =~ {basePackage}" +
            "    AND" +
            "  t.name =~ {typeName} " +
            "SET" +
            "  t:Internal " +
            "RETURN" +
            "  count(t)")
    Long markAllInternalTypes(@Parameter("typeName") String typeName, @Parameter("basePackage") String basePackage, @Parameter("artifact") String artifact);

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
            "  (t1:Type:Internal)-[:COUPLES]-(d:Type:Internal)-[:COUPLES]-(t2:Type:Internal)\n" +
            "WHERE \n" +
            "  t1 <> t2 AND ID(t1) > ID(t2)\n" +
            "WITH\n" +
            "  DISTINCT t1, t2, d\n" +
            "MATCH\n" +
            "  (t1)-[c1:COUPLES]-(d)\n" +
            "WITH\n" +
            "  t1, t2, d, SUM(c1.coupling) AS t1Coup\n" +
            "MATCH\n" +
            "  (t2)-[c2:COUPLES]-(d)\n" +
            "WITH \n" +
            "  t1, t2, t1Coup, SUM(c2.coupling) AS t2Coup\n" +
            "WITH\n" +
            "  t1, t2, SUM(t1Coup) + SUM(t2Coup) AS intersection\n" +
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
            "  (t1)-[:IS_SIMILAR_TO{similarity:(intersection / (t1Coup + t2Coup))}]-(t2)")
    void computeTypeSimilarity();
}
