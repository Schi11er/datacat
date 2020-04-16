package de.bentrm.datacat.domain.relationship;

import de.bentrm.datacat.domain.XtdRoot;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity(label = XtdRelActsUpon.LABEL)
public class XtdRelActsUpon extends XtdRelationship implements Association {

    public static final String LABEL = "XtdRelActsUpon";
    public static final String RELATIONSHIP_TYPE = "ACTS_UPON";

    @Relationship(type = RELATIONSHIP_TYPE, direction = Relationship.INCOMING)
    private XtdRoot relatingThing;

    @Relationship(type = RELATIONSHIP_TYPE)
    private Set<XtdRoot> relatedThings = new HashSet<>();

    public XtdRoot getRelatingThing() {
        return relatingThing;
    }

    public void setRelatingThing(XtdRoot relatingThing) {
        this.relatingThing = relatingThing;
    }

    public Set<XtdRoot> getRelatedThings() {
        return relatedThings;
    }

    public void setRelatedThings(Set<XtdRoot> relatedThings) {
        this.relatedThings = relatedThings;
    }

}
