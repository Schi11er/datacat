package de.bentrm.datacat.graphql.payload;

import de.bentrm.datacat.catalog.domain.XtdObject;
import lombok.Data;

@Data
public class AddCommentPayload {
    XtdObject catalogEntry;
}
