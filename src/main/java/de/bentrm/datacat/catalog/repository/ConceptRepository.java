package de.bentrm.datacat.catalog.repository;

import de.bentrm.datacat.base.repository.EntityRepository;
import de.bentrm.datacat.catalog.domain.XtdConcept;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConceptRepository extends EntityRepository<XtdConcept> {

    @Query("""
            MATCH (n:XtdConcept {id: $conceptId})-[:SIMILAR_TO]->(p:XtdConcept)
            RETURN p.id""")
    List<String> findAllConceptIdsAssignedToConcept(String conceptId);
}
