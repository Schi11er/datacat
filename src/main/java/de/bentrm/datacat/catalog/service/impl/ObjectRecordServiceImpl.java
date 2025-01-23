package de.bentrm.datacat.catalog.service.impl;

import de.bentrm.datacat.catalog.domain.CatalogRecordType;
import de.bentrm.datacat.catalog.domain.SimpleRelationType;
import de.bentrm.datacat.catalog.domain.XtdDictionary;
import de.bentrm.datacat.catalog.domain.XtdLanguage;
import de.bentrm.datacat.catalog.domain.XtdObject;
import de.bentrm.datacat.catalog.domain.XtdText;
import de.bentrm.datacat.catalog.domain.Enums.XtdStatusOfActivationEnum;
import de.bentrm.datacat.catalog.domain.XtdMultiLanguageText;
import de.bentrm.datacat.catalog.repository.MultiLanguageTextRepository;
import de.bentrm.datacat.catalog.repository.ObjectRepository;
import de.bentrm.datacat.catalog.repository.TextRepository;
import de.bentrm.datacat.catalog.service.CatalogCleanupService;
import de.bentrm.datacat.catalog.service.DictionaryRecordService;
import de.bentrm.datacat.catalog.service.LanguageRecordService;
import de.bentrm.datacat.catalog.service.MultiLanguageTextRecordService;
import de.bentrm.datacat.catalog.service.ObjectRecordService;
import de.bentrm.datacat.catalog.service.dto.ObjectDtoProjection;
import de.bentrm.datacat.catalog.service.dto.Relationships.CommentsDtoProjection;
import de.bentrm.datacat.catalog.service.dto.Relationships.DeprecationExplanationDtoProjection;
import de.bentrm.datacat.catalog.service.dto.Relationships.DictionaryDtoProjection;
import de.bentrm.datacat.catalog.service.dto.Relationships.NamesDtoProjection;
import de.bentrm.datacat.catalog.service.dto.Relationships.ReplacedObjectsDtoProjection;
import de.bentrm.datacat.catalog.service.dto.Relationships.TextsDtoProjection;
import de.bentrm.datacat.graphql.input.AddCommentInput;
import de.bentrm.datacat.graphql.input.AddNameInput;
import de.bentrm.datacat.graphql.input.TranslationInput;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Slf4j
@Service
@Validated
@Transactional(readOnly = true)
public class ObjectRecordServiceImpl extends AbstractSimpleRecordServiceImpl<XtdObject, ObjectRepository>
        implements ObjectRecordService {

    @Autowired
    private DictionaryRecordService dictionaryRecordService;

    @Autowired
    private MultiLanguageTextRecordService multiLanguageTextRecordService;

    @Autowired
    private LanguageRecordService languageRecordService;

    @Autowired
    private TextRepository textRepository;

    @Autowired
    private MultiLanguageTextRepository multiLanguageTextRepository;

    public ObjectRecordServiceImpl(Neo4jTemplate neo4jTemplate, ObjectRepository repository,
            CatalogCleanupService cleanupService) {
        super(XtdObject.class, neo4jTemplate, repository, cleanupService);
    }

    @Override
    public @NotNull CatalogRecordType getSupportedCatalogRecordType() {
        return CatalogRecordType.Object;
    }

    @Override
    public Optional<XtdDictionary> getDictionary(XtdObject object) {
        Assert.notNull(object.getId(), "Object must be persistent.");
        final String dictionaryId = getRepository().findDictionaryIdAssignedToObject(object.getId());
        if (dictionaryId == null) {
            return null;
        }
        final Optional<XtdDictionary> dictionary = dictionaryRecordService.findByIdWithDirectRelations(dictionaryId);
        return dictionary;
    }

    @Override
    public Optional<XtdMultiLanguageText> getDeprecationExplanation(XtdObject object) {
        Assert.notNull(object.getId(), "Object must be persistent.");
        final String deprecationExplanationId = getRepository().findMultiLanguageTextIdAssignedToObject(object.getId());
        if (deprecationExplanationId == null) {
            return null;
        }
        final Optional<XtdMultiLanguageText> deprecationExplanation = multiLanguageTextRecordService
                .findByIdWithDirectRelations(deprecationExplanationId);
        return deprecationExplanation;
    }

    @Override
    public List<XtdObject> getReplacedObjects(XtdObject object) {
        log.info("Fetching replaced objects for object {}", object.getId());
        Assert.notNull(object.getId(), "Object must be persistent.");
        final List<String> replacedObjectIds = getRepository().findAllReplacedObjectIdsAssignedToObject(object.getId());
        final Iterable<XtdObject> replacedObjects = getRepository().findAllEntitiesById(replacedObjectIds);

        return StreamSupport.stream(replacedObjects.spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public List<XtdObject> getReplacingObjects(XtdObject object) {
        Assert.notNull(object.getId(), "Object must be persistent.");
        final List<String> replacingObjectIds = getRepository()
                .findAllReplacingObjectIdsAssignedToObject(object.getId());
        final Iterable<XtdObject> replacingObjects = getRepository().findAllEntitiesById(replacingObjectIds);

        return StreamSupport.stream(replacingObjects.spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public List<XtdMultiLanguageText> getNames(XtdObject object) {
        Assert.notNull(object.getId(), "Object must be persistent.");
        final List<String> nameIds = getRepository().findAllNamesAssignedToObject(object.getId());
        final Iterable<XtdMultiLanguageText> names = multiLanguageTextRecordService.findAllEntitiesById(nameIds);

        return StreamSupport.stream(names.spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public List<XtdMultiLanguageText> getComments(XtdObject object) {
        Assert.notNull(object.getId(), "Object must be persistent.");
        final List<String> commentsId = getRepository().findCommentsAssignedToObject(object.getId());
        if (commentsId == null) {
            return null;
        }
        final Iterable<XtdMultiLanguageText> comments = multiLanguageTextRecordService.findAllEntitiesById(commentsId);

        return StreamSupport.stream(comments.spliterator(), false).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public @NotNull XtdObject setRelatedRecords(@NotBlank String recordId,
            @NotEmpty List<@NotBlank String> relatedRecordIds, @NotNull SimpleRelationType relationType) {

        final XtdObject object = getRepository().findById(recordId).orElseThrow(() -> new IllegalArgumentException("No record with id " + recordId + " found."));

        switch (relationType) {
        case Dictionary -> {
            if (object.getDictionary() != null) {
                throw new IllegalArgumentException("Object already has a dictionary assigned.");
            } else if (relatedRecordIds.size() != 1) {
                throw new IllegalArgumentException("Exactly one dictionary must be assigned.");
            } else {
                final XtdDictionary dictionary = dictionaryRecordService
                        .findByIdWithDirectRelations(relatedRecordIds.get(0)).orElseThrow(() -> new IllegalArgumentException("No record with id " + relatedRecordIds.get(0) + " found."));
                object.setDictionary(dictionary);
            }
            neo4jTemplate.saveAs(object, DictionaryDtoProjection.class);
        }
        case DeprecationExplanation -> {
            if (object.getDeprecationExplanation() != null) {
                throw new IllegalArgumentException("Object already has a deprecation explanation assigned.");
            } else if (relatedRecordIds.size() != 1) {
                throw new IllegalArgumentException("Exactly one deprecation explanation must be assigned.");
            } else {
                final XtdMultiLanguageText deprecationExplanation = multiLanguageTextRecordService
                        .findByIdWithDirectRelations(relatedRecordIds.get(0)).orElseThrow(() -> new IllegalArgumentException("No record with id " + relatedRecordIds.get(0) + " found."));
                object.setDeprecationExplanation(deprecationExplanation);
            }
            neo4jTemplate.saveAs(object, DeprecationExplanationDtoProjection.class);
        }
        case ReplacedObjects -> {
            final Iterable<XtdObject> replacedObjects = getRepository().findAllEntitiesById(relatedRecordIds);
            final List<XtdObject> relatedObjects = StreamSupport.stream(replacedObjects.spliterator(), false)
                    .collect(Collectors.toList());

            object.getReplacedObjects().clear();
            object.getReplacedObjects().addAll(relatedObjects);
            neo4jTemplate.saveAs(object, ReplacedObjectsDtoProjection.class);
        }
        default -> log.error("Unsupported relation type: {}", relationType);
        }

        log.trace("Updated relationship: {}", object);
        return object;
    }

    @Transactional
    @Override
    public XtdObject addComment(AddCommentInput input) {
        final XtdObject item = getRepository().findByIdWithDirectRelations(input.getCatalogEntryId()).orElseThrow(() -> new IllegalArgumentException("No record with id " + input.getCatalogEntryId() + " found."));
        TranslationInput translation = input.getComment();
        
        XtdText text = createText(translation);

        XtdMultiLanguageText multiLanguage = item.getComments().stream().findFirst().orElse(null);
        if (multiLanguage == null) {
            multiLanguage = new XtdMultiLanguageText();
            multiLanguage = multiLanguageTextRepository.save(multiLanguage);
            item.getComments().add(multiLanguage);
            neo4jTemplate.saveAs(item, CommentsDtoProjection.class);
        } else {
            multiLanguage = multiLanguageTextRecordService.findByIdWithDirectRelations(multiLanguage.getId())
                    .orElse(null);
        }
        multiLanguage.getTexts().add(text);

        neo4jTemplate.saveAs(multiLanguage, TextsDtoProjection.class);

        return item;
    }

    @Transactional
    @Override
    public XtdObject addName(AddNameInput input) {
        final XtdObject item = getRepository().findByIdWithDirectRelations(input.getCatalogEntryId()).orElseThrow(() -> new IllegalArgumentException("No record with id " + input.getCatalogEntryId() + " found."));
        TranslationInput translation = input.getName();

        XtdText text = createText(translation);

        XtdMultiLanguageText multiLanguage = item.getNames().stream().findFirst().orElse(null);
        if (multiLanguage == null) {
            multiLanguage = new XtdMultiLanguageText();
            multiLanguage = multiLanguageTextRepository.save(multiLanguage);
            item.getNames().add(multiLanguage);
            neo4jTemplate.saveAs(item, NamesDtoProjection.class);
        } else {
            multiLanguage = multiLanguageTextRecordService.findByIdWithDirectRelations(multiLanguage.getId())
                    .orElse(null);
        }
        multiLanguage.getTexts().add(text);

        neo4jTemplate.saveAs(multiLanguage, TextsDtoProjection.class);

        return item;
    }

    @Transactional
    public XtdText createText(TranslationInput translation) {

        final XtdLanguage language = languageRecordService.findByCode(translation.getLanguageTag()).orElseThrow(() -> new IllegalArgumentException("No record with id " + translation.getLanguageTag() + " found."));

        XtdText text = new XtdText();
        text.setText(translation.getValue());
        text.setId(translation.getId());
        text.setLanguage(language);

        text = textRepository.save(text);
        return text;
    }

    @Transactional
    @Override
    public @NotNull XtdObject updateStatus(String id, XtdStatusOfActivationEnum status) {
        final XtdObject item = getRepository().findByIdWithDirectRelations(id).orElseThrow(() -> new IllegalArgumentException("No record with id " + id + " found."));
        item.setStatus(status);
        neo4jTemplate.saveAs(item, ObjectDtoProjection.class);
        return item;
    }

    @Transactional
    @Override
    public @NotNull XtdObject updateMajorVersion(String id, Integer majorVersion) {
        final XtdObject item = getRepository().findByIdWithDirectRelations(id).orElseThrow(() -> new IllegalArgumentException("No record with id " + id + " found."));
        item.setMajorVersion(majorVersion);
        neo4jTemplate.saveAs(item, ObjectDtoProjection.class);
        return item;
    }

    @Transactional
    @Override
    public @NotNull XtdObject updateMinorVersion(String id, Integer minorVersion) {
        final XtdObject item = getRepository().findByIdWithDirectRelations(id).orElseThrow(() -> new IllegalArgumentException("No record with id " + id + " found."));
        item.setMinorVersion(minorVersion);
        neo4jTemplate.saveAs(item, ObjectDtoProjection.class);
        return item;
    }
}
