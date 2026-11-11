package com.sitepark.ies.application.userrepository;

import com.sitepark.ies.application.EntityNameResolver;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.query.filter.Filter;
import com.sitepark.ies.userrepository.core.usecase.user.GetAllUsersUseCase;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Resolves the display names of users. */
public final class UserNameResolver implements EntityNameResolver {

  private static final String ENTITY_TYPE = EntityRef.toTypeString(User.class);

  private final GetAllUsersUseCase getAllUsersUseCase;

  @Inject
  public UserNameResolver(GetAllUsersUseCase getAllUsersUseCase) {
    this.getAllUsersUseCase = getAllUsersUseCase;
  }

  @Override
  public String type() {
    return ENTITY_TYPE;
  }

  @Override
  public Map<EntityRef, String> resolveNames(Set<EntityRef> entityRefs) {
    String[] ids =
        entityRefs.stream()
            .filter(entityRef -> ENTITY_TYPE.equals(entityRef.type()))
            .map(EntityRef::id)
            .distinct()
            .toArray(String[]::new);
    if (ids.length == 0) {
      return Map.of();
    }
    return this.getAllUsersUseCase.getAllUsers(Filter.idList(ids)).stream()
        .collect(
            Collectors.toMap(
                user ->
                    EntityRef.of(
                        ENTITY_TYPE, Objects.requireNonNull(user.id(), "stored user has an id")),
                User::toDisplayName));
  }
}
