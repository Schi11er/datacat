package de.bentrm.datacat.catalog.service.value;

import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Value
public class OneToOneRelationshipValue {
    String id;
    VersionValue version;
    List<@NotNull @Valid TranslationValue> names;
    List<@NotNull @Valid TranslationValue> descriptions;
    @NotBlank String from;
    @NotBlank String to;
}
