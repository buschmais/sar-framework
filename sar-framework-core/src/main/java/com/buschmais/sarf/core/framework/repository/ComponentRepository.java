package com.buschmais.sarf.core.framework.repository;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.core.framework.metamodel.ComponentDescriptor;
import com.buschmais.sarf.core.plugin.api.ClassificationInfoDescriptor;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.annotation.Repository;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.TypedNeo4jRepository;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

import java.util.Map;

/**
 * @author Stephan Pirnbaum
 */
@Repository
public interface ComponentRepository extends TypedNeo4jRepository<ComponentDescriptor> {

    @ResultOf
    @Cypher("MATCH" +
            "  (conf:ClassificationConfiguration) " +
            "WITH" +
            "  max(conf.iteration) AS current " +
            "MATCH" +
            "  (:ClassificationConfiguration{iteration: current})-[:CONTAINS]->(:ClassificationCriterion)" +
            "    -[:CREATED]->(:ClassificationInfo)-[:MAPS]->(c:Component)" +
            "RETURN" +
            "  DISTINCT c")
    Result<ComponentDescriptor> getComponentsOfCurrentIteration();

    @ResultOf
    @Cypher("MATCH" +
            "  (conf:ClassificationConfiguration) " +
            "WITH" +
            "  max(conf.iteration) AS current " +
            "MATCH" +
            "  (c:Component:SARF{shape:{shape}, name:{name}})," +
            "   (cur:ClassificationConfiguration{iteration: current})" +
            "WHERE" +
            "  EXISTS((cur)-[:CONTAINS]->(:ClassificationCriterion)-[:CREATED]->(:ClassificationInfo)-[:MAPS]->(c))" +
            "    OR" +
            "  EXISTS((cur)-[:DEFINES]->(c))" +
            "    OR" +
            "  EXISTS((cur)-[:DEFINES]->(:Component:SARF)-[:CONTAINS*]->(c)) " +
            "RETURN " +
            "  DISTINCT c")
    Result<ComponentDescriptor> getComponentOfCurrentIteration(@Parameter("shape") String shape, @Parameter("name") String name);

    @ResultOf
    @Cypher("MATCH\n" +
            "  (:SARF:Component {shape:{shape2}, name:{name2}})" +
            "    <-[:MAPS]-(info2:ClassificationInfo {iteration:{iteration}})-[:CLASSIFIES]->" +
            "  (type1:Type:Internal)" +
            "    <-[:CLASSIFIES]-(info1:ClassificationInfo {iteration:{iteration}})-[:MAPS]->" +
            "  (:SARF:Component {shape:{shape1}, name:{name1}})\n" +
            "WITH \n" +
            "  count(DISTINCT type1) AS intersection\n" +
            "MATCH\n" +
            "  (type1:Type:Internal)" +
            "    <-[:CLASSIFIES]-(info1:ClassificationInfo {iteration:{iteration}})-[:MAPS]->" +
            "  (comp1:SARF:Component {shape:{shape1}, name:{name1}})\n" +
            "WITH\n" +
            "  intersection, collect(type1) AS types\n" +
            "MATCH\n" +
            "  (type2:Type:Internal)" +
            "    <-[:CLASSIFIES]-(info2:ClassificationInfo {iteration:{iteration}})-[:MAPS]->" +
            "  (comp2:SARF:Component {shape:{shape2}, name:{name2}})\n" +
            "WITH \n" +
            "  intersection, types + collect(type2) AS rows\n" +
            "UNWIND \n" +
            "  rows AS row\n" +
            "RETURN\n" +
            "  toFloat(intersection)/count(DISTINCT row)")
    double computeJaccardSimilarity(@Parameter("shape1") String shape1, @Parameter("name1") String name1,
                                    @Parameter("shape2") String shape2, @Parameter("name2") String nam2,
                                    @Parameter("iteration") Integer iteration);

