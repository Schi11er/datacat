package de.bentrm.datacat.graphql.input.verification;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Data
public class findMissingDescriptionFilterInput {
    @NotNull @Valid findMissingDescriptionNodeTypeFilterInput nodeTypeFilter;
}
