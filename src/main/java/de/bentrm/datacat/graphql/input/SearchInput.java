package de.bentrm.datacat.graphql.input;

import de.bentrm.datacat.catalog.domain.EntityType;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SearchInput {
    private String query;
    private List<@NotNull @Valid EntryFilterInput> filters;
    private List<@NotNull EntityType> entityTypeIn;
    private List<@NotNull EntityType> entityTypeNotIn;
    private List<@NotNull String> idIn;
    private List<@NotNull String> idNotIn;
    private List<@NotNull String> tagged;
    private Integer pageNumber = 0;
    private Integer pageSize = 10;
}
