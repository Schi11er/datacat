package de.bentrm.datacat.catalog.service.impl;

import de.bentrm.datacat.catalog.domain.CatalogRecordType;
import de.bentrm.datacat.catalog.domain.SimpleRelationType;
import de.bentrm.datacat.catalog.domain.XtdDimension;
import de.bentrm.datacat.catalog.domain.XtdRational;
import de.bentrm.datacat.catalog.domain.XtdSubject;
import de.bentrm.datacat.catalog.repository.DimensionRepository;
import de.bentrm.datacat.catalog.repository.RationalRepository;
import de.bentrm.datacat.catalog.service.CatalogCleanupService;
import de.bentrm.datacat.catalog.service.ConceptRecordService;
import de.bentrm.datacat.catalog.service.DimensionRecordService;
import lombok.extern.slf4j.Slf4j;

import org.neo4j.ogm.session.SessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Slf4j
@Service
@Validated
@Transactional(readOnly = true)
public class DimensionRecordServiceImpl
        extends AbstractSimpleRecordServiceImpl<XtdDimension, DimensionRepository>
        implements DimensionRecordService {

    private final RationalRepository rationalRepository;
    private final ConceptRecordService conceptRecordService;

    public DimensionRecordServiceImpl(SessionFactory sessionFactory,
            DimensionRepository repository,
            RationalRepository rationalRepository,
            ConceptRecordService conceptRecordService,
            CatalogCleanupService cleanupService) {
        super(XtdDimension.class, sessionFactory, repository, cleanupService);
        this.rationalRepository = rationalRepository;
        this.conceptRecordService = conceptRecordService;
    }

    @Override
    public @NotNull CatalogRecordType getSupportedCatalogRecordType() {
        return CatalogRecordType.Dimension;
    }

    @Transactional
    @Override
    public @NotNull XtdDimension setRelatedRecords(@NotBlank String recordId,
            @NotEmpty List<@NotBlank String> relatedRecordIds, @NotNull SimpleRelationType relationType) {

        final XtdDimension dimension = getRepository().findById(recordId, 0).orElseThrow();

        switch (relationType) {
            case ThermodynamicTemperatureExponent:
                if (dimension.getThermodynamicTemperatureExponent() != null) {
                    throw new IllegalArgumentException("ThermodynamicTemperatureExponent already set.");
                } else if (relatedRecordIds.size() != 1) {
                    throw new IllegalArgumentException(
                            "ThermodynamicTemperatureExponent requires exactly one related record.");
                } else {
                    final XtdRational rational = rationalRepository.findById(relatedRecordIds.get(0), 0).orElseThrow();
                    dimension.setThermodynamicTemperatureExponent(rational);
                }
                break;
            case AmountOfSubstanceExponent:
                if (dimension.getAmountOfSubstanceExponent() != null) {
                    throw new IllegalArgumentException("AmountOfSubstanceExponent already set.");
                } else if (relatedRecordIds.size() != 1) {
                    throw new IllegalArgumentException(
                            "AmountOfSubstanceExponent requires exactly one related record.");
                } else {
                    final XtdRational rational = rationalRepository.findById(relatedRecordIds.get(0), 0).orElseThrow();
                    dimension.setAmountOfSubstanceExponent(rational);
                }
                break;
            case LengthExponent:
                if (dimension.getLengthExponent() != null) {
                    throw new IllegalArgumentException("LengthExponent already set.");
                } else if (relatedRecordIds.size() != 1) {
                    throw new IllegalArgumentException("LengthExponent requires exactly one related record.");
                } else {
                    final XtdRational rational = rationalRepository.findById(relatedRecordIds.get(0), 0).orElseThrow();
                    dimension.setLengthExponent(rational);
                }
                break;
            case MassExponent:
                if (dimension.getMassExponent() != null) {
                    throw new IllegalArgumentException("MassExponent already set.");
                } else if (relatedRecordIds.size() != 1) {
                    throw new IllegalArgumentException("MassExponent requires exactly one related record.");
                } else {
                    final XtdRational rational = rationalRepository.findById(relatedRecordIds.get(0), 0).orElseThrow();
                    dimension.setMassExponent(rational);
                }
                break;
            case TimeExponent:
                if (dimension.getTimeExponent() != null) {
                    throw new IllegalArgumentException("TimeExponent already set.");
                } else if (relatedRecordIds.size() != 1) {
                    throw new IllegalArgumentException("TimeExponent requires exactly one related record.");
                } else {
                    final XtdRational rational = rationalRepository.findById(relatedRecordIds.get(0), 0).orElseThrow();
                    dimension.setTimeExponent(rational);
                }
                break;
            case ElectricCurrentExponent:
                if (dimension.getElectricCurrentExponent() != null) {
                    throw new IllegalArgumentException("ElectricCurrentExponent already set.");
                } else if (relatedRecordIds.size() != 1) {
                    throw new IllegalArgumentException("ElectricCurrentExponent requires exactly one related record.");
                } else {
                    final XtdRational rational = rationalRepository.findById(relatedRecordIds.get(0), 0).orElseThrow();
                    dimension.setElectricCurrentExponent(rational);
                }
                break;
            case LuminousIntensityExponent:
                if (dimension.getLuminousIntensityExponent() != null) {
                    throw new IllegalArgumentException("LuminousIntensityExponent already set.");
                } else if (relatedRecordIds.size() != 1) {
                    throw new IllegalArgumentException(
                            "LuminousIntensityExponent requires exactly one related record.");
                } else {
                    final XtdRational rational = rationalRepository.findById(relatedRecordIds.get(0), 0).orElseThrow();
                    dimension.setLuminousIntensityExponent(rational);
                }
                break;
            default:
                conceptRecordService.setRelatedRecords(recordId, relatedRecordIds, relationType);
                break;
        }

        final XtdDimension persistentDimension = getRepository().save(dimension);
        log.trace("Updated dimension: {}", persistentDimension);
        return persistentDimension;
    }
}
