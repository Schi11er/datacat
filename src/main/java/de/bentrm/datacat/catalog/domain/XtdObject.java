package de.bentrm.datacat.catalog.domain;

import lombok.Getter;
import lombok.ToString;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@ToString(callSuper = true)
@NodeEntity(label = XtdObject.LABEL)
public abstract class XtdObject extends XtdRoot {

    public static final String TITLE = "Object";
    public static final String TITLE_PLURAL = "Objects";
    public static final String LABEL = PREFIX + TITLE;

    @Relationship(type = XtdRelAssignsCollections.RELATIONSHIP_TYPE)
    private final Set<XtdRelAssignsCollections> assignedCollections = new HashSet<>();

    @ToString.Exclude
    @Relationship(type = XtdRelAssignsProperties.RELATIONSHIP_TYPE)
    private final Set<XtdRelAssignsProperties> assignedProperties = new HashSet<>();

    @ToString.Exclude
    @Relationship(type = "ASSIGNS_PROPERTY_WITH_VALUES")
    private final Set<XtdRelAssignsPropertyWithValues> assignedPropertiesWithValues = new HashSet<>();

    @Override
    public List<XtdRelationship> getOwnedRelationships() {
        return Stream
                .of(
                    super.getOwnedRelationships(),
                    assignedCollections,
                    assignedProperties,
                    assignedPropertiesWithValues
                )
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
