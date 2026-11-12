package de.bentrm.datacat.catalog.repository;

import org.springframework.stereotype.Component;

@Component
public record RecordRepositories(
        CountryRepository countryRepository,
        LanguageRepository languageRepository,
        MultiLanguageTextRepository multiLanguageTextRepository,
        TextRepository textRepository) {}
