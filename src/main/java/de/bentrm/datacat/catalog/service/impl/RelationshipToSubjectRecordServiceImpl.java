package de.bentrm.datacat.catalog.service.impl;

import de.bentrm.datacat.catalog.domain.CatalogRecordType;
import de.bentrm.datacat.catalog.domain.SimpleRelationType;
import de.bentrm.datacat.catalog.domain.XtdRelationshipToSubject;
import de.bentrm.datacat.catalog.domain.XtdRelationshipType;
import de.bentrm.datacat.catalog.domain.XtdSubject;
import de.bentrm.datacat.catalog.domain.Enums.XtdRelationshipKindEnum;
import de.bentrm.datacat.catalog.repository.RelationshipToSubjectRepository;
import de.bentrm.datacat.catalog.repository.RelationshipTypeRepository;
import de.bentrm.datacat.catalog.service.CatalogCleanupService;
import de.bentrm.datacat.catalog.service.RelationshipToSubjectRecordService;
import de.bentrm.datacat.catalog.service.RelationshipTypeRecordService;
import de.bentrm.datacat.catalog.service.SubjectRecordService;
import de.bentrm.datacat.catalog.service.dto.Relationships.ConnectingSubjectDtoProjection;
import de.bentrm.datacat.catalog.service.dto.Relationships.RelationshipTypeDtoProjection;
import de.bentrm.datacat.catalog.service.dto.Relationships.ScopeSubjectsDtoProjection;
import de.bentrm.datacat.catalog.service.dto.Relationships.TargetSubjectsDtoProjection;
import de.bentrm.datacat.catalog.service.ObjectRecordService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.neo4j.core.Neo4jTemplate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Slf4j
@Service
@Validated
@Transactional(readOnly = true)
public class RelationshipToSubjectRecordServiceImpl
        extends AbstractSimpleRecordServiceImpl<XtdRelationshipToSubject, RelationshipToSubjectRepository>
        implements RelationshipToSubjectRecordService {

            @Autowired
            @Lazy
            private SubjectRecordService subjectRecordService;

            @Autowired
            private ObjectRecordService objectRecordService;

            @Autowired
            private RelationshipTypeRecordService relationshipTypeRecordService;

            @Autowired
            private RelationshipTypeRepository relationshipTypeRepository;

            @Autowired
            private de.bentrm.datacat.catalog.repository.LanguageRepository languageRepository;

            @Autowired
            private de.bentrm.datacat.catalog.repository.TextRepository textRepository;

            @Autowired
            private de.bentrm.datacat.catalog.repository.MultiLanguageTextRepository multiLanguageTextRepository;

    public RelationshipToSubjectRecordServiceImpl(Neo4jTemplate neo4jTemplate,
                                     RelationshipToSubjectRepository repository,
                                     CatalogCleanupService cleanupService) {
        super(XtdRelationshipToSubject.class, neo4jTemplate, repository, cleanupService);
    }

    @Transactional
    @Override
    public XtdRelationshipToSubject addRecord(@NotBlank String id,
            @NotBlank String relatingRecordId,
            @NotEmpty List<@NotBlank String> relatedRecordIds) {
        try {
            XtdRelationshipToSubject newRecord = this.getDomainClass().getDeclaredConstructor().newInstance();

            newRecord.setId(id);
            log.trace("Persisting new relationship record...");
            newRecord = this.getRepository().save(newRecord);

            log.trace("Setting relating record with id: {}", relatingRecordId);
            newRecord = this.setRelatingRecord(newRecord, relatingRecordId);
            log.trace("Set relating record with id: {}", relatingRecordId);

            log.trace("Setting related records with ids: {}", relatedRecordIds);
            for (String relatedRecordId : relatedRecordIds) {    
                getRepository().attachTargetSubject(id, relatedRecordId);
            }
            log.trace("Set related records with ids: {}", relatedRecordIds);

            log.trace("Persisted new relationship record with id: {}", newRecord.getId());

            return newRecord;
        } catch (ReflectiveOperationException e) {
            log.warn("Can not instantiate catalog records of type: {}", this.getDomainClass());
            throw new IllegalArgumentException("unsupported record type");
        }
    }

    @Override
    public @NotNull CatalogRecordType getSupportedCatalogRecordType() {
        return CatalogRecordType.RelationshipToSubject;
    }

    @Override
    public @NotNull List<XtdSubject> getScopeSubjects(XtdRelationshipToSubject relationshipToSubject) {
        Assert.notNull(relationshipToSubject.getId(), "RelationshipToSubject must be persistent.");
        final List<String> subjectIds = getRepository().findAllScopeSubjectIdsAssignedToRelationshipToSubject(relationshipToSubject.getId());
        final Iterable<XtdSubject> subjects = subjectRecordService.findAllEntitiesById(subjectIds);

        return StreamSupport
                .stream(subjects.spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull List<XtdSubject> getTargetSubjects(XtdRelationshipToSubject relationshipToSubject) {
        Assert.notNull(relationshipToSubject.getId(), "RelationshipToSubject must be persistent.");
        final List<String> subjectIds = getRepository().findAllTargetSubjectIdsAssignedToRelationshipToSubject(relationshipToSubject.getId());
        final Iterable<XtdSubject> subjects = subjectRecordService.findAllEntitiesById(subjectIds);

        return StreamSupport
                .stream(subjects.spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull XtdSubject getConnectingSubject(XtdRelationshipToSubject relationshipToSubject) {
        Assert.notNull(relationshipToSubject.getId(), "RelationshipToSubject must be persistent.");
        final String subjectId = getRepository().findConnectingSubjectIdAssignedToRelationshipToSubject(relationshipToSubject.getId());
        if (subjectId == null) {
            throw new IllegalArgumentException("No connecting subject found for RelationshipToSubject with id " + relationshipToSubject.getId());
        }
        return subjectRecordService.findById(subjectId).orElseThrow(() -> new IllegalArgumentException("No record with id " + subjectId + " found."));
    }

    @Override
    public XtdRelationshipType getRelationshipType(XtdRelationshipToSubject relationshipToSubject) {
        Assert.notNull(relationshipToSubject.getId(), "RelationshipToSubject must be persistent.");
        final String relationshipTypeId = getRepository().findRelationshipTypeIdAssignedToRelationshipToSubject(relationshipToSubject.getId());
        if (relationshipTypeId == null) {
            return null;
        }
        return relationshipTypeRecordService.findByIdWithDirectRelations(relationshipTypeId).orElseThrow(() -> new IllegalArgumentException("No record with id " + relationshipTypeId + " found."));
    }

    @Transactional
    @Override
    public @NotNull XtdRelationshipToSubject addRelationshipType(@NotNull XtdRelationshipToSubject relationshipToSubject,
                                                    @NotNull XtdRelationshipKindEnum relationshipKind) {
        Assert.notNull(relationshipToSubject.getId(), "RelationshipToSubject must be persistent.");
        Assert.notNull(relationshipKind, "RelationshipType must be a valid input.");
        XtdRelationshipType relationshipType = new XtdRelationshipType();
        relationshipType.setKind(relationshipKind);
        relationshipType = relationshipTypeRepository.save(relationshipType);
        relationshipToSubject.setRelationshipType(relationshipType);
        neo4jTemplate.saveAs(relationshipToSubject, RelationshipTypeDtoProjection.class);
        return relationshipToSubject;
    }

    @Transactional
    @Override
    public @NotNull XtdRelationshipToSubject addRelationshipTypeByName(@NotNull XtdRelationshipToSubject relationshipToSubject,
                                                    @NotNull String name,
                                                    @NotNull XtdRelationshipKindEnum relationshipKind) {
        Assert.notNull(relationshipToSubject.getId(), "RelationshipToSubject must be persistent.");
        Assert.notNull(name, "Name must not be null.");
        Assert.notNull(relationshipKind, "RelationshipKind must be a valid input.");

        // Suche nach bestehendem XtdRelationshipType mit exakt diesem Namen und Kind
        // XtdRelationshipType existingType = findRelationshipTypeByNameAndKind(name, relationshipKind);
        XtdRelationshipType existingType = relationshipTypeRepository
            .findByNameAndKind(name, relationshipKind)
            .orElse(null);

        if (existingType != null) {
            // Bestehendes Objekt verwenden
            relationshipToSubject.setRelationshipType(existingType);
        } else {
            // Neues XtdRelationshipType erstellen
            XtdRelationshipType relationshipType = new XtdRelationshipType();
            relationshipType.setKind(relationshipKind);
            relationshipType = relationshipTypeRepository.save(relationshipType);

            // Namen als XtdMultiLanguageText mit deutscher Sprache hinzufügen
            de.bentrm.datacat.catalog.domain.XtdLanguage germanLanguage = languageRepository.findByCode("de")
                    .orElseThrow(() -> new IllegalArgumentException("German language not found in database."));

            de.bentrm.datacat.catalog.domain.XtdText text = new de.bentrm.datacat.catalog.domain.XtdText();
            text.setText(name);
            text.setLanguage(germanLanguage);
            text = textRepository.save(text);

            de.bentrm.datacat.catalog.domain.XtdMultiLanguageText multiLanguageText = new de.bentrm.datacat.catalog.domain.XtdMultiLanguageText();
            multiLanguageText.getTexts().add(text);
            multiLanguageText = multiLanguageTextRepository.save(multiLanguageText);

            relationshipType.getNames().add(multiLanguageText);
            relationshipType = relationshipTypeRepository.save(relationshipType);

            relationshipToSubject.setRelationshipType(relationshipType);
        }

        neo4jTemplate.saveAs(relationshipToSubject, RelationshipTypeDtoProjection.class);
        return relationshipToSubject;
    }

    @Transactional
    protected XtdRelationshipToSubject setRelatingRecord(@NotNull XtdRelationshipToSubject relationshipRecord,
                                     @NotBlank String relatingRecordId) {
        XtdSubject relatingCatalogRecord = new XtdSubject();
        relatingCatalogRecord.setId(relatingRecordId);

        relationshipRecord.setConnectingSubject(relatingCatalogRecord);
        neo4jTemplate.saveAs(relationshipRecord, ConnectingSubjectDtoProjection.class);
        return relationshipRecord;
    }

    @Transactional
    @Override
    public @NotNull XtdRelationshipToSubject setRelatedRecords(@NotBlank String recordId,
                                                    @NotEmpty List<@NotBlank String> relatedRecordIds, @NotNull SimpleRelationType relationType) {

        if (!getRepository().existsById(recordId)) {
            throw new IllegalArgumentException("No record with id " + recordId + " found.");
        }

        switch (relationType) {
            case ScopeSubjects -> {
                for (String subjectId : relatedRecordIds) {
                    getRepository().attachScopeSubject(recordId, subjectId);
                }
            }
            case TargetSubjects -> {
                for (String subjectId : relatedRecordIds) {
                    getRepository().attachTargetSubject(recordId, subjectId);
                }
            }
            default -> objectRecordService.setRelatedRecords(recordId, relatedRecordIds, relationType);
        }

        XtdRelationshipToSubject relationship = new XtdRelationshipToSubject();
        relationship.setId(recordId);
        log.trace("Updated relationship {} from: {}", relationType, recordId);
        return relationship;
    }

    @Override
    public Long countRelationshipsUsingRelationshipType(@NotBlank String relationshipTypeId) {
        return getRepository().countRelationshipsUsingRelationshipType(relationshipTypeId);
    }
}
