package de.bentrm.datacat.catalog.repository;

import de.bentrm.datacat.base.repository.EntityRepository;
import de.bentrm.datacat.catalog.domain.XtdRelationshipType;
import de.bentrm.datacat.catalog.domain.Enums.XtdRelationshipKindEnum;

import java.util.Optional;

import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

public interface RelationshipTypeRepository extends EntityRepository<XtdRelationshipType> {

 @Query("""
            MATCH (t:XtdRelationshipType {kind: $kind})-[:NAMES]->(:XtdMultiLanguageText)-[:TEXTS]->(txt:XtdText {text: $name})
            RETURN t
            LIMIT 1
            """)
    Optional<XtdRelationshipType> findByNameAndKind(@Param("name") String name, @Param("kind") XtdRelationshipKindEnum kind);
}
