package com.buschmais.sarf.core.framework.repository;

import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.annotation.Repository;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

import java.util.Map;

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
            "MERGE" +
            "  (t1)-[c:COUPLES{coupling:{coupling}}]->(t2) " +
            "RETURN" +
            "  c")
    void setCoupling(@Parameter("t1") Long id1, @Parameter("t2") Long id2, @Parameter("coupling") Double coupling);


    @ResultOf
    @Cypher("MATCH" +
        "  (t1:Type:Internal)-[:DECLARES]->(:Method)-[i:INVOKES]->(m:Method)<-[:DECLARES]-(t2:Type) " +
        "WHERE" +
        "  ID(t1) <> ID(t2) AND (NOT EXISTS(m.static) OR m.static = false) " +
        "WITH" +
        "  t1, count(i) AS cntInvokes " +
        "MATCH" +
        "  (t1)-[:DECLARES]->(:Method)-[i:INVOKES]->(m:Method)<-[:DECLARES]-(t2:Type:Internal) " +
        "WHERE" +
        "  ID(t1) <> ID(t2) AND (NOT EXISTS(m.static) OR m.static = false) " +
        "WITH" +
        "  ID(t1) AS source, toFloat(count(i))/cntInvokes AS coupling, ID(t2) AS target " +
        "RETURN" +
        "  {" +
        "    source: source," +
        "    coupling: coupling," +
        "    target: target" +
        "  }"
    )
    Result<Map> computeCouplingInvokes();

    @ResultOf
    @Cypher("MATCH" +
        "  (t1:Type:Internal)-[:DECLARES]->(:Method)-[i:INVOKES]->(:Method{abstract:true})<-[:DECLARES]-(t2:Type) " +
        "WHERE" +
        "  ID(t1) <> ID(t2) " +
        "WITH" +
        "  t1, count(i) AS cntInvokes " +
        "MATCH" +
        "  (t1)-[:DECLARES]->(:Method)-[i:INVOKES]->(:Method{abstract:true})<-[:DECLARES]-(t2:Type:Internal) " +
        "WHERE" +
        "  ID(t1) <> ID(t2) " +
        "WITH" +
        "  ID(t1) AS source, toFloat(count(i))/cntInvokes AS coupling, ID(t2) AS target " +
        "RETURN" +
        "  {" +
        "    source: source," +
        "    coupling: coupling," +
        "    target: target" +
        "  }"
    )
    Result<Map> computeCouplingInvokesAbstract();

    @ResultOf
    @Cypher("MATCH" +
        "  (t1:Type:Internal)-[:DECLARES]->(:Method)-[i:INVOKES]->(:Method{static:true})<-[:DECLARES]-(t2:Type) " +
        "WHERE" +
        "  ID(t1) <> ID(t2) " +
        "WITH" +
        "  t1, count(i) AS cntInvokes " +
        "MATCH" +
        "  (t1)-[:DECLARES]->(:Method)-[i:INVOKES]->(:Method{static:true})<-[:DECLARES]-(t2:Type:Internal) " +
        "WHERE" +
        "  ID(t1) <> ID(t2) " +
        "WITH" +
        "  ID(t1) AS source, toFloat(count(i))/cntInvokes AS coupling, ID(t2) AS target " +
        "RETURN" +
        "  {" +
        "    source: source," +
        "    coupling: coupling," +
        "    target: target" +
        "  }"
    )
    Result<Map> computeCouplingInvokesStatic();

    @ResultOf
    @Cypher("MATCH" +
        "  (source:Type:Internal)-[e:EXTENDS]->(target:Type:Internal) " +
        "WITH" +
        "  ID(source) AS source, toFloat(count(e)) AS coupling, ID(target) AS target " +
        "RETURN" +
        "  {" +
        "    source: source," +
        "    coupling: coupling," +
        "    target: target" +
        "  }")
    Result<Map> computeCouplingExtends();

    @ResultOf
    @Cypher("MATCH" +
        "  (source:Type:Internal)-[i:IMPLEMENTS]->(target:Type:Internal) " +
        "WITH" +
        "  ID(source) AS source, toFloat(count(i)) AS coupling, ID(target) AS target " +
        "RETURN" +
        "  {" +
        "    source: source," +
        "    coupling: coupling," +
        "    target: target" +
        "  }")
    Result<Map> computeCouplingImplements();

    @ResultOf
    @Cypher("MATCH" +
        "  (source:Type:Internal)-[:DECLARES]->(m:Method)-[:RETURNS]->(target:Type:Internal) " +
        "WHERE" +
        "  ID(source) <> ID(target) " +
        "WITH" +
        "  source, count(DISTINCT m) AS cntRet, target " +
        "MATCH" +
        "  (source)-[:DECLARES]->(m:Method) " +
        "WITH" +
        "  ID(source) AS source, toFloat(cntRet)/count(m) AS coupling, ID(target) AS target " +
        "RETURN" +
        "  {" +
        "    source: source," +
        "    coupling: coupling," +
        "    target: target" +
        "  }")
    Result<Map> computeCouplingReturns();

    @ResultOf
    @Cypher("MATCH" +
        "  (source:Type:Internal)-[:DECLARES]->(m:Method)-[:HAS]->(:Parameter)-[:OF_TYPE]->(target:Type:Internal) " +
        "WHERE" +
        "  ID(source) <> ID(target) " +
        "WITH" +
        "  source, count(DISTINCT m) AS cntPar, target " +
        "MATCH" +
        "  (source)-[:DECLARES]->(m:Method) " +
        "WITH" +
        "  ID(source) AS source, toFloat(cntPar)/count(m) AS coupling, ID(target) AS target " +
        "RETURN" +
        "  {" +
        "    source: source," +
        "    coupling: coupling," +
        "    target: target" +
        "  }")
    Result<Map> computeCouplingParameterized();

    @ResultOf
    @Cypher("MATCH" +
        "  (source:Type:Internal)-[:DECLARES]->(f:Field)-[:OF_TYPE]->(target:Type:Internal) " +
        "WITH" +
        "  ID(source) AS source, toFloat(count(DISTINCT f)) AS coupling, ID(target) AS target " +
        "RETURN" +
        "  {" +
        "    source: source," +
        "    coupling: coupling," +
        "    target: target" +
        "  }")
    Result<Map> computeCouplingComposes();

    @ResultOf
    @Cypher("MATCH" +
        "  (source:Type:Internal)-[d:DECLARES]->(target:Type:Internal) " +
        "WITH" +
        "  ID(source) AS source, toFloat(count(DISTINCT d)) AS coupling, ID(target) AS target " +
        "RETURN" +
        "  {" +
        "    source: source," +
        "    coupling: coupling," +
        "    target: target" +
        "  }")
    Result<Map> computeCouplingDeclaresInnerClass();

    @ResultOf
    @Cypher("MATCH" +
        "  (source:Type:Internal)-[d:DEPENDS_ON]->(target:Type:Internal) " +
        "WHERE" +
        "  ID(source) <> ID(target) " +
        "WITH" +
        "  ID(source) AS source, toFloat(1) AS coupling, ID(target) AS target " +
        "RETURN" +
        "  {" +
        "    source: source," +
        "    coupling: coupling," +
        "    target: target" +
        "  }")
    Result<Map> computeCouplingDependsOn();

    @ResultOf
    @Cypher("MATCH" +
        "  (source:Type:Internal)-[:DECLARES]->(:Method)-[r:READS]->(f:Field)<-[:DECLARES]-(target:Type:Internal) " +
        "WHERE" +
        "  NOT EXISTS(f.static) OR f.static = false " +
        "WITH" +
        "  source, toFloat(count(DISTINCT r)) AS cntReads, target " +
        "MATCH" +
        "  (source:Type:Internal)-[:DECLARES]->(:Method)-[r:READS]->(f:Field)<-[:DECLARES]-(t:Type) " +
        "WHERE" +
        "  ID(source) <> ID(t) AND (NOT EXISTS(f.static) OR f.static = false) " +
        "WITH" +
        "  source, cntReads, toFloat(count(DISTINCT r)) AS cntReadsExt, target " +
        "MATCH" +
        "  (target:Type:Internal)-[:DECLARES]->(f:Field)<-[r:READS]-(:Method)<-[:DECLARES]-(t:Type) " +
        "WHERE" +
        "  ID(target) <> ID(t) AND (NOT EXISTS(f.static) OR f.static = false) " +
        "WITH" +
        "  source, cntReads, cntReadsExt, toFloat(count(DISTINCT r)) AS cntReadByExt, target " +
        "WITH" +
        "  ID(source) AS source, toFloat(cntReads * cntReads)/(cntReadsExt * cntReadByExt) AS coupling, ID(target) AS target " +
        "RETURN" +
        "  {" +
        "    source: source," +
        "    coupling: coupling," +
        "    target: target" +
        "  }")
    Result<Map> computeCouplingReads();

    @ResultOf
    @Cypher("MATCH" +
        "  (source:Type:Internal)-[:DECLARES]->(:Method)-[r:READS]->(f:Field{static:true})<-[:DECLARES]-(target:Type:Internal) " +
        "WITH" +
        "  source, toFloat(count(DISTINCT r)) AS cntReads, target " +
        "MATCH" +
        "  (source:Type:Internal)-[:DECLARES]->(:Method)-[r:READS]->(f:Field)<-[:DECLARES]-(t:Type) " +
        "WHERE" +
        "  ID(source) <> ID(t) " +
        "WITH" +
        "  source, cntReads, toFloat(count(DISTINCT r)) AS cntReadsExt, target " +
        "MATCH" +
        "  (target:Type:Internal)-[:DECLARES]->(f:Field{static:true})<-[r:READS]-(:Method)<-[:DECLARES]-(t:Type) " +
        "WHERE" +
        "  ID(target) <> ID(t) " +
        "WITH" +
        "  source, cntReads, cntReadsExt, toFloat(count(DISTINCT r)) AS cntReadByExt, target " +
        "WITH" +
        "  ID(source) AS source, toFloat(cntReads * cntReads)/(cntReadsExt * cntReadByExt) AS coupling, ID(target) AS target " +
        "RETURN" +
        "  {" +
        "    source: source," +
        "    coupling: coupling," +
        "    target: target" +
        "  }")
    Result<Map> computeCouplingReadsStatic();

    @ResultOf
    @Cypher("MATCH" +
        "  (source:Type:Internal)-[:DECLARES]->(:Method)-[w:WRITES]->(f:Field)<-[:DECLARES]-(target:Type:Internal) " +
        "WHERE" +
        "  NOT EXISTS(f.static) OR f.static = false " +
        "WITH" +
        "  source, toFloat(count(DISTINCT w)) AS cntWrites, target " +
        "MATCH" +
        "  (source:Type:Internal)-[:DECLARES]->(:Method)-[w:WRITES]->(f:Field)<-[:DECLARES]-(t:Type) " +
        "WHERE" +
        "  ID(source) <> ID(t) AND (NOT EXISTS(f.static) OR f.static = false) " +
        "WITH" +
        "  source, cntWrites, toFloat(count(DISTINCT w)) AS cntWritesExt, target " +
        "MATCH" +
        "  (target:Type:Internal)-[:DECLARES]->(f:Field)<-[w:WRITES]-(:Method)<-[:DECLARES]-(t:Type) " +
        "WHERE" +
        "  ID(target) <> ID(t) AND (NOT EXISTS(f.static) OR f.static = false) " +
        "WITH" +
        "  source, cntWrites, cntWritesExt, toFloat(count(DISTINCT w)) AS cntWrittenByExt, target " +
        "WITH" +
        "  ID(source) AS source, toFloat(cntWrites * cntWrites)/(cntWritesExt * cntWrittenByExt) AS coupling, ID(target) AS target " +
        "RETURN" +
        "  {" +
        "    source: source," +
        "    coupling: coupling," +
        "    target: target" +
        "  }")
    Result<Map> computeCouplingWrites();

    @ResultOf
    @Cypher("MATCH" +
        "  (source:Type:Internal)-[:DECLARES]->(:Method)-[w:WRITES]->(f:Field{static:true})<-[:DECLARES]-(target:Type:Internal) " +
        "WITH" +
        "  source, toFloat(count(DISTINCT w)) AS cntWrites, target " +
        "MATCH" +
        "  (source:Type:Internal)-[:DECLARES]->(:Method)-[w:WRITES]->(f:Field)<-[:DECLARES]-(t:Type) " +
        "WHERE" +
        "  ID(source) <> ID(t) " +
        "WITH" +
        "  source, cntWrites, toFloat(count(DISTINCT w)) AS cntWritesExt, target " +
        "MATCH" +
        "  (target:Type:Internal)-[:DECLARES]->(f:Field{static:true})<-[w:WRITES]-(:Method)<-[:DECLARES]-(t:Type) " +
        "WHERE" +
        "  ID(target) <> ID(t) " +
        "WITH" +
        "  source, cntWrites, cntWritesExt, toFloat(count(DISTINCT w)) AS cntWrittenByExt, target " +
        "WITH" +
        "  ID(source) AS source, toFloat(cntWrites * cntWrites)/(cntWritesExt * cntWrittenByExt) AS coupling, ID(target) AS target " +
        "RETURN" +
        "  {" +
        "    source: source," +
        "    coupling: coupling," +
        "    target: target" +
        "  }")
    Result<Map> computeCouplingWritesStatic();

}
