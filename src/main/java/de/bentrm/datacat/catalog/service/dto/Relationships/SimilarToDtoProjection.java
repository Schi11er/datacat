package de.bentrm.datacat.catalog.service.dto.Relationships;

import java.util.Set;

import de.bentrm.datacat.catalog.service.dto.EntityDtoProjection;

public interface SimilarToDtoProjection extends EntityDtoProjection {
    
    Set<EntityDtoProjection> getSimilarTo();
}
