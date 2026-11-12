package de.bentrm.datacat.base.domain;

import org.springframework.data.domain.AuditorAware;
import org.springframework.data.neo4j.core.mapping.callback.BeforeBindCallback;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

/**
 * Event listener bound to the data store.
 */
@Slf4j
@Component
@RequiredArgsConstructor 
public class PreSaveEventListener implements BeforeBindCallback<Entity> {

    private final AuditorAware<String> auditorAware;

    /**
     * Called before save of any database entity.
     * @param entity The entity to persist.
     */
    @Override
    public @NonNull Entity onBeforeBind(@NonNull Entity entity) {
        final String currentAuditor = auditorAware.getCurrentAuditor().orElse("SYSTEM");
        Instant now = Instant.now();

            if (entity.getCreated() == null) {
                entity.setCreated(now);
            }
            if (entity.getCreatedBy() == null) {
                entity.setCreatedBy(currentAuditor);
            }
            entity.setLastModified(now);
            entity.setLastModifiedBy(currentAuditor);

        return entity;
    }

}
