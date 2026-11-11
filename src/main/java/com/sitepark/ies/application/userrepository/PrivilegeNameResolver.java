package com.sitepark.ies.application.userrepository;

import com.sitepark.ies.application.EntityNameResolver;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import com.sitepark.ies.userrepository.core.usecase.privilege.GetPrivilegesByIdsUseCase;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Resolves the names of privileges. */
public final class PrivilegeNameResolver implements EntityNameResolver {

  private static final String ENTITY_TYPE = EntityRef.toTypeString(Privilege.class);

  private final GetPrivilegesByIdsUseCase getPrivilegesByIdsUseCase;

  @Inject
  public PrivilegeNameResolver(GetPrivilegesByIdsUseCase getPrivilegesByIdsUseCase) {
    this.getPrivilegesByIdsUseCase = getPrivilegesByIdsUseCase;
  }

  @Override
  public String type() {
    return ENTITY_TYPE;
  }

  @Override
  public Map<EntityRef, String> resolveNames(Set<EntityRef> entityRefs) {
    List<String> ids =
        entityRefs.stream()
            .filter(entityRef -> ENTITY_TYPE.equals(entityRef.type()))
            .map(EntityRef::id)
            .distinct()
            .toList();
    if (ids.isEmpty()) {
      return Map.of();
    }
    return this.getPrivilegesByIdsUseCase.getPrivilegesByIds(ids).stream()
        .collect(
            Collectors.toMap(
                privilege ->
                    EntityRef.of(
                        ENTITY_TYPE,
                        Objects.requireNonNull(privilege.id(), "stored privilege has an id")),
                Privilege::name));
  }
}
