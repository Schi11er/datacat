package de.bentrm.datacat.catalog.service;

import de.bentrm.datacat.catalog.domain.*;
import de.bentrm.datacat.catalog.service.value.HierarchyValue;
import de.bentrm.datacat.catalog.specification.CatalogRecordSpecification;
import de.bentrm.datacat.graphql.dto.CatalogStatistics;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

public interface CatalogService {

    CatalogStatistics getStatistics();

    @PreAuthorize("hasRole('ADMIN')")
    @NotNull Tag createTag(String id, @NotBlank String name);

    @PreAuthorize("hasRole('ADMIN')")
    @NotNull Tag updateTag(@NotBlank String id, @NotBlank String name);

    @PreAuthorize("hasRole('ADMIN')")
    @NotNull Tag deleteTag(@NotBlank String id);

    @PreAuthorize("hasRole('USER')")
    @NotNull CatalogRecord addTag(@NotBlank String entryId, @NotBlank String tagId);

    @PreAuthorize("hasRole('USER')")
    @NotNull CatalogRecord removeTag(@NotBlank String entryId, @NotBlank String tagId);

    @PreAuthorize("hasRole('READONLY')")
    @NotNull List<CatalogRecord> getAllEntriesById(List<String> ids);

    @PreAuthorize("hasRole('READONLY')")
    @NotNull List<XtdRoot> getAllRootItemsById(List<String> ids);

    @PreAuthorize("hasRole('READONLY')")
    @NotNull List<XtdText> getAllTextsById(List<String> ids);

    @PreAuthorize("hasRole('READONLY')")
    @NotNull List<XtdObject> getAllObjectsById(List<String> ids);

    @PreAuthorize("hasRole('READONLY')")
    @NotNull List<XtdConcept> getAllConceptsById(List<String> ids);

    @PreAuthorize("hasRole('READONLY')")
    @NotNull List<XtdExternalDocument> getAllExternalDocumentsById(List<String> ids);

    @PreAuthorize("hasRole('READONLY')")
    @NotNull Optional<CatalogRecord> getEntryById(@NotBlank String id);

    @PreAuthorize("hasRole('READONLY')")
    @NotNull Optional<XtdObject> getObject(@NotNull String id);

    @PreAuthorize("hasRole('READONLY')")
    @NotNull String getRelationshipBetweenObjects(@NotBlank String fromId, @NotBlank String toId);

    @PreAuthorize("hasRole('READONLY')")
    Long countTargetRelationships(@NotBlank String objectId);

    @PreAuthorize("hasRole('READONLY')")
    @NotNull Page<XtdObject> findAllCatalogRecords(@NotNull CatalogRecordSpecification specification);

    @PreAuthorize("hasRole('READONLY')")
    Long countCatalogRecords(@NotNull CatalogRecordSpecification specification);

    @PreAuthorize("hasRole('READONLY')")
    HierarchyValue getHierarchy(@NotNull CatalogRecordSpecification rootNodeSpecification);

}
