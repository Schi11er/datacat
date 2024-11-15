package de.bentrm.datacat.catalog.service.value.verification;

import de.bentrm.datacat.catalog.domain.XtdRoot;
import lombok.Value;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Value
public class findMissingDescriptionValue {
    @NotNull List<XtdRoot> nodes;
    @NotNull List<List<String>> paths;
}
