package de.bentrm.datacat.catalog.service.impl;

import de.bentrm.datacat.catalog.domain.CatalogRecordType;
import de.bentrm.datacat.catalog.domain.SimpleRelationType;
import de.bentrm.datacat.catalog.domain.XtdLanguage;
import de.bentrm.datacat.catalog.domain.XtdText;
import de.bentrm.datacat.catalog.repository.LanguageRepository;
import de.bentrm.datacat.catalog.repository.TextRepository;
import de.bentrm.datacat.catalog.service.CatalogCleanupService;
import de.bentrm.datacat.catalog.service.TextRecordService;
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
public class TextRecordServiceImpl
        extends AbstractSimpleRecordServiceImpl<XtdText, TextRepository>
        implements TextRecordService {

            @Autowired
            private LanguageRepository languageRepository;

    public TextRecordServiceImpl(Neo4jTemplate neo4jTemplate,
                                     TextRepository repository,
                                     CatalogCleanupService cleanupService) {
        super(XtdText.class, neo4jTemplate, repository, cleanupService);
    }

    @Override
    public @NotNull CatalogRecordType getSupportedCatalogRecordType() {
        return CatalogRecordType.Text;
    }

    @Override
    public XtdLanguage getLanguage(XtdText text) {
        Assert.notNull(text, "Text must not be null");
        final String languageId = getRepository().findLanguageIdByTextId(text.getId());
        if (languageId == null) {
            return null;
        }
  
        final XtdLanguage language = languageRepository.findByIdWithDirectRelations(languageId).orElse(null);  

        return language;
        
    }

    @Transactional
    @Override
    public @NotNull XtdText setRelatedRecords(@NotBlank String recordId,
                                                    @NotEmpty List<@NotBlank String> relatedRecordIds, @NotNull SimpleRelationType relationType) {

        final XtdText text = getRepository().findById(recordId).orElseThrow();

        switch (relationType) {
            case Language -> {
                if (text.getLanguage() != null) {
                    throw new IllegalArgumentException("Text already has a language assigned.");
                } else if (relatedRecordIds.size() != 1) {
                    throw new IllegalArgumentException("Exactly one language must be assigned to a text.");
                } else {
                    // final XtdLanguage language = languageRepository.findById(relatedRecordIds.get(0)).orElseThrow();
                    final XtdLanguage language = neo4jTemplate.findById(relatedRecordIds.get(0), XtdLanguage.class).orElseThrow();

                    text.setLanguage(language);
                }
                    }
            default -> log.error("Unsupported relation type: {}", relationType);
        }

        final XtdText persistentText = getRepository().save(text);
        log.trace("Updated relationship: {}", persistentText);
        return persistentText;
    }   

    @Transactional
    @Override
    public XtdText updateText(String textId, String value) {
        final XtdText item = getRepository().findById(textId).orElseThrow();
        item.setText(value.trim());
        return getRepository().save(item);
    }

    @Transactional
    @Override
    public XtdText deleteText(String textId) {
        final XtdText item = getRepository().findById(textId).orElseThrow();
        getRepository().deleteById(textId);
        return item;
    }
}