    @ResultOf
    @Cypher("MATCH" +
            "  (:SARF:Component{shape:{shape}, name:{name}})" +
            "    <-[:MAPS]-(:ClassificationInfo{iteration:{iteration}})-[:CLASSIFIES]->" +
            "  (t:Type) " +
            "RETURN" +
            "  count(DISTINCT t)")
    Long computeComponentCardinality(@Parameter("shape") String shape, @Parameter("name") String string,
                                       @Parameter("iteration") Integer iteration);

    @ResultOf
    @Cypher("MATCH" +
            "  (:SARF:Component {shape:{shape2}, name:{name2}})" +
            "    <-[:MAPS]-(info2:ClassificationInfo {iteration:{iteration}})-[:CLASSIFIES]->" +
            "  (type:Type:Internal)" +
            "    <-[:CLASSIFIES]-(info1:ClassificationInfo {iteration:{iteration}})-[:MAPS]->" +
            "  (:SARF:Component {shape:{shape1}, name:{name1}})" +
            "RETURN" +
            "  count(DISTINCT type)")
    Long computeComponentIntersectionCardinality(@Parameter("shape1") String shape1, @Parameter("name1") String name1,
                                                   @Parameter("shape2") String shape2, @Parameter("name2") String name2,
                                                   @Parameter("iteration") Integer iteration);

    @ResultOf
    @Cypher("MATCH" +
            "  (:SARF:Component {shape:{shape2}, name:{name2}})" +
            "    <-[:MAPS]-(:ClassificationInfo {iteration:{iteration}})-[:CLASSIFIES]->" +
            "  (type:Type:Internal) " +
            "WITH" +
            "  type " +
            "OPTIONAL MATCH" +
            "  (type)" +
            "    <-[:CLASSIFIES]-(info1:ClassificationInfo {iteration:{iteration}})-[:MAPS]->" +
            "  (:SARF:Component {shape:{shape1}, name:{name1}}) " +
            "WHERE" +
            "  info1 IS NULL " +
            "RETURN" +
            "  count(DISTINCT type)")
    Long computeComplementCardinality(@Parameter("shape1") String ofShape, @Parameter("name1") String ofName,
                                      @Parameter("shape2") String inShape, @Parameter("name2") String inName,
                                      @Parameter("iteration") Integer iteration);

    @ResultOf
    @Cypher("MATCH" +
            "  (c:SARF:Component)<-[:MAPS]-(info:ClassificationInfo) " +
            "WHERE" +
            "  ID(c) = {id} " +
            "RETURN" +
            "  info")
    Result<ClassificationInfoDescriptor> getCandidateTypes(@Parameter("id") Long componentId);

    @ResultOf
    @Cypher("MATCH" +
            "  (e1), (e2) " +
            "WHERE" +
            "  ID(e1) = {cId} AND ID(e2) = {id} " +
            "RETURN" +
            "  EXISTS((e1)<-[:MAPS]-(:ClassificationInfo)-[:CLASSIFIES]->(e2))")
    boolean isCandidateType(@Parameter("cId") Long componentId, @Parameter("id") Long id);

    @ResultOf
    @Cypher("MATCH" +
            "  (e1), (e2) " +
            "WHERE" +
            "  ID(e1) = {cId} AND ID(e2) = {id} " +
            "RETURN" +
            "  EXISTS((e1)-[:CONTAINS]->(e2))")
    boolean isCandidateComponent(@Parameter("cId") Long cId, @Parameter("id") Long id);

    @ResultOf
    @Cypher("MATCH" +
            "  (c:SARF:Component{shape:{shape}})<-[:MAPS]-(info:ClassificationInfo)-[:CLASSIFIES]->(t:Type) " +
            "WHERE" +
            "  ID(c) IN {ids} AND ID(t) = {tid} " +
            "WITH" +
            "  max(info.weight) AS maxWeight " +
            "MATCH" +
            "  (c:SARF:Component{shape:{shape}})<-[:MAPS]-(info:ClassificationInfo)-[:CLASSIFIES]->(t:Type) " +
            "WHERE" +
            "  ID(c) IN {ids} AND ID(t) = {tid} AND info.weight = maxWeight " +
            "RETURN" +
            "  ID(c) " +
            "LIMIT" +
            "  1")
    Long getBestComponentForShape(@Parameter("ids") long[] longs, @Parameter("shape") String shape, @Parameter("tid") Long typeId);

