package de.bentrm.datacat.catalog.repository;

import de.bentrm.datacat.base.repository.EntityRepository;
import de.bentrm.datacat.catalog.domain.XtdSubdivision;

import java.util.List;

import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubdivisionRepository extends EntityRepository<XtdSubdivision> {

        @Query("""
                        MATCH (n:XtdSubdivision {id: $subdivisionId})-[:SUBDIVISIONS]->(p:XtdSubdivision)
                        RETURN p.id""")
        List<String> findAllSubdivisionIdsAssignedToSubdivision(String subdivisionId);

    @Query("""
            MATCH (o {id: $divisionId})
            MATCH (subdivision:XtdSubdivision {id: $subdivisionId})
            MERGE (o)-[:SUBDIVISIONS]->(subdivision)
            """)
    void attachSubdivision(@Param("divisionId") String divisionId, @Param("subdivisionId") String subdivisionId);
}
