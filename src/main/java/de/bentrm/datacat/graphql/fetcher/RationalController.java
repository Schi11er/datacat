package de.bentrm.datacat.graphql.fetcher;

import de.bentrm.datacat.catalog.domain.XtdRational;
import de.bentrm.datacat.catalog.service.RationalRecordService;
import de.bentrm.datacat.catalog.specification.CatalogRecordSpecification;
import de.bentrm.datacat.graphql.Connection;
import de.bentrm.datacat.graphql.dto.FilterInput;
import de.bentrm.datacat.graphql.dto.SpecificationMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
@RequiredArgsConstructor 
public class RationalController {

    private final RationalRecordService service;

    private final SpecificationMapper specificationMapper;

    @QueryMapping
    public Optional<XtdRational> getRational(@Argument String id) {
        return service.findByIdWithDirectRelations(id, XtdRational.class.getSimpleName());
    }
    
    @QueryMapping
    public Connection<XtdRational> findRationals(@Argument FilterInput input) {
        if (input == null) input = new FilterInput();
        final CatalogRecordSpecification specification = specificationMapper.toCatalogRecordSpecification(input);
        final Page<XtdRational> page = service.findAll(specification);
        return Connection.of(page);
    }
}
