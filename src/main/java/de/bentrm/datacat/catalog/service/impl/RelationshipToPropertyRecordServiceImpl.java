package de.bentrm.datacat.catalog.service.impl;

import de.bentrm.datacat.catalog.domain.CatalogRecordType;
import de.bentrm.datacat.catalog.domain.SimpleRelationType;
import de.bentrm.datacat.catalog.domain.XtdRelationshipToProperty;
import de.bentrm.datacat.catalog.domain.Enums.XtdPropertyRelationshipTypeEnum;
import de.bentrm.datacat.catalog.domain.XtdProperty;
import de.bentrm.datacat.catalog.repository.RecordRepositories;
import de.bentrm.datacat.catalog.repository.RelationshipToPropertyRepository;
import de.bentrm.datacat.catalog.service.CatalogCleanupService;
import de.bentrm.datacat.catalog.service.RelationshipToPropertyRecordService;
import de.bentrm.datacat.catalog.service.dto.RelationshipToPropertyDtoProjection;
import de.bentrm.datacat.catalog.service.dto.Relationships.ConnectingPropertyDtoProjection;
import de.bentrm.datacat.catalog.service.PropertyRecordService;
import de.bentrm.datacat.catalog.service.ObjectRecordService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Slf4j
@Service
@Validated
@Transactional(readOnly = true)
public class RelationshipToPropertyRecordServiceImpl
        extends AbstractSimpleRecordServiceImpl<XtdRelationshipToProperty, RelationshipToPropertyRepository>
        implements RelationshipToPropertyRecordService {

private final PropertyRecordService propertyRecordService;

private final ObjectRecordService objectRecordService;

    public RelationshipToPropertyRecordServiceImpl(Neo4jTemplate neo4jTemplate,
                                     RelationshipToPropertyRepository repository,
                                        @Lazy PropertyRecordService propertyRecordService,
                                        ObjectRecordService objectRecordService,
                                     CatalogCleanupService cleanupService,
                                     RecordRepositories repos) {
        super(XtdRelationshipToProperty.class, neo4jTemplate, repository, cleanupService, repos);
        this.propertyRecordService = propertyRecordService;
        this.objectRecordService = objectRecordService;
    }

    @Transactional
    @Override
    public XtdRelationshipToProperty addRecord(@NotBlank String id,
            @NotBlank String relatingRecordId,
            @NotEmpty List<@NotBlank String> relatedRecordIds) {
        try {
            XtdRelationshipToProperty newRecord = this.getDomainClass().getDeclaredConstructor().newInstance();

            newRecord.setId(id);
            log.trace("Persisting new relationship record...");
            newRecord = this.getRepository().save(newRecord);

            log.trace("Setting relating record with id: {}", relatingRecordId);
            newRecord = this.setRelatingRecord(newRecord, relatingRecordId);
            log.trace("Set relating record with id: {}", relatingRecordId);

            log.trace("Setting related records with ids: {}", relatedRecordIds);
            for (String relatedRecordId : relatedRecordIds) {    
                getRepository().attachTargetProperty(id, relatedRecordId);
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
        return CatalogRecordType.RelationshipToProperty;
    }

    @Override
    public @NotNull XtdProperty getConnectingProperty(@NotNull XtdRelationshipToProperty relationshipToProperty) {
        Assert.notNull(relationshipToProperty, "RelationshipToProperty must not be null");
        final String propertyId = getRepository().findConnectingPropertyId(relationshipToProperty.getId());
        return propertyRecordService.findByIdWithDirectRelations(propertyId).orElseThrow(() -> new IllegalArgumentException("No record with id " + propertyId + " found."));
    }

    @Override
    public @NotNull List<XtdProperty> getTargetProperties(@NotNull XtdRelationshipToProperty relationshipToProperty) {
        Assert.notNull(relationshipToProperty, "RelationshipToProperty must not be null");
        final List<String> propertyIds = getRepository().findTargetPropertyIds(relationshipToProperty.getId());
        final Iterable<XtdProperty> properties = propertyRecordService.findAllEntitiesById(propertyIds);

        return StreamSupport
                .stream(properties.spliterator(), false)
                .collect(Collectors.toList());
    }

    @Transactional
    protected XtdRelationshipToProperty setRelatingRecord(@NotNull XtdRelationshipToProperty relationshipRecord,
                                     @NotBlank String relatingRecordId) {
        final XtdProperty relatingCatalogRecord = propertyRecordService
                .findByIdWithDirectRelations(relatingRecordId)
                .orElseThrow(() -> new IllegalArgumentException("No record with id " + relatingRecordId + " found."));
        relationshipRecord.setConnectingProperty(relatingCatalogRecord);
        neo4jTemplate.saveAs(relationshipRecord, ConnectingPropertyDtoProjection.class);
        return relationshipRecord;
    }

    @Transactional
    @Override
    public @NotNull XtdRelationshipToProperty setRelatedRecords(@NotBlank String recordId,
                                                    @NotEmpty List<@NotBlank String> relatedRecordIds, @NotNull SimpleRelationType relationType) {

        if (!getRepository().existsById(recordId)) {
            throw new IllegalArgumentException("No record with id " + recordId + " found.");
        }

        switch (relationType) {

            case TargetProperties -> {
                for (String propertyId : relatedRecordIds) {
                    getRepository().attachTargetProperty(recordId, propertyId);
                }
            }
            default -> objectRecordService.setRelatedRecords(recordId, relatedRecordIds, relationType);
        }

        XtdRelationshipToProperty relationship = new XtdRelationshipToProperty();
        relationship.setId(recordId);
        log.trace("Updated relationship {} from: {}", relationType, recordId);
        return relationship;
    }  


    @Transactional
    @Override
    public @NotNull XtdRelationshipToProperty addRelationshipType(@NotNull XtdRelationshipToProperty relationship, @NotNull XtdPropertyRelationshipTypeEnum relationshipType) {
        relationship.setRelationshipType(relationshipType);
        neo4jTemplate.saveAs(relationship, RelationshipToPropertyDtoProjection.class);
        return relationship;
    }
}
