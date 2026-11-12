package de.bentrm.datacat.graphql.fetcher;

import de.bentrm.datacat.catalog.domain.CatalogRecord;
import de.bentrm.datacat.catalog.service.CatalogService;
import de.bentrm.datacat.graphql.dto.CatalogStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor 
public class BaseController {

    private final CatalogService catalogService;

    @QueryMapping
    public CatalogStatistics statistics() {
        return catalogService.getStatistics();
    }

    @QueryMapping
    public Optional<CatalogRecord> node(@Argument String id) {
        return catalogService.getEntryById(id);
    }

    // @QueryMapping
    // public Optional<XtdObject> objectById(@Argument String id) {
    //     return catalogService.getObject(id);
    // }
}
