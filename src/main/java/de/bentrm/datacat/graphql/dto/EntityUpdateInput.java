package de.bentrm.datacat.graphql.dto;

import de.bentrm.datacat.validation.IdConstraint;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class EntityUpdateInput {

    @NotBlank @IdConstraint
    private String id;

    private @NotEmpty
    final List<@Valid @NotNull TextInput> names = new ArrayList<>();

    public String getId() {
        return id;
    }

    public List<TextInput> getNames() {
        return names;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("names", names)
                .toString();
    }
}
