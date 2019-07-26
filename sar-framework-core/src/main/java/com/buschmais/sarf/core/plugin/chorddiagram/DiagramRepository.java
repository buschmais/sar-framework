package com.buschmais.sarf.core.plugin.chorddiagram;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.sarf.core.framework.metamodel.ComponentDescriptor;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.annotation.Repository;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

import java.util.Map;

@Repository
public interface DiagramRepository {

    @ResultOf
    @Cypher("MATCH" +
        "  (source:Type:Internal)-[d:DEPENDS_ON]->(target:Type:Internal) " +
        "WHERE" +
        "  id(source) = {sId) and id(target) = {tId} " +
        "RETURN" +
        "  d.weight")
    Long getDependencyWeight(@Parameter("sId") Long sId, @Parameter("tId") Long tId);

    @ResultOf
    @Cypher("MATCH" +
        "  (c:Component:SARF)-[:CONTAINS]->(d:Component:SARF) " +
        "WHERE" +
        "  id(c) = {parId} " +
        "WITH" +
        "  d " +
        "MATCH" +
        "  (t:Type:Internal) " +
        "WHERE" +
        "  id(t) = {tId} " +
        "WITH" +
        "  d, t " +
        "MATCH" +
        "  (d)-[:CONTAINS*]->(t1:Type:Internal)<-[dep:DEPENDS_ON]-(t) " +
        "WITH " +
        "  d.name AS name, toInt(sum(dep.weight)) AS weight " +
        "RETURN" +
        "  {" +
        "    name: name," +
        "    weight: weight" +
        "  }")
    Result<Map> getTypeComponentDependenciesIn(@Parameter("parId") Long parId, @Parameter("tId") Long tId);

    @ResultOf
    @Cypher("MATCH" +
        "  (c:Component:SARF)-[:CONTAINS]->(d:Type:Internal) " +
        "WHERE" +
        "  id(c) = {parId} " +
        "WITH " +
        "  d " +
        "MATCH" +
        "  (t:Type:Internal)-[dep:DEPENDS_ON]->(d) " +
        "WHERE" +
        "  id(t) = {tId} " +
        "WITH" +
        "  d.name AS name, toInt(dep.weight) AS weight " +
        "RETURN" +
        "  {" +
        "    name: name," +
        "    weight: weight" +
        "  }")
    Result<Map> getTypeTypeDependenciesIn(@Parameter("parId") Long parId, @Parameter("tId") Long tId);

    @ResultOf
    @Cypher("MATCH" +
        "  (c:Component:SARF), (c1:Component:SARF) " +
        "WHERE" +
        "  id(c) = {cId} and id(c1) = {toId} " +
        "WITH" +
        "  c, c1 " +
        "MATCH" +
        "  (c)-[:CONTAINS*]->(t:Type:Internal) " +
        "WITH" +
        "  c1, t " +
        "OPTIONAL MATCH" +
        "  (t1:Type:Internal)<-[:CONTAINS*]-(c1) " +
        "WITH" +
        "  t, t1 " +
        "OPTIONAL MATCH" +
        "  (t)-[dep:DEPENDS_ON]->(t1) " +
        "WITH " +
        "  DISTINCT dep " +
        "RETURN" +
        "  sum(dep.weight)")
    Long getDependencyCount(@Parameter("cId") Long cId, @Parameter("toId") Long toId);

    @ResultOf
    @Cypher("MATCH" +
        "  (c:Component:SARF)-[:COUPLES]->(c1:Component:SARF) " +
        "WHERE" +
        "  id(c) = {cId} " +
        "RETURN" +
        "  DISTINCT c1")
    Result<ComponentDescriptor> getDependencies(@Parameter("cId") Long cId);

    @ResultOf
    @Cypher("MATCH" +
        "  (par:Component:SARF)-[:CONTAINS]->(c:Component:SARF)-[:COUPLES]->(t:Type:Internal)<-[:CONTAINS]-(par) " +
        "WHERE" +
        "  id(c) = {cId} " +
        "RETURN" +
        "  DISTINCT t")
    Result<TypeDescriptor> getTypeDependencies(@Parameter("cId") Long cId);

    @ResultOf
    @Cypher("MATCH" +
        "  (c:Component:SARF), (t1:Type) " +
        "WHERE" +
        "  id(c) = {cId} and id(t1) = {toId} " +
        "WITH" +
        "  c, t1 " +
        "MATCH" +
        "  (c)-[:CONTAINS*]->(t:Type:Internal) " +
        "WITH" +
        "  t1, t " +
        "OPTIONAL MATCH" +
        "  (t)-[dep:DEPENDS_ON]->(t1) " +
        "WITH " +
        "  DISTINCT dep " +
        "RETURN" +
        "  sum(dep.weight)")
    Long getTypeDependencyCount(@Parameter("cId") Long cId, @Parameter("toId") Long toId);

    @ResultOf
    @Cypher("MATCH" +
        "  (c:Component:SARF)-[:CONTAINS*]->(t:Type:Internal) " +
        "WHERE" +
        "  id(c) = {cId} " +
        "RETURN" +
        "  count(DISTINCT t)")
    Long getTypeCountRecursive(@Parameter("cId") Long cId);
}
