package de.bentrm.datacat.catalog.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@Node(XtdSymbol.LABEL)
public class XtdSymbol extends XtdRoot {

    public static final String LABEL = "XtdSymbol";

    // Subject used to attach a specific “context” for using the symbol of a property
    @Relationship(type = "SUBJECT")
    private XtdSubject subject;

    // Unicode symbol of the property
    @Relationship(type = "SYMBOL")
    private XtdText symbol;
}
