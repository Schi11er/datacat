package de.bentrm.datacat.catalog.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@NodeEntity(label = XtdRoot.LABEL)
public abstract class XtdRoot extends CatalogRecord {

    public static final String LABEL = "XtdRoot";

    @Relationship(type = XtdRelCollects.RELATIONSHIP_TYPE, direction = Relationship.INCOMING)
    private final Set<XtdRelCollects> collectedBy = new HashSet<>();

    @Relationship(type = XtdRelClassifies.RELATIONSHIP_TYPE, direction = Relationship.INCOMING)
    private final Set<XtdRelClassifies> classifiedBy = new HashSet<>();

    // @Relationship(type = XtdRelDocuments.RELATIONSHIP_TYPE, direction = Relationship.INCOMING)
    // private final Set<XtdRelDocuments> documentedBy = new HashSet<>();

    @Override
    public List<XtdRelationship> getOwnedRelationships()
    {
        return Stream
                .of(classifiedBy) // TODO wieder entfernen, ist nur Behelf
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
