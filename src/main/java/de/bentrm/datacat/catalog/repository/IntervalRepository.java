package de.bentrm.datacat.catalog.repository;

import de.bentrm.datacat.base.repository.EntityRepository;
import de.bentrm.datacat.catalog.domain.XtdInterval;

import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

public interface IntervalRepository extends EntityRepository<XtdInterval> {

    @Query("""
            MATCH (n:XtdInterval {id: $intervalId})-[:MAXIMUM]->(p:XtdValueList)
            RETURN p.id""")
    String findMaxValueListIdAssignedToInterval(String intervalId);

    @Query("""
            MATCH (n:XtdInterval {id: $intervalId})-[:MINIMUM]->(p:XtdValueList)
            RETURN p.id""")
    String findMinValueListIdAssignedToInterval(String intervalId);

        @Query("RETURN EXISTS { MATCH (o {id: $id})-[:MINIMUM]->() }")
        boolean hasMinimumAssigned(@Param("id") String id);

        @Query("RETURN EXISTS { MATCH (o {id: $id})-[:MAXIMUM]->() }")
        boolean hasMaximumAssigned(@Param("id") String id);

        @Query("""
                MATCH (o {id: $intervalId})
                MATCH (max:XtdValueList {id: $maxValueListId})
                MERGE (o)-[:MAXIMUM]->(max)
                """)
        void attachMaxValueList(@Param("intervalId") String intervalId, @Param("maxValueListId") String maxValueListId);

        @Query("""
                MATCH (o {id: $intervalId})
                MATCH (min:XtdValueList {id: $minValueListId})
                MERGE (o)-[:MINIMUM]->(min)
                """)
        void attachMinValueList(@Param("intervalId") String intervalId, @Param("minValueListId") String minValueListId);

}
