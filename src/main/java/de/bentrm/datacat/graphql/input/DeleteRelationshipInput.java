package de.bentrm.datacat.graphql.input;

import lombok.Data;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import de.bentrm.datacat.catalog.domain.SimpleRelationType;

@Data
public class DeleteRelationshipInput {
    @NotNull SimpleRelationType relationshipType;
    @NotBlank String fromId;
    @NotBlank String toId;
}