    @ResultOf
    @Cypher("MATCH" +
            "  (c:Component:SARF) " +
            "WHERE" +
            "  ID(c) IN {ids} " +
            "RETURN" +
            "  c")
    Result<ComponentDescriptor> getComponentsWithId(@Parameter("ids") long[] longs);

    @ResultOf
    @Cypher("MATCH\n" +
            "  (c1:Component:SARF)-[cont1:CONTAINS]->(e1)-[coup:COUPLES]->(e2)<-[:CONTAINS]-(c2:Component:SARF) \n" +
            "WHERE\n" +
            "  NOT ID(c1) = ID(c2) AND ID(c1) IN {ids} AND ID(c2) IN {ids}\n" +
            "WITH\n" +
            "  c1, c2, SUM(coup.coupling) AS coupling\n" +
            "MATCH\n" +
            "  (c1)-[:CONTAINS]->(e1)\n" +
            "WITH \n" +
            "  c1, c2, count(e1) AS size1, coupling\n" +
            "MATCH\n" +
            "  (c2)-[:CONTAINS]->(e2)\n" +
            "WITH\n" +
            "  c1, c2, size1 + count(e2) AS size, coupling\n" +
            "WITH \n" +
            "  c1, c2, coupling / ((size * (size - 1))/2) AS relCoupling\n" +
            "MERGE\n" +
            "  (c1)-[:COUPLES{coupling:relCoupling}]->(c2)")
    void computeCouplingBetweenComponents(@Parameter("ids") long[] ids);

    @ResultOf
    @Cypher("MATCH" +
            "  (c:Component:SARF)," +
            "  (t:Type) " +
            "WHERE" +
            "  ID(c) = {cId} AND ID(t) = {tId} " +
            "RETURN" +
            "  exists((c)-[:CONTAINS]->(t))")
    boolean containsType(@Parameter("cId") Long cId, @Parameter("tId") Long tId);

    @ResultOf
    @Cypher("MATCH\n" +
            "  (c1:Component:SARF)-[:COUPLES]-(c)-[:COUPLES]-(c2:Component:SARF)\n" +
            "WHERE \n" +
            "  ID(c1) IN {ids} AND ID(c2) IN {ids} AND ID(c1) > ID(c2)\n" +
            "WITH\n" +
            "  DISTINCT c1, c2, c\n" +
            "MATCH\n" +
            "  (c1)-[coup:COUPLES]-(c)\n" +
            "WITH\n" +
            "  c1, c2, c, SUM(coup.coupling) AS c1Coup\n" +
            "MATCH\n" +
            "  (c2)-[coup:COUPLES]-(c)\n" +
            "WITH \n" +
            "  c1, c2, c1Coup, SUM(coup.coupling) AS c2Coup\n" +
            "WITH\n" +
            "  c1, c2, SUM(c1Coup) + SUM(c2Coup) AS intersection\n" +
            "OPTIONAL MATCH\n" +
            "  (c1)-[coup:COUPLES]-(c)\n" +
            "WHERE \n" +
            "  c <> c2\n" +
            "WITH \n" +
            "  c1, c2, intersection, sum(coup.coupling) AS c1Coup\n" +
            "OPTIONAL MATCH\n" +
            "  (c2)-[coup:COUPLES]-(c)\n" +
            "WHERE\n" +
            "  c <> c1\n" +
            "WITH \n" +
            "  c1, c2, c1Coup, sum(coup.coupling) AS c2Coup, intersection\n" +
            "MERGE\n" +
            "  (c1)-[:IS_SIMILAR_TO{similarity:(intersection / (c1Coup + c2Coup))}]-(c2)")
    void computeSimilarityBetweenComponents(@Parameter("ids") long[] ids);

