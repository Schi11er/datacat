package de.bentrm.datacat.catalog.domain;

import org.neo4j.driver.types.Relationship;
import org.springframework.util.Assert;

/**
 * Enumeration of all supported catalog record types.
 */
public enum SimpleRelationType {
    AmountOfSubstanceExponent("AMOUNT_OF_SUBSTANCE_EXPONENT"),
    BoundaryValues("BOUNDARY_VALUES"),
    Coefficient("COEFFICIENT"),
    CountryOfOrigin("COUNTRY_OF_ORIGIN"),
    DateOfCreation("DATE_OF_CREATION"),
    DateOfPublication("DATE_OF_PUBLICATION"),
    Definition("DEFINITION"),
    DeprecationExplanation("DEPRECATION_EXPLANATION"),
    Descriptions("DESCRIPTIONS"),
    Dictionary("DICTIONARY"),
    Dimension("DIMENSION"),
    ElectricCurrentExponent("ELECTRIC_CURRENT_EXPONENT"),
    Examples("EXAMPLES"),
    Languages("LANGUAGES"),
    Language("LANGUAGE"),
    LanguageOfCreator("LANGUAGE_OF_CREATOR"),
    LengthExponent("LENGTH_EXPONENT"),
    LuminousIntensityExponent("LUMINOUS_INTENSITY_EXPONENT"),
    MassExponent("MASS_EXPONENT"),
    Maximum("MAXIMUM"),
    Minimum("MINIMUM"),
    Name("NAME"),
    Names("NAMES"),
    Offset("OFFSET"),
    OrderedValues("ORDERED_VALUES"),
    PossibleValues("POSSIBLE_VALUES"),
    Properties("PROPERTIES"),
    QuantityKinds("QUANTITY_KINDS"),
    ReferenceDocuments("REFERENCE_DOCUMENTS"),
    RelationshipToProperty("RelationshipToProperty"),
    RelationshipToSubject("RelationshipToSubject"),
    RelationshipType("RELATIONSHIP_TYPE"),
    ReplacedObjects("REPLACED_OBJECTS"),
    ScopeSubjects("SCOPE_SUBJECTS"),
    SimilarTo("SIMILAR_TO"),
    Subject("SUBJECT"),
    Subdivisions("SUBDIVISIONS"),
    Symbols("SYMBOLS"),
    Symbol("SYMBOL"),
    TargetProperties("TARGET_PROPERTIES"),
    TargetSubjects("TARGET_SUBJECTS"),
    Texts("TEXTS"),
    ThermodynamicTemperatureExponent("THERMODYNAMIC_TEMPERATURE_EXPONENT"),
    TimeExponent("TIME_EXPONENT"),
    Units("UNITS"),
    Unit("UNIT"),
    Values("VALUES"),
    Value("VALUE");
    
    private final String relationProperty;

    SimpleRelationType(String relationProperty) {
        this.relationProperty = relationProperty;
    }

    public String getRelationProperty() {
        return relationProperty;
    }
}
