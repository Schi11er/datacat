package de.bentrm.datacat.catalog.repository;

import de.bentrm.datacat.base.repository.EntityRepository;
import de.bentrm.datacat.catalog.domain.XtdLanguage;

import java.util.Optional;

import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LanguageRepository extends EntityRepository<XtdLanguage> {

        @Query("MATCH (n:XtdLanguage) WHERE n.code = $languageCode RETURN n")
        Optional<XtdLanguage> findByCode(String languageCode);

        @Query("""
                MATCH (concept {id: $conceptId})
                MATCH (lang:XtdLanguage {id: $languageId})
                MERGE (concept)-[:LANGUAGE_OF_CREATOR]->(lang)
                """)
        void attachLanguageOfCreator(@Param("conceptId") String conceptId, @Param("languageId") String languageId);

        @Query("""
                MATCH (text:XtdText {id: $textId})
                MATCH (lang:XtdLanguage {id: $languageId})
                MERGE (text)-[:LANGUAGE]->(lang)
                """)
        void attachTextLanguage(@Param("textId") String textId, @Param("languageId") String languageId);
}