    @ResultOf
    @Cypher("MATCH" +
            "  (c:Component)-[:CONTAINS*]->(t:Type:Internal) " +
            "WHERE" +
            "  ID(c) = {id} " +
            "RETURN" +
            "  DISTINCT t")
    Result<TypeDescriptor> getContainedTypesRecursively(@Parameter("id") long id);

    @ResultOf
    @Cypher("MATCH" +
            "  (c:Component:SARF)-[:CONTAINS]->(e)-[coup:COUPLES]->(t:Type:Internal) " +
            "WHERE" +
            "  ID(c) IN {ids} AND ID(t) IN {ids} " +
            "WITH" +
            "  c, sum(coup.coupling) AS coupling, t " +
            "MATCH" +
            "  (c)-[cont:CONTAINS]->(e) " +
            "WITH" +
            "  c, coupling, count(e) AS max, t " +
            "MERGE " +
            "  (c)-[:COUPLES{coupling:(coupling / max)}]->(t)")
    void computeCouplingBetweenComponentsAndTypes(@Parameter("ids") long[] ids);

    @ResultOf
    @Cypher("MATCH" +
            "  (c:Component:SARF)-[:CONTAINS]->(e)<-[coup:COUPLES]-(t:Type:Internal) " +
            "WHERE" +
            "  ID(c) IN {ids} AND ID(t) IN {ids} " +
            "WITH" +
            "  c, sum(coup.coupling) AS coupling, t " +
            "MATCH" +
            "  (c)-[cont:CONTAINS]->(e) " +
            "WITH" +
            "  c, coupling, count(e) AS max, t " +
            "MERGE " +
            "  (c)<-[:COUPLES{coupling:(coupling / max)}]-(t)")
    void computeCouplingBetweenTypesAndComponents(@Parameter("ids") long[] ids);

    @ResultOf
    @Cypher("MATCH\n" +
            "  (c:Component:SARF)-[:COUPLES]-(e)-[:COUPLES]-(t:Type:Internal)\n" +
            "WHERE \n" +
            "  ID(c) IN {ids} AND ID(t) IN {ids}\n" +
            "WITH\n" +
            "  DISTINCT c, e, t\n" +
            "MATCH\n" +
            "  (c)-[coup:COUPLES]-(e)\n" +
            "WITH\n" +
            "  c, e, t, SUM(coup.coupling) AS cCoup\n" +
            "MATCH\n" +
            "  (t)-[coup:COUPLES]-(e)\n" +
            "WITH \n" +
            "  c, t, cCoup, SUM(coup.coupling) AS tCoup\n" +
            "WITH\n" +
            "  c, t, SUM(cCoup) + SUM(tCoup) AS intersection\n" +
            "OPTIONAL MATCH\n" +
            "  (c)-[coup:COUPLES]-(e)\n" +
            "WHERE \n" +
            "  e <> t\n" +
            "WITH \n" +
            "  c, t, intersection, sum(coup.coupling) AS cCoup\n" +
            "OPTIONAL MATCH\n" +
            "  (t)-[coup:COUPLES]-(e)\n" +
            "WHERE\n" +
            "  c <> e\n" +
            "WITH \n" +
            "  c, t, cCoup, sum(coup.coupling) AS tCoup, intersection\n" +
            "MERGE\n" +
            "  (c)-[:IS_SIMILAR_TO{similarity:(intersection / (cCoup + tCoup))}]-(t)")
    void computeSimilarityBetweenComponentsAndTypes(@Parameter("ids") long[] ids);

    @ResultOf
    @Cypher("MATCH\n" +
            "  (c:Component:SARF)\n" +
            "WHERE\n" +
            "  c.name STARTS WITH \"COH1\"\n" +
            "OPTIONAL MATCH\n" +
            "  (c)-[cont:CONTAINS]->(c1)\n" +
            "RETURN\n" +
            "  { c: c, " +
            "    cont: cont, " +
            "    c1: c1" +
            "  }")
    Result<Map> getDecomposition(@Parameter("ids") long[] ids);
}
