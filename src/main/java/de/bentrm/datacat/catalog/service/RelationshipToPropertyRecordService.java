package de.bentrm.datacat.catalog.service;

import java.util.List;

import de.bentrm.datacat.catalog.domain.XtdProperty;
import de.bentrm.datacat.catalog.domain.XtdRelationshipToProperty;
import jakarta.validation.constraints.NotNull;

public interface RelationshipToPropertyRecordService extends RelationshipRecordService<XtdRelationshipToProperty> {

    XtdProperty getConnectingProperty(@NotNull XtdRelationshipToProperty relationshipToProperty);
    
    List<XtdProperty> getTargetProperties(@NotNull XtdRelationshipToProperty relationshipToProperty);
}
