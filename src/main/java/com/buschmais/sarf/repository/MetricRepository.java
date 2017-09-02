package com.buschmais.sarf.repository;

import com.buschmais.xo.api.annotation.Repository;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

/**
 * @author Stephan Pirnbaum
 */
@Repository
public interface MetricRepository {

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} " +
            "WITH" +
            "  t1 " +
            "MATCH" +
            "  (t2:Type) " +
            "WHERE" +
            "  ID(t2) = {t2} " +
            "WITH" +
            "  t1, t2 " +
            "MATCH" +
            " (t1)-[:DECLARES]->(:Method)-[i:INVOKES]->(m:Method)<-[:DECLARES]-(t2) " +
            "WHERE" +
            "  NOT EXISTS(m.static) OR m.static = false " +
            "RETURN" +
            "  count(i)")
    Long countInvokes(@Parameter("t1") Long t1,
                      @Parameter("t2") Long t2);

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type) " +
            "WHERE" +
            "  ID(t) = {t} " +
            "WITH" +
            "  t " +
            "MATCH" +
            "  (t)-[:DECLARES]->(:Method)-[i:INVOKES]->(m:Method)<-[:DECLARES]-(t2:Type) " +
            "WHERE" +
            "  ID(t) <> ID(t2) AND (NOT EXISTS(m.static) OR m.static = false) " +
            "RETURN" +
            "  count(i)")
    Long countAllInvokesExternal(@Parameter("t") Long t);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} " +
            "WITH" +
            "  t1 " +
            "MATCH" +
            "  (t2:Type) " +
            "WHERE" +
            "  ID(t2) = {t2} " +
            "WITH" +
            "  t1, t2 " +
            "MATCH" +
            " (t1)-[:DECLARES]->(:Method)-[i:INVOKES]->(:Method{static:true})<-[:DECLARES]-(t2) " +
            "RETURN" +
            "  count(i)")
    Long countInvokesStatic(@Parameter("t1") Long t1,
                      @Parameter("t2") Long t2);

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type) " +
            "WHERE" +
            "  ID(t) = {t} " +
            "WITH" +
            "  t " +
            "MATCH" +
            "  (t)-[:DECLARES]->(:Method)-[i:INVOKES]->(:Method{static:true})<-[:DECLARES]-(t2:Type) " +
            "WHERE" +
            "  ID(t) <> ID(t2) " +
            "RETURN" +
            "  count(i)")
    Long countAllInvokesExternalStatic(@Parameter("t") Long t);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} " +
            "WITH" +
            "  t1 " +
            "MATCH" +
            "  (t2:Type) " +
            "WHERE" +
            "  ID(t2) = {t2} " +
            "WITH" +
            "  t1, t2 " +
            "MATCH " +
            "  (t1)-[e:EXTENDS]->(t2) " +
            "RETURN" +
            "  count(e) > 0")
    boolean typeExtends(@Parameter("t1") Long t1, @Parameter("t2") Long t2);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} " +
            "WITH" +
            "  t1 " +
            "MATCH" +
            "  (t2:Type) " +
            "WHERE" +
            "  ID(t2) = {t2} " +
            "WITH" +
            "  t1, t2 " +
            "MATCH " +
            "  (t1)-[e:IMPLEMENTS]->(t2) " +
            "RETURN" +
            "  count(e) > 0")
    boolean typeImplements(@Parameter("t1") Long t1, @Parameter("t2") Long t2);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type)-[:DECLARES]->(:Method)-[r:RETURNS]->(ret:Type) " +
            "WHERE" +
            "  ID(t1) = {t1}" +
            "  AND ID(ret) = {t2} " +
            "RETURN" +
            "  count(r)")
    Long countReturns(@Parameter("t1") Long t1, @Parameter("t2") Long t2);

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type)-[:DECLARES]->(m:Method) " +
            "WHERE" +
            "  ID(t) = {t} " +
            "RETURN" +
            "  count(m)")
    Long countMethods(@Parameter("t") Long t);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type)-[:DECLARES]->(m:Method)-[:HAS]->(:Parameter)-[:OF_TYPE]->(t2:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} AND ID(t2) = {t2}" +
            "RETURN" +
            "  count(DISTINCT m)")
    Long countParameterized(@Parameter("t1") Long t1, @Parameter("t2") Long t2);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type)-[:DECLARES]->(:Method)-[r:READS]->(f:Field)<-[:DECLARES]-(t2:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} AND ID(t2) = {t2} AND (NOT EXISTS(f.static) OR f.static = false) " +
            "RETURN" +
            "  count(DISTINCT r)")
    Long countReads(@Parameter("t1") Long t1, @Parameter("t2") Long t2);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type)-[:DECLARES]->(:Method)-[r:READS]->(f:Field)<-[:DECLARES]-(t2:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} AND ID(t1) <> ID(t2) AND (NOT EXISTS(f.static) OR f.static = false) " +
            "RETURN" +
            "  count(DISTINCT r)")
    Long countReadsExternal(@Parameter("t1") Long t);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type)-[:DECLARES]->(f:Field)<-[r:READS]-(:Method)<-[:DECLARES]-(t2:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} AND ID(t1) <> ID(t2) AND (NOT EXISTS(f.static) OR f.static = false) " +
            "RETURN" +
            "  count(DISTINCT r)")
    Long countReadByExternal(@Parameter("t1") Long t);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type)-[:DECLARES]->(:Method)-[r:READS]->(f:Field{static:true})<-[:DECLARES]-(t2:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} AND ID(t2) = {t2} " +
            "RETURN" +
            "  count(DISTINCT r)")
    Long countReadsStatic(@Parameter("t1") Long t1, @Parameter("t2") Long t2);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type)-[:DECLARES]->(:Method)-[r:READS]->(f:Field{static:true})<-[:DECLARES]-(t2:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} AND ID(t1) <> ID(t2) " +
            "RETURN" +
            "  count(DISTINCT r)")
    Long countReadsStaticExternal(@Parameter("t1") Long t);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type)-[:DECLARES]->(f:Field{static:true})<-[r:READS]-(:Method)<-[:DECLARES]-(t2:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} AND ID(t1) <> ID(t2) " +
            "RETURN" +
            "  count(DISTINCT r)")
    Long countReadByExternalStatic(@Parameter("t1") Long t);



    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type)-[:DECLARES]->(:Method)-[w:WRITES]->(f:Field)<-[:DECLARES]-(t2:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} AND ID(t2) = {t2} AND (NOT EXISTS(f.static) OR f.static = false) " +
            "RETURN" +
            "  count(DISTINCT w)")
    Long countWrites(@Parameter("t1") Long t1, @Parameter("t2") Long t2);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type)-[:DECLARES]->(:Method)-[w:WRITES]->(f:Field)<-[:DECLARES]-(t2:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} AND ID(t1) <> ID(t2) AND (NOT EXISTS(f.static) OR f.static = false) " +
            "RETURN" +
            "  count(DISTINCT w)")
    Long countWritesExternal(@Parameter("t1") Long t);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type)-[:DECLARES]->(f:Field)<-[w:WRITES]-(:Method)<-[:DECLARES]-(t2:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} AND ID(t1) <> ID(t2) AND (NOT EXISTS(f.static) OR f.static = false) " +
            "RETURN" +
            "  count(DISTINCT w)")
    Long countWrittenByExternal(@Parameter("t1") Long t);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type)-[:DECLARES]->(:Method)-[w:WRITES]->(f:Field{static:true})<-[:DECLARES]-(t2:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} AND ID(t2) = {t2} " +
            "RETURN" +
            "  count(DISTINCT w)")
    Long countWritesStatic(@Parameter("t1") Long t1, @Parameter("t2") Long t2);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type)-[:DECLARES]->(:Method)-[w:WRITES]->(f:Field{static:true})<-[:DECLARES]-(t2:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} AND ID(t1) <> ID(t2) " +
            "RETURN" +
            "  count(DISTINCT w)")
    Long countWritesStaticExternal(@Parameter("t1") Long t);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type)-[:DECLARES]->(f:Field{static:true})<-[w:WRITES]-(:Method)<-[:DECLARES]-(t2:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} AND ID(t1) <> ID(t2) " +
            "RETURN" +
            "  count(DISTINCT w)")
    Long countWrittenByExternalStatic(@Parameter("t1") Long t);


    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} " +
            "WITH" +
            "  t1 " +
            "MATCH" +
            "  (t2:Type) " +
            "WHERE" +
            "  ID(t2) = {t2} " +
            "MERGE" +
            "  (t1)-[c:COUPLES{coupling:{coupling}}]->(t2) " +
            "RETURN" +
            "  c")
    void setCoupling(@Parameter("t1") Long id1, @Parameter("t2") Long id2, @Parameter("coupling") Double coupling);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type)-[:DECLARES]->(f:Field)-[:OF_TYPE]->(t2:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} AND ID(t2) = {t2} " +
            "RETURN" +
            "  count(DISTINCT f) > 0")
    boolean typeComposes(@Parameter("t1") Long id1, @Parameter("t2") Long id2);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type)-[d:DECLARES]->(t2:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} AND ID(t2) = {t2} " +
            "RETURN" +
            "  count(DISTINCT d) > 0")
    boolean declaresInnerClass(@Parameter("t1") Long id1, @Parameter("t2") Long id2);



    @ResultOf
    @Cypher("MATCH" +
            "  (e1)-[c:COUPLES]->(e2) " +
            "WHERE" +
            "  ID(e1) IN {ids1} AND ID(e2) IN {ids2} " +
            "RETURN" +
            "  toFloat(SUM(c.coupling))")
    Double computeCouplingBetweenComponents(@Parameter("ids1") long[] ids1, @Parameter("ids2") long[] ids2);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type), (t2:Type) " +
            "WHERE" +
            "  ID(t1) = {id1} AND ID(t2) = {id2} " +
            "RETURN" +
            "  EXISTS((t1)-[:DEPENDS_ON]->(t2))")
    boolean dependsOn(@Parameter("id1") Long id1, @Parameter("id2") Long id2);

    @ResultOf
    @Cypher("MATCH" +
            "  (e1)-[s:IS_SIMILAR_TO]->(e2) " +
            "WHERE" +
            "  ID(e1) IN {ids} AND ID(e2) IN {ids} " +
            "RETURN" +
            "  toFloat(SUM(s.similarity))")
    Double computeSimilarityCohesionInComponent(@Parameter("ids") long[] ids);

    @ResultOf
    @Cypher("MATCH" +
            "  (e1)-[s:IS_SIMILAR_TO]->(e2) " +
            "WHERE" +
            "  ID(e1) IN {ids1} AND ID(e2) IN {ids2} " +
            "RETURN" +
            "  toFloat(SUM(s.similarity))")
    Double computeSimilarityCouplingBetweenComponents(@Parameter("ids1") long[] ids1, @Parameter("ids2") long[] ids2);

    @ResultOf
    @Cypher("MATCH" +
            "  (e1)-[c:COUPLES]->(e2) " +
            "WHERE" +
            "  ID(e1) IN {ids} AND ID(e2) IN {ids} " +
            "RETURN" +
            "  toFloat(SUM(c.coupling))")
    Double computeCouplingCohesionInComponent(@Parameter("ids") long[] ids);

    @ResultOf
    @Cypher("MATCH" +
            "  (t1:Type) " +
            "WHERE" +
            "  ID(t1) = {t1} " +
            "WITH" +
            "  t1 " +
            "MATCH" +
            "  (t2:Type) " +
            "WHERE" +
            "  ID(t2) = {t2} " +
            "WITH" +
            "  t1, t2 " +
            "MATCH" +
            " (t1)-[:DECLARES]->(:Method)-[i:INVOKES]->(:Method{abstract:true})<-[:DECLARES]-(t2) " +
            "RETURN" +
            "  count(i)")
    Long countInvokesAbstract(@Parameter("t1") Long id1, @Parameter("t2") Long id2);

    @ResultOf
    @Cypher("MATCH" +
            "  (t:Type) " +
            "WHERE" +
            "  ID(t) = {t} " +
            "WITH" +
            "  t " +
            "MATCH" +
            "  (t)-[:DECLARES]->(:Method)-[i:INVOKES]->(:Method{abstract:true})<-[:DECLARES]-(t2:Type) " +
            "WHERE" +
            "  ID(t) <> ID(t2) " +
            "RETURN" +
            "  count(i)")
    Long countAllInvokesExternalAbstract(@Parameter("t") Long t);
}
