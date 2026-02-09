package com.sitepark.ies.application;

import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.label.core.usecase.GetLabelsByIdsUseCase;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.privilege.GetPrivilegesByIdsUseCase;
import com.sitepark.ies.userrepository.core.usecase.query.filter.Filter;
import com.sitepark.ies.userrepository.core.usecase.role.GetRolesByIdsUseCase;
import com.sitepark.ies.userrepository.core.usecase.user.GetAllUsersUseCase;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("PMD.UseConcurrentHashMap")
public final class MultiEntityNameResolver {

  private final GetAllUsersUseCase getAllUsersUseCase;
  private final GetRolesByIdsUseCase getRolesByIdsUseCase;
  private final GetPrivilegesByIdsUseCase getPrivilegesByIdsUseCase;
  private final GetLabelsByIdsUseCase getLabelsByIdsUseCase;

  @Inject
  public MultiEntityNameResolver(
      GetAllUsersUseCase getAllUsersUseCase,
      GetRolesByIdsUseCase getRolesByIdsUseCase,
      GetPrivilegesByIdsUseCase getPrivilegesByIdsUseCase,
      GetLabelsByIdsUseCase getLabelsByIdsUseCase) {
    this.getAllUsersUseCase = getAllUsersUseCase;
    this.getRolesByIdsUseCase = getRolesByIdsUseCase;
    this.getPrivilegesByIdsUseCase = getPrivilegesByIdsUseCase;
    this.getLabelsByIdsUseCase = getLabelsByIdsUseCase;
  }

  public Map<EntityRef, String> resolveNames(Set<EntityRef> entityRefs) {
    Map<EntityRef, String> resolved = new HashMap<>();

    resolved.putAll(this.getUserNames(entityRefs));
    resolved.putAll(this.getRolesNames(entityRefs));
    resolved.putAll(this.getPrivilegesNames(entityRefs));
    resolved.putAll(this.getLabelNames(entityRefs));

    return resolved;
  }

  private Map<EntityRef, String> getUserNames(Set<EntityRef> entityRefs) {

    String type = EntityRef.toTypeString(User.class);
    Set<String> ids =
        entityRefs.stream()
            .filter(entityRef -> type.equals(entityRef.type()))
            .map(EntityRef::id)
            .collect(Collectors.toSet());
    if (ids.isEmpty()) {
      return Map.of();
    }
    List<User> users =
        this.getAllUsersUseCase.getAllUsers(Filter.idList(ids.toArray(String[]::new)));
    return users.stream()
        .collect(Collectors.toMap(user -> EntityRef.of(type, user.id()), User::toDisplayName));
  }

  private Map<EntityRef, String> getRolesNames(Set<EntityRef> entityRefs) {

    String type = EntityRef.toTypeString(Role.class);
    Set<String> ids =
        entityRefs.stream()
            .filter(entityRef -> type.equals(entityRef.type()))
            .map(EntityRef::id)
            .collect(Collectors.toSet());
    if (ids.isEmpty()) {
      return Map.of();
    }
    List<Role> roles = this.getRolesByIdsUseCase.getRolesByIds(new ArrayList<>(ids));
    return roles.stream()
        .collect(Collectors.toMap(role -> EntityRef.of(type, role.id()), Role::name));
  }

  private Map<EntityRef, String> getPrivilegesNames(Set<EntityRef> entityRefs) {
    String type = EntityRef.toTypeString(Privilege.class);
    Set<String> ids =
        entityRefs.stream()
            .filter(entityRef -> type.equals(entityRef.type()))
            .map(EntityRef::id)
            .collect(Collectors.toSet());
    if (ids.isEmpty()) {
      return Map.of();
    }
    List<Privilege> privileges =
        this.getPrivilegesByIdsUseCase.getPrivilegesByIds(new ArrayList<>(ids));
    return privileges.stream()
        .collect(
            Collectors.toMap(privilege -> EntityRef.of(type, privilege.id()), Privilege::name));
  }

  private Map<EntityRef, String> getLabelNames(Set<EntityRef> entityRefs) {
    String type = EntityRef.toTypeString(Label.class);
    Set<String> ids =
        entityRefs.stream()
            .filter(entityRef -> type.equals(entityRef.type()))
            .map(EntityRef::id)
            .collect(Collectors.toSet());
    if (ids.isEmpty()) {
      return Map.of();
    }
    List<Label> privileges = this.getLabelsByIdsUseCase.getLabelsByIds(new ArrayList<>(ids));
    return privileges.stream()
        .collect(Collectors.toMap(label -> EntityRef.of(type, label.id()), Label::name));
  }
}
