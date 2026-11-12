package de.bentrm.datacat.catalog.service.impl;

import de.bentrm.datacat.catalog.domain.CatalogRecordType;
import de.bentrm.datacat.catalog.domain.SimpleRelationType;
import de.bentrm.datacat.catalog.domain.XtdInterval;
import de.bentrm.datacat.catalog.domain.XtdValueList;
import de.bentrm.datacat.catalog.repository.IntervalRepository;
import de.bentrm.datacat.catalog.repository.RecordRepositories;
import de.bentrm.datacat.catalog.repository.ValueListRepository;
import de.bentrm.datacat.catalog.service.CatalogCleanupService;
import de.bentrm.datacat.catalog.service.IntervalRecordService;
import de.bentrm.datacat.catalog.service.ValueListRecordService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Slf4j
@Service
@Validated
@Transactional(readOnly = true)
public class IntervalRecordServiceImpl
        extends AbstractSimpleRecordServiceImpl<XtdInterval, IntervalRepository>
        implements IntervalRecordService {

            private final ValueListRecordService valueListRecordService;

    private final ValueListRepository valueListRepository;        

    public IntervalRecordServiceImpl(Neo4jTemplate neo4jTemplate,
                                     IntervalRepository repository,
                                        ValueListRecordService valueListRecordService,
                                        ValueListRepository valueListRepository,
                                     CatalogCleanupService cleanupService,
                                     RecordRepositories repos) {
        super(XtdInterval.class, neo4jTemplate, repository, cleanupService, repos);
        this.valueListRecordService = valueListRecordService;
        this.valueListRepository = valueListRepository;
    }

    @Override
    public @NotNull Page<XtdInterval> findAll(@NotNull de.bentrm.datacat.base.specification.QuerySpecification specification) {
        // Use optimized query when no complex filters are applied
        if (isSimpleQuery(specification)) {
            List<XtdInterval> intervals = findIntervalsWithRelations(specification);
            Pageable pageable = specification.getPageable().orElse(PageRequest.of(0, 20));
            return PageableExecutionUtils.getPage(intervals, pageable, 
                () -> getRepository().count());
        }
        // Fallback to default implementation for complex queries
        return super.findAll(specification);
    }

    private boolean isSimpleQuery(de.bentrm.datacat.base.specification.QuerySpecification specification) {
        // Consider it simple if there are no filters, only pagination and sorting
        return specification.getFilters() == null || specification.getFilters().isEmpty();
    }

    private List<XtdInterval> findIntervalsWithRelations(de.bentrm.datacat.base.specification.QuerySpecification specification) {
        Pageable pageable = specification.getPageable().orElse(PageRequest.of(0, 20));
        String query = buildOptimizedIntervalQuery(pageable);
        return getNeo4jTemplate().findAll(query, XtdInterval.class);
    }

    private String buildOptimizedIntervalQuery(Pageable pageable) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("MATCH (i:XtdInterval) ");
        
        // Add optional matches for commonly used relations
        queryBuilder.append("OPTIONAL MATCH (i)<-[:INTERVALS]-(properties:XtdProperty) ");
        queryBuilder.append("OPTIONAL MATCH (i)-[:MINIMUM]->(minimum:XtdValueList) ");
        queryBuilder.append("OPTIONAL MATCH (i)-[:MAXIMUM]->(maximum:XtdValueList) ");
        queryBuilder.append("OPTIONAL MATCH (i)-[:NAMES]->(names:XtdMultiLanguageText) ");
        queryBuilder.append("OPTIONAL MATCH (i)-[:COMMENTS]->(comments:XtdMultiLanguageText) ");
        
        queryBuilder.append("RETURN i, ");
        queryBuilder.append("collect(DISTINCT properties) as properties, ");
        queryBuilder.append("collect(DISTINCT minimum) as minimum, ");
        queryBuilder.append("collect(DISTINCT maximum) as maximum, ");
        queryBuilder.append("collect(DISTINCT names) as names, ");
        queryBuilder.append("collect(DISTINCT comments) as comments ");
        
        // Add sorting if specified
        if (pageable.getSort().isSorted()) {
            queryBuilder.append("ORDER BY ");
            String sortClause = pageable.getSort().stream()
                    .map(order -> "i." + order.getProperty() + " " + order.getDirection())
                    .collect(Collectors.joining(", "));
            queryBuilder.append(sortClause).append(" ");
        }
        
        // Add pagination
        queryBuilder.append("SKIP ").append(pageable.getOffset()).append(" ");
        queryBuilder.append("LIMIT ").append(pageable.getPageSize());
        
        return queryBuilder.toString();
    }

    @Override
    public @NotNull CatalogRecordType getSupportedCatalogRecordType() {
        return CatalogRecordType.Interval;
    }

    @Override
    public Optional<XtdValueList> getMinimum(XtdInterval interval) {
        Assert.notNull(interval.getId(), "Interval must be persistent.");
        final String valueListId = getRepository().findMinValueListIdAssignedToInterval(interval.getId());
        if (valueListId == null) {
            return null;
        }
        return valueListRecordService.findByIdWithDirectRelations(valueListId);
    }

    @Override
    public Optional<XtdValueList> getMaximum(XtdInterval interval) {
        Assert.notNull(interval.getId(), "Interval must be persistent.");
        final String valueListId = getRepository().findMaxValueListIdAssignedToInterval(interval.getId());
        if (valueListId == null) {
            return null;
        }
        return valueListRecordService.findByIdWithDirectRelations(valueListId);
    }

    @Transactional
    @Override
    public @NotNull XtdInterval setRelatedRecords(@NotBlank String recordId,
                                                    @NotEmpty List<@NotBlank String> relatedRecordIds, @NotNull SimpleRelationType relationType) {
        
        if (!getRepository().existsById(recordId)) {
            throw new IllegalArgumentException("No record with id " + recordId + " found.");
        }

        switch (relationType) {
            case Minimum -> {
                if (getRepository().hasMinimumAssigned(recordId)) {
                    throw new IllegalArgumentException("Minimum already set.");
                }
                if (relatedRecordIds.size() > 1) {
                    throw new IllegalArgumentException("Maximum one value list must be provided.");
                }
                String minValueListId = relatedRecordIds.get(0);
                if (!valueListRepository.existsById(minValueListId)) {
                    throw new IllegalArgumentException("No value list with id " + minValueListId + " found.");
                }
                getRepository().attachMinValueList(recordId, minValueListId);
            }
            case Maximum -> {
                if (getRepository().hasMaximumAssigned(recordId)) {
                    throw new IllegalArgumentException("Maximum already set.");
                }
                if (relatedRecordIds.size() > 1) {
                    throw new IllegalArgumentException("Maximum one value list must be provided.");
                }
                String maxValueListId = relatedRecordIds.get(0);
                if (!valueListRepository.existsById(maxValueListId)) {
                    throw new IllegalArgumentException("No value list with id " + maxValueListId + " found.");
                }
                getRepository().attachMaxValueList(recordId, maxValueListId);
            }
            default -> log.error("Unsupported relation type: {}", relationType);
        }
        
        XtdInterval interval = new XtdInterval();
        interval.setId(recordId);
        log.trace("Updated relationship {} from: {}", relationType, recordId);
        return interval;
    }
}
