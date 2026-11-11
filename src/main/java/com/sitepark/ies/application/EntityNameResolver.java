package com.sitepark.ies.application;

import com.sitepark.ies.sharedkernel.domain.EntityRef;
import java.util.Map;
import java.util.Set;

/**
 * Resolves the names of the entities of one type, e.g. for the audit log.
 *
 * <p>The {@link MultiEntityNameResolver} gets all implementations injected. Each bounded context
 * contributes the resolver of its entities, so the application services do not depend on all
 * cores.
 */
public interface EntityNameResolver {

  /**
   * The type of the entities this resolver resolves.
   *
   * @return the entity type as of {@link EntityRef#toTypeString(Class)}
   */
  String type();

  /**
   * Resolves the names of the given entities, all of the type of this resolver.
   *
   * @param entityRefs the entities whose names are resolved
   * @return the names of the entities that exist; missing entities are left out
   */
  Map<EntityRef, String> resolveNames(Set<EntityRef> entityRefs);
}
