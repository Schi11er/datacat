package de.bentrm.datacat.graphql.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AttachConceptsToDictionaryInput {

    @NotBlank
    private String dictionaryId;

    @NotEmpty
    private List<@NotBlank String> conceptIds;
}
