package de.bentrm.datacat.graphql.fetcher;

import de.bentrm.datacat.catalog.domain.XtdRelationshipToProperty;
import de.bentrm.datacat.catalog.domain.XtdProperty;
import de.bentrm.datacat.catalog.service.RelationshipToPropertyRecordService;
import de.bentrm.datacat.catalog.specification.CatalogRecordSpecification;
import de.bentrm.datacat.graphql.Connection;
import de.bentrm.datacat.graphql.dto.FilterInput;
import de.bentrm.datacat.graphql.dto.SpecificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class RelationshipToPropertyController {


    @Autowired
    private RelationshipToPropertyRecordService service;

    @Autowired
    private SpecificationMapper specificationMapper;

    @QueryMapping
    public Optional<XtdRelationshipToProperty> getRelationshipToProperty(@Argument String id) {
        return service.findByIdWithDirectRelations(id, XtdRelationshipToProperty.class.getSimpleName());
    }

    @QueryMapping
    public Connection<XtdRelationshipToProperty> findRelationshipToProperties(@Argument FilterInput input) {
        if (input == null) input = new FilterInput();
        final CatalogRecordSpecification specification = specificationMapper.toCatalogRecordSpecification(input);
        final Page<XtdRelationshipToProperty> page = service.findAll(specification);
        return Connection.of(page);
    }

    @SchemaMapping(typeName = "XtdRelationshipToProperty", field = "connectingProperty")
    public XtdProperty getConnectingProperty(XtdRelationshipToProperty relationshipToProperty) {
        return service.getConnectingProperty(relationshipToProperty);
    }

    @SchemaMapping(typeName = "XtdRelationshipToProperty", field = "targetProperties")
    public List<XtdProperty> getTargetProperties(XtdRelationshipToProperty relationshipToProperty) {
        return service.getTargetProperties(relationshipToProperty);
    }
}
