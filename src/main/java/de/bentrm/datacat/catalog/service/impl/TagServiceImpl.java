package de.bentrm.datacat.catalog.service.impl;

import de.bentrm.datacat.catalog.domain.Tag;
import de.bentrm.datacat.catalog.repository.TagRepository;
import de.bentrm.datacat.catalog.service.TagService;
import de.bentrm.datacat.catalog.specification.TagSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;
import jakarta.validation.constraints.NotNull;

@Service
public class TagServiceImpl extends AbstractQueryServiceImpl<Tag, TagRepository> implements TagService {

    public TagServiceImpl(Neo4jTemplate neo4jTemplate, TagRepository repository) {
        super(Tag.class, neo4jTemplate, repository);
    }

    @Override
    public Page<Tag> findAll(@NotNull TagSpecification specification) {
        return super.findAll(specification);
    }

    @Override
    public long count(@NotNull TagSpecification specification) {
        return super.count(specification);
    }
}
