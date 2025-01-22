package de.bentrm.datacat.catalog.service.impl;

import de.bentrm.datacat.catalog.domain.CatalogRecordType;
import de.bentrm.datacat.catalog.domain.SimpleRelationType;
import de.bentrm.datacat.catalog.domain.XtdDictionary;
import de.bentrm.datacat.catalog.domain.XtdMultiLanguageText;
import de.bentrm.datacat.catalog.repository.DictionaryRepository;
import de.bentrm.datacat.catalog.service.CatalogCleanupService;
import de.bentrm.datacat.catalog.service.DictionaryRecordService;
import de.bentrm.datacat.catalog.service.MultiLanguageTextRecordService;
import de.bentrm.datacat.catalog.service.dto.Relationships.NameDtoProjection;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.Assert;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jTemplate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Slf4j
@Service
@Validated
@Transactional(readOnly = true)
public class DictionaryRecordServiceImpl
        extends AbstractSimpleRecordServiceImpl<XtdDictionary, DictionaryRepository>
        implements DictionaryRecordService {

    @Autowired
    private MultiLanguageTextRecordService multiLanguageTextRecordService;

    public DictionaryRecordServiceImpl(Neo4jTemplate neo4jTemplate,
                                     DictionaryRepository repository,
                                     CatalogCleanupService cleanupService) {
        super(XtdDictionary.class, neo4jTemplate, repository, cleanupService);
    }

    @Override
    public @NotNull CatalogRecordType getSupportedCatalogRecordType() {
        return CatalogRecordType.Dictionary;
    }

    @Override
    public @NotNull XtdMultiLanguageText getName(XtdDictionary dictionary) {
        Assert.notNull(dictionary.getId(), "Dictionary must be persistent.");
        final String nameId = getRepository().findMultiLanguageTextIdAssignedToDictionary(dictionary.getId());
        if (nameId == null) {
            return null;
        }
        final XtdMultiLanguageText name = multiLanguageTextRecordService.findByIdWithDirectRelations(nameId).orElseThrow();
        return name;
    }

    @Transactional
    @Override
    public @NotNull XtdDictionary setRelatedRecords(@NotBlank String recordId,
                                                    @NotEmpty List<@NotBlank String> relatedRecordIds, @NotNull SimpleRelationType relationType) {

        final XtdDictionary dictionary = getRepository().findById(recordId).orElseThrow();

        switch (relationType) {
            case Name -> {
                if (dictionary.getName() != null) {
                    throw new IllegalArgumentException("Dictionary already has a name assigned.");
                } else if (relatedRecordIds.size() != 1) {
                    throw new IllegalArgumentException("Exactly one name must be assigned to a dictionary.");
                } else {
                    final XtdMultiLanguageText name = multiLanguageTextRecordService.findByIdWithDirectRelations(relatedRecordIds.get(0)).orElseThrow();
                    dictionary.setName(name);
                }   }
            default -> log.error("Unsupported relation type: {}", relationType);
        }
        
        neo4jTemplate.saveAs(dictionary, NameDtoProjection.class); 
        log.trace("Updated relationship: {}", dictionary);
        return dictionary;
    }
}
