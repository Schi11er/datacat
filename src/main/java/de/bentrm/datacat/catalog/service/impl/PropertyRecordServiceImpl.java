package de.bentrm.datacat.catalog.service.impl;

import de.bentrm.datacat.base.specification.QuerySpecification;
import de.bentrm.datacat.catalog.domain.CatalogRecordType;
import de.bentrm.datacat.catalog.domain.SimpleRelationType;
import de.bentrm.datacat.catalog.domain.XtdProperty;
import de.bentrm.datacat.catalog.domain.XtdRelationshipToProperty;
import de.bentrm.datacat.catalog.domain.XtdSubject;
import de.bentrm.datacat.catalog.domain.XtdUnit;
import de.bentrm.datacat.catalog.domain.XtdValueList;
import de.bentrm.datacat.catalog.domain.Enums.XtdDataTypeEnum;
import de.bentrm.datacat.catalog.domain.XtdDimension;
import de.bentrm.datacat.catalog.domain.XtdInterval;
import de.bentrm.datacat.catalog.domain.XtdSymbol;
import de.bentrm.datacat.catalog.domain.XtdQuantityKind;
import de.bentrm.datacat.catalog.repository.DimensionRepository;
import de.bentrm.datacat.catalog.repository.PropertyRepository;
import de.bentrm.datacat.catalog.repository.RecordRepositories;
import de.bentrm.datacat.catalog.service.CatalogCleanupService;
import de.bentrm.datacat.catalog.service.PropertyRecordService;
import de.bentrm.datacat.catalog.service.QuantityKindRecordService;
import de.bentrm.datacat.catalog.service.RelationshipToPropertyRecordService;
import de.bentrm.datacat.catalog.service.SubjectRecordService;
import de.bentrm.datacat.catalog.service.SymbolRecordService;
import de.bentrm.datacat.catalog.service.UnitRecordService;
import de.bentrm.datacat.catalog.service.ValueListRecordService;
import de.bentrm.datacat.catalog.service.dto.PropertyDtoProjection;
import de.bentrm.datacat.catalog.service.ConceptRecordService;
import de.bentrm.datacat.catalog.service.DimensionRecordService;
import de.bentrm.datacat.catalog.service.IntervalRecordService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@Validated
@Transactional(readOnly = true)
public class PropertyRecordServiceImpl extends AbstractSimpleRecordServiceImpl<XtdProperty, PropertyRepository>
                implements PropertyRecordService {

        private final SubjectRecordService subjectRecordService;

        private final ValueListRecordService valueListRecordService;

        private final UnitRecordService unitRecordService;

        private final RelationshipToPropertyRecordService relationshipToPropertyRecordService;

        private final DimensionRecordService dimensionRecordService;

        private final SymbolRecordService symbolRecordService;

        private final IntervalRecordService intervalRecordService;

        private final QuantityKindRecordService quantityKindRecordService;

        private final ConceptRecordService conceptRecordService;

        private final DimensionRepository dimensionRepository;

        public PropertyRecordServiceImpl(Neo4jTemplate neo4jTemplate, PropertyRepository repository,
                @Lazy SubjectRecordService subjectRecordService, @Lazy ValueListRecordService valueListRecordService,
                @Lazy UnitRecordService unitRecordService, RelationshipToPropertyRecordService relationshipToPropertyRecordService,
                DimensionRecordService dimensionRecordService, @Lazy SymbolRecordService symbolRecordService,
                @Lazy IntervalRecordService intervalRecordService, @Lazy QuantityKindRecordService quantityKindRecordService,
                ConceptRecordService conceptRecordService, DimensionRepository dimensionRepository,
                        CatalogCleanupService cleanupService, RecordRepositories repos) {
                super(XtdProperty.class, neo4jTemplate, repository, cleanupService, repos);
                this.subjectRecordService = subjectRecordService;
                this.valueListRecordService = valueListRecordService;
                this.unitRecordService = unitRecordService;
                this.relationshipToPropertyRecordService = relationshipToPropertyRecordService;
                this.dimensionRecordService = dimensionRecordService;
                this.symbolRecordService = symbolRecordService;
                this.intervalRecordService = intervalRecordService;
                this.quantityKindRecordService = quantityKindRecordService;
                this.conceptRecordService = conceptRecordService;
                this.dimensionRepository = dimensionRepository;
        }

        @Override
        public @NotNull CatalogRecordType getSupportedCatalogRecordType() {
                return CatalogRecordType.Property;
        }

        @Override
        public @NotNull Optional<XtdProperty> findById(@NotNull String id) {
                // Überschreibe die Standard-Methode, um KEINE Relationen zu laden
                return getRepository().findByIdWithoutRelations(id);
        }

        @Override
        public List<XtdSubject> getSubjects(XtdProperty property) {
                Assert.notNull(property.getId(), "Property must be persistent.");
                final List<String> subjectIds = getRepository().findAllSubjectIdsAssignedToProperty(property.getId());
                final Iterable<XtdSubject> subjects = subjectRecordService.findAllEntitiesById(subjectIds);

                return StreamSupport.stream(subjects.spliterator(), false).collect(Collectors.toList());
        }

        @Override
        public List<XtdValueList> getValueLists(XtdProperty property) {
                Assert.notNull(property.getId(), "Property must be persistent.");
                final List<String> valueListIds = getRepository()
                                .findAllValueListIdsAssignedToProperty(property.getId());
                final Iterable<XtdValueList> valueLists = valueListRecordService.findAllEntitiesById(valueListIds);
  
                return StreamSupport.stream(valueLists.spliterator(), false).collect(Collectors.toList());
        }

        @Override
        public List<XtdUnit> getUnits(XtdProperty property) {
                Assert.notNull(property.getId(), "Property must be persistent.");
                final List<String> unitIds = getRepository().findAllUnitIdsAssignedToProperty(property.getId());
                final Iterable<XtdUnit> units = unitRecordService.findAllEntitiesById(unitIds);

                return StreamSupport.stream(units.spliterator(), false).collect(Collectors.toList());
        }

        @Override
        public List<XtdRelationshipToProperty> getConnectedProperties(XtdProperty property) {
                Assert.notNull(property.getId(), "Property must be persistent.");
                final List<String> relationshipIds = getRepository()
                                .findAllConnectedPropertyRelationshipIdsAssignedToProperty(property.getId());
                final Iterable<XtdRelationshipToProperty> relations = relationshipToPropertyRecordService
                                .findAllEntitiesById(relationshipIds);

                return StreamSupport.stream(relations.spliterator(), false).collect(Collectors.toList());
        }

        @Override
        public List<XtdRelationshipToProperty> getConnectingProperties(XtdProperty property) {
                Assert.notNull(property.getId(), "Property must be persistent.");
                final List<String> relationshipIds = getRepository()
                                .findAllConnectingPropertyRelationshipIdsAssignedToProperty(property.getId());
                final Iterable<XtdRelationshipToProperty> relations = relationshipToPropertyRecordService
                                .findAllEntitiesById(relationshipIds);

                return StreamSupport.stream(relations.spliterator(), false).collect(Collectors.toList());
        }

        @Override
        public Optional<XtdDimension> getDimension(XtdProperty property) {
                Assert.notNull(property.getId(), "Property must be persistent.");
                final String dimensionId = getRepository().findDimensionIdAssignedToProperty(property.getId());
                if (dimensionId == null) {
                        return null;
                }
                final Optional<XtdDimension> dimension = dimensionRecordService.findById(dimensionId);

                return dimension;
        }

        @Override
        public List<XtdSymbol> getSymbols(XtdProperty property) {
                Assert.notNull(property.getId(), "Property must be persistent.");
                final List<String> symbolIds = getRepository().findAllSymbolIdsAssignedToProperty(property.getId());
                final Iterable<XtdSymbol> symbols = symbolRecordService.findAllEntitiesById(symbolIds);

                return StreamSupport.stream(symbols.spliterator(), false).collect(Collectors.toList());
        }

        @Override
        public List<XtdInterval> getIntervals(XtdProperty property) {
                Assert.notNull(property.getId(), "Property must be persistent.");
                final List<String> intervalIds = getRepository().findAllIntervalIdsAssignedToProperty(property.getId());
                final Iterable<XtdInterval> intervals = intervalRecordService.findAllEntitiesById(intervalIds);

                return StreamSupport.stream(intervals.spliterator(), false).collect(Collectors.toList());

        }

        @Override
        public List<XtdQuantityKind> getQuantityKinds(XtdProperty property) {
                Assert.notNull(property.getId(), "Property must be persistent.");
                final List<String> quantityKindIds = getRepository()
                                .findAllQuantityKindIdsAssignedToProperty(property.getId());
                final Iterable<XtdQuantityKind> quantityKinds = quantityKindRecordService
                                .findAllEntitiesById(quantityKindIds);

                return StreamSupport.stream(quantityKinds.spliterator(), false).collect(Collectors.toList());
        }

        @Transactional
        @Override
        public @NotNull XtdProperty updateDataType(@NotNull String id, @NotNull XtdDataTypeEnum dataType) {
                Assert.hasText(id, "Id must not be empty.");
                Assert.notNull(dataType, "Data type must not be null.");

                final XtdProperty property = getRepository().findByIdWithDirectRelations(id)
                                .orElseThrow(() -> new IllegalArgumentException("No record with id " + id + " found."));

                property.setDataType(dataType);
                neo4jTemplate.saveAs(property, PropertyDtoProjection.class);
                return property;
        }

        @Override
        public Optional<XtdProperty> findByIdWithIncomingAndOutgoingRelations(String id) {
                return getRepository().findByIdWithIncomingAndOutgoingRelations(id);
        }

        @Override
        public @NotNull Page<XtdProperty> findAll(@NotNull QuerySpecification specification) {
                // Verwende eine optimierte Query für Properties, die wichtige Relationen
                // vorlädt
                Collection<XtdProperty> properties;
                Pageable pageable;
                final Long count = count(specification);

                final Optional<Pageable> paged = specification.getPageable();
                if (paged.isPresent()) {
                        pageable = paged.get();
                        // Verwende optimierte Cypher-Query statt der Standard-Query
                        properties = findPropertiesWithRelations(specification, pageable);
                } else {
                        pageable = PageRequest.of(0, (int) Math.max(count, 10));
                        properties = findPropertiesWithRelations(specification, pageable);
                }

                return PageableExecutionUtils.getPage(List.copyOf(properties), pageable, () -> count);
        }

        private Collection<XtdProperty> findPropertiesWithRelations(QuerySpecification specification,
                        Pageable pageable) {
                String query = buildOptimizedPropertyQuery(specification, pageable);
                return neo4jTemplate.findAll(query, XtdProperty.class);
        }

        private String buildOptimizedPropertyQuery(QuerySpecification specification, Pageable pageable) {
                String sort = "";
                if (pageable.getSort().isSorted()) {
                        final Sort.Direction direction = pageable.getSort().get().findFirst()
                                        .map(Sort.Order::getDirection).orElse(Sort.Direction.ASC);
                        final String[] properties = pageable.getSort().get().map(Sort.Order::getProperty)
                                        .toArray(String[]::new);
                        List<String> prefixedProperties = Arrays.stream(properties)
                                        .map(property -> "p.`" + property + "` " + direction.name())
                                        .collect(Collectors.toList());
                        sort = " ORDER BY " + String.join(", ", prefixedProperties);
                }

                String whereClause = "";
                if (!specification.getFilters().isEmpty()) {
                        whereClause = " WHERE " + String.join(" AND ", specification.getFilters().stream()
                                        .map(filter -> filter.replace("n.", "p.")).collect(Collectors.toList()));
                }

                return String.format("""
                                MATCH (p:XtdProperty)%s

                                // Wichtigste Relationen vorladen
                                OPTIONAL MATCH (p)-[:DIMENSION]->(dim:XtdDimension)
                                OPTIONAL MATCH (p)-[:POSSIBLE_VALUES]->(vl:XtdValueList)
                                OPTIONAL MATCH (p)-[:UNITS]->(unit:XtdUnit)
                                OPTIONAL MATCH (p)-[:SYMBOLS]->(sym:XtdSymbol)
                                OPTIONAL MATCH (p)-[:QUANTITY_KINDS]->(qk:XtdQuantityKind)
                                OPTIONAL MATCH (p)-[:BOUNDARY_VALUES]->(iv:XtdInterval)
                                OPTIONAL MATCH (p)-[:TAGGED]->(tag:Tag)

                                WITH p,
                                     collect(DISTINCT dim) as dimensions,
                                     collect(DISTINCT vl) as valueLists,
                                     collect(DISTINCT unit) as units,
                                     collect(DISTINCT sym) as symbols,
                                     collect(DISTINCT qk) as quantityKinds,
                                     collect(DISTINCT iv) as intervals,
                                     collect(DISTINCT tag) as tags%s

                                RETURN p, dimensions, valueLists, units, symbols, quantityKinds, intervals, tags
                                SKIP %d LIMIT %d
                                """, whereClause, sort, pageable.getOffset(), pageable.getPageSize());
        }

        @Transactional
        @Override
        public @NotNull XtdProperty setRelatedRecords(@NotBlank String recordId,
                        @NotEmpty List<@NotBlank String> relatedRecordIds, @NotNull SimpleRelationType relationType) {

                if (!getRepository().existsById(recordId)) {
                throw new IllegalArgumentException("No record with id " + recordId + " found.");
                }

                switch (relationType) {
                case Symbols -> {
                        for (String symbolId : relatedRecordIds) {
                                getRepository().attachSymbol(recordId, symbolId);
                        }
                }
                case Units -> {
                        for (String unitId : relatedRecordIds) {
                                getRepository().attachUnit(recordId, unitId);
                        }
                }
                case Dimension -> {
                        if (getRepository().hasDimensionAssigned(recordId)) {
                                throw new IllegalArgumentException("Property already has a dimension assigned.");
                        } 
                        if (relatedRecordIds.size() != 1) {
                                throw new IllegalArgumentException("Exactly one dimension must be assigned.");
                        } 
                        String dimensionId = relatedRecordIds.get(0);
                       if (!dimensionRepository.existsById(dimensionId)) {
                                throw new IllegalArgumentException("No dimension with id " + dimensionId + " found.");
                        }
                        getRepository().attachDimension(recordId, dimensionId);
                }
                case BoundaryValues -> {
                        for (String intervalId : relatedRecordIds) {
                                getRepository().attachBoundaryValue(recordId, intervalId);
                        }
                }
                case QuantityKinds -> {
                        for (String quantityKindId : relatedRecordIds) {
                                getRepository().attachQuantityKind(recordId, quantityKindId);
                        }
                }
                case PossibleValues -> {
                        for (String valueListId : relatedRecordIds) {
                                getRepository().attachValueList(recordId, valueListId);
                        }
                }
                default -> conceptRecordService.setRelatedRecords(recordId, relatedRecordIds, relationType);
                }

                XtdProperty property = new XtdProperty();
                property.setId(recordId);
                log.trace("Updated relationship {} from: {}", relationType, recordId);
                return property;
        }
}
