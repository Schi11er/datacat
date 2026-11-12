package de.bentrm.datacat.catalog.repository;

import de.bentrm.datacat.base.repository.EntityRepository;
import de.bentrm.datacat.catalog.domain.XtdSymbol;

import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

public interface SymbolRepository extends EntityRepository<XtdSymbol> {

    @Query("""
        MATCH (n:XtdSymbol {id: $symbolId})-[:SUBJECT]->(p:XtdSubject)
        RETURN p.id""")
    String findSubjectIdAssignedToSymbol(String symbolId);

    // @Query("""
    //     MATCH (n:XtdSymbol {id: $symbolId})-[:SUBJECT]->(p:XtdSubject)
    //     RETURN p""")
    // Optional<XtdSubject> findSubjectAssignedToSymbol(String symbolId);

    @Query("""
        MATCH (n:XtdSymbol {id: $symbolId})-[:SYMBOL]->(p:XtdText)
        RETURN p.id""")
    String findSymbolText(String symbolId);

    @Query("""
        MATCH (n:XtdSymbol {id: $symbolId})
        MATCH (sub:XtdSubject {id: $subjectId})
        MERGE (n)-[:SUBJECT]->(sub)
        """)
    void attachSubject(@Param("symbolId") String symbolId, @Param("subjectId") String subjectId);

    @Query("RETURN EXISTS { MATCH (s:XtdSymbol {id: $symbolId})-[:SUBJECT]->() }")
    boolean hasSubject(@Param("symbolId") String symbolId);

}