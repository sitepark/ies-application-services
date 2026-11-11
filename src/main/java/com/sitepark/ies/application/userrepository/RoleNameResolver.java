package com.sitepark.ies.application.userrepository;

import com.sitepark.ies.application.EntityNameResolver;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.usecase.role.GetRolesByIdsUseCase;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Resolves the names of roles. */
public final class RoleNameResolver implements EntityNameResolver {

  private static final String ENTITY_TYPE = EntityRef.toTypeString(Role.class);

  private final GetRolesByIdsUseCase getRolesByIdsUseCase;

  @Inject
  public RoleNameResolver(GetRolesByIdsUseCase getRolesByIdsUseCase) {
    this.getRolesByIdsUseCase = getRolesByIdsUseCase;
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
    return this.getRolesByIdsUseCase.getRolesByIds(ids).stream()
        .collect(
            Collectors.toMap(
                role ->
                    EntityRef.of(
                        ENTITY_TYPE, Objects.requireNonNull(role.id(), "stored role has an id")),
                Role::name));
  }
}
