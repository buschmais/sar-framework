package com.buchmais.sarf.repository;

import com.buchmais.sarf.node.ComponentDescriptor;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.annotation.Repository;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.TypedNeo4jRepository;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

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
            "  (:ClassificationConfiguration{iteration: current})-[:CONTAINS]->(:ClassificationCriterion)" +
            "    -[:CREATED]->(:ClassificationInfo)-[:MAPS]->(c:Component {shape: {shape}, name: {name}})" +
            "RETURN" +
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
            "  type" +
            "OPTIONAL MATCH" +
            "  (type)" +
            "    <-[:CLASSIFIES]-(info1:ClassificationInfo {iteration:{iteration}})-[:MAPS]->" +
            "  (:SARF:Component {shape:{shape1}, name:{name1}}) " +
            "WHERE" +
            "  info1 IS NULL" +
            "RETURN" +
            "  count(DISTINCT type)")
    Long computeComplementCardinality(@Parameter("shape1") String ofShape, @Parameter("name1") String ofName,
                                      @Parameter("shape2") String inShape, @Parameter("name2") String inName,
                                      @Parameter("iteration") Integer iteration);
}
