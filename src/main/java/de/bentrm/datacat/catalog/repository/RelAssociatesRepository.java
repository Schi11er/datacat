package de.bentrm.datacat.catalog.repository;

import de.bentrm.datacat.base.repository.GraphEntityRepository;
import de.bentrm.datacat.catalog.domain.XtdRelAssociates;
import org.springframework.stereotype.Repository;

@Repository
public interface RelAssociatesRepository extends GraphEntityRepository<XtdRelAssociates> {
}
