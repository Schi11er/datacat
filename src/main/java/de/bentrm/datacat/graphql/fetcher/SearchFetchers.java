package de.bentrm.datacat.graphql.fetcher;

import de.bentrm.datacat.catalog.domain.XtdObject;
import de.bentrm.datacat.catalog.service.CatalogSearchService;
import de.bentrm.datacat.catalog.service.CatalogService;
import de.bentrm.datacat.catalog.service.value.HierarchyValue;
import de.bentrm.datacat.catalog.specification.CatalogRecordSpecification;
import de.bentrm.datacat.graphql.Connection;
import de.bentrm.datacat.graphql.dto.SpecificationMapper;
import de.bentrm.datacat.graphql.input.ApiInputMapper;
import de.bentrm.datacat.graphql.input.HierarchyFilterInput;
import de.bentrm.datacat.graphql.input.HierarchyRootNodeFilterInput;
import de.bentrm.datacat.graphql.input.SearchInput;
import de.bentrm.datacat.validation.LanguageCodeValidator;
import graphql.schema.DataFetcher;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class SearchFetchers implements QueryFetchers {

    protected final Log logger = LogFactory.getLog(SearchFetchers.class);

    final int DEFAULT_HIERARCHY_DEPTH = 10;

    @Autowired
    private ApiInputMapper inputMapper;

    @Autowired
    private SpecificationMapper specificationMapper;

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private CatalogSearchService catalogSearchService;

    @Override
    public Map<String, DataFetcher> getQueryFetchers() {
        return Map.ofEntries(
                Map.entry("search", search()),
                Map.entry("hierarchy", hierarchy())
        );
    }

    public DataFetcher<Connection<XtdObject>> search() {
        return environment -> {
            Map<String, Object> argument = environment.getArgument("input");
            logger.info("search input: {}" + argument);
            SearchInput searchInput = inputMapper.toSearchInput(argument);
            logger.info("search input: {}" + searchInput.getQuery());

            if (searchInput == null) searchInput = new SearchInput();

            Integer pageSize = environment.getArgument("pageSize");
            if (pageSize != null) searchInput.setPageSize(pageSize);

            Integer pageNumber = environment.getArgument("pageNumber");
            if (pageNumber != null) searchInput.setPageNumber(pageNumber);

            CatalogRecordSpecification spec = specificationMapper.toCatalogRecordSpecification(searchInput);
            logger.info("search spec: {}" + spec.getFilters().toString());

            if (environment.getSelectionSet().containsAnyOf("nodes/*", "pageInfo/*")) {
                Page<XtdObject> page = catalogSearchService.search(spec);
                return Connection.of(page);
            } else {
                long totalElements = catalogSearchService.count(spec);
                return Connection.empty(totalElements);
            }

        };
    }

    public DataFetcher<HierarchyValue> hierarchy() {
        return environment -> {
            Map<String, Object> argument = environment.getArgument("input");
            final HierarchyFilterInput input = inputMapper.toHierarchyFilterInput(argument);
            final HierarchyRootNodeFilterInput rootNodeFilter = input.getRootNodeFilter();
            final CatalogRecordSpecification rootNodeSpecification = specificationMapper.toCatalogRecordSpecification(rootNodeFilter);
            return catalogService.getHierarchy(rootNodeSpecification, DEFAULT_HIERARCHY_DEPTH);
        };
    }

    ;
}
