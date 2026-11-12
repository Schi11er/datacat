package de.bentrm.datacat.catalog.service.impl;

import de.bentrm.datacat.catalog.domain.CatalogRecordType;
import de.bentrm.datacat.catalog.domain.SimpleRelationType;
import de.bentrm.datacat.catalog.domain.XtdSymbol;
import de.bentrm.datacat.catalog.domain.XtdText;
import de.bentrm.datacat.catalog.domain.XtdSubject;
import de.bentrm.datacat.catalog.repository.RecordRepositories;
import de.bentrm.datacat.catalog.repository.SubjectRepository;
import de.bentrm.datacat.catalog.repository.SymbolRepository;
import de.bentrm.datacat.catalog.service.CatalogCleanupService;
import de.bentrm.datacat.catalog.service.SubjectRecordService;
import de.bentrm.datacat.catalog.service.SymbolRecordService;
import de.bentrm.datacat.catalog.service.TextRecordService;
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
public class SymbolRecordServiceImpl
        extends AbstractSimpleRecordServiceImpl<XtdSymbol, SymbolRepository>
        implements SymbolRecordService {

            private final SubjectRecordService subjectRecordService;

            private final TextRecordService textRecordService;

            private final SubjectRepository subjectRepository;

    public SymbolRecordServiceImpl(Neo4jTemplate neo4jTemplate,
                                     SymbolRepository repository,
                                        SubjectRecordService subjectRecordService,
                                        TextRecordService textRecordService,
                                        SubjectRepository subjectRepository,
                                     CatalogCleanupService cleanupService,
                                     RecordRepositories repos) {
        super(XtdSymbol.class, neo4jTemplate, repository, cleanupService, repos);
        this.subjectRecordService = subjectRecordService;
        this.textRecordService = textRecordService;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public @NotNull Page<XtdSymbol> findAll(@NotNull de.bentrm.datacat.base.specification.QuerySpecification specification) {
        // Use optimized query when no complex filters are applied
        if (isSimpleQuery(specification)) {
            List<XtdSymbol> symbols = findSymbolsWithRelations(specification);
            Pageable pageable = specification.getPageable().orElse(PageRequest.of(0, 20));
            return PageableExecutionUtils.getPage(symbols, pageable, 
                () -> getRepository().count());
        }
        // Fallback to default implementation for complex queries
        return super.findAll(specification);
    }

    private boolean isSimpleQuery(de.bentrm.datacat.base.specification.QuerySpecification specification) {
        // Consider it simple if there are no filters, only pagination and sorting
        return specification.getFilters() == null || specification.getFilters().isEmpty();
    }

    private List<XtdSymbol> findSymbolsWithRelations(de.bentrm.datacat.base.specification.QuerySpecification specification) {
        Pageable pageable = specification.getPageable().orElse(PageRequest.of(0, 20));
        String query = buildOptimizedSymbolQuery(pageable);
        return getNeo4jTemplate().findAll(query, XtdSymbol.class);
    }

    private String buildOptimizedSymbolQuery(Pageable pageable) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("MATCH (s:XtdSymbol) ");
        
        // Add optional matches for commonly used relations
        queryBuilder.append("OPTIONAL MATCH (s)<-[:SYMBOLS]-(properties:XtdProperty) ");
        queryBuilder.append("OPTIONAL MATCH (s)-[:NAMES]->(names:XtdMultiLanguageText) ");
        queryBuilder.append("OPTIONAL MATCH (s)-[:COMMENTS]->(comments:XtdMultiLanguageText) ");
        
        queryBuilder.append("RETURN s, ");
        queryBuilder.append("collect(DISTINCT properties) as properties, ");
        queryBuilder.append("collect(DISTINCT names) as names, ");
        queryBuilder.append("collect(DISTINCT comments) as comments ");
        
        // Add sorting if specified
        if (pageable.getSort().isSorted()) {
            queryBuilder.append("ORDER BY ");
            String sortClause = pageable.getSort().stream()
                    .map(order -> "s." + order.getProperty() + " " + order.getDirection())
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
        return CatalogRecordType.Symbol;
    }

    @Override
    public Optional<XtdSubject> getSubject(XtdSymbol symbol) {
        Assert.notNull(symbol.getId(), "Symbol must be persistent.");
        final String subjectId = getRepository().findSubjectIdAssignedToSymbol(symbol.getId());
        if (subjectId == null) {
            return null;
        }
        final Optional<XtdSubject> subject = subjectRecordService.findByIdWithDirectRelations(subjectId);
        // final Optional<XtdSubject> subject = getRepository().findSubjectAssignedToSymbol(symbol.getId());
        return subject;
    }

    @Override
    public Optional<XtdText> getSymbolText(XtdSymbol symbol) {
        Assert.notNull(symbol.getId(), "Symbol must be persistent.");
        final String textId = getRepository().findSymbolText(symbol.getId());
        if (textId == null) {
            return null;
        }
        final Optional<XtdText> text = textRecordService.findByIdWithDirectRelations(textId);
        return text;
    }

    @Transactional
    @Override
    public @NotNull XtdSymbol setRelatedRecords(@NotBlank String recordId,
                                                    @NotEmpty List<@NotBlank String> relatedRecordIds, @NotNull SimpleRelationType relationType) {

        if (!getRepository().existsById(recordId)) {
            throw new IllegalArgumentException("No record with id " + recordId + " found.");
        }

        switch (relationType) {
            case Subject -> {
                if (getRepository().hasSubject(recordId)) {
                    throw new IllegalArgumentException("Symbol already has a subject assigned.");
                } 
                if (relatedRecordIds.size() != 1) {
                    throw new IllegalArgumentException("Exactly one subject must be assigned.");
                } 
                String subjectId = relatedRecordIds.get(0);
                if (!subjectRepository.existsById(subjectId)) {
                    throw new IllegalArgumentException("No subject with id " + subjectId + " found.");
                }
                getRepository().attachSubject(recordId, subjectId);
            }
            default -> log.error("Unsupported relation type: {}", relationType);
        }

        XtdSymbol symbol = new XtdSymbol();
        symbol.setId(recordId);
        log.trace("Updated relationship {} from: {}", relationType, recordId);
        return symbol;
    }
}
