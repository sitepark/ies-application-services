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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MultiEntityNameResolver {

  private final GetAllUsersUseCase getAllUsersUseCase;
  private final GetRolesByIdsUseCase getRolesByIdsUseCase;
  private final GetPrivilegesByIdsUseCase getPrivilegesByIdsUseCase;
  private final GetLabelsByIdsUseCase getLabelsByIdsUseCase;

  private final Map<String, Function<Set<EntityRef>, Map<EntityRef, String>>> resolverMap;

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

    this.resolverMap =
        Map.of(
            EntityRef.toTypeString(User.class), this::getUserNames,
            EntityRef.toTypeString(Role.class), this::getRolesNames,
            EntityRef.toTypeString(Privilege.class), this::getPrivilegesNames,
            EntityRef.toTypeString(Label.class), this::getLabelNames);
  }

  public String resolveName(EntityRef entityRef) {
    Map<EntityRef, String> resolved =
        resolverMap.getOrDefault(entityRef.type(), refs -> Map.of()).apply(Set.of(entityRef));

    return resolved.get(entityRef);
  }

  public Map<EntityRef, String> resolveNames(Set<EntityRef> entityRefs) {
    return entityRefs.stream()
        .collect(Collectors.groupingBy(EntityRef::type, Collectors.toSet()))
        .entrySet()
        .stream()
        .flatMap(
            entry ->
                resolverMap
                    .getOrDefault(entry.getKey(), refs -> Map.of())
                    .apply(entry.getValue())
                    .entrySet()
                    .stream())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public String resolveDisplayUserName(String userId) {
    List<User> users = getAllUsersUseCase.getAllUsers(Filter.idList(userId));
    if (users.isEmpty()) {
      return null;
    } else {
      return users.getFirst().toDisplayName();
    }
  }

  public Map<String, String> resolveDisplayUserNames(Set<String> userIds) {
    if (userIds.isEmpty()) {
      return Map.of();
    }
    List<User> users =
        getAllUsersUseCase.getAllUsers(Filter.idList(userIds.toArray(String[]::new)));
    return users.stream().collect(Collectors.toMap(User::id, User::toDisplayName));
  }

  public Map<String, String> resolveRoleNames(Set<String> roleIds) {
    if (roleIds.isEmpty()) {
      return Map.of();
    }
    List<Role> roles = getRolesByIdsUseCase.getRolesByIds(new ArrayList<>(roleIds));
    return roles.stream().collect(Collectors.toMap(Role::id, Role::name));
  }

  private Map<EntityRef, String> getUserNames(Set<EntityRef> entityRefs) {
    Set<String> ids = extractIds(entityRefs, User.class);
    if (ids.isEmpty()) {
      return Map.of();
    }

    String type = EntityRef.toTypeString(User.class);
    List<User> users = getAllUsersUseCase.getAllUsers(Filter.idList(ids.toArray(String[]::new)));
    return users.stream()
        .collect(Collectors.toMap(user -> EntityRef.of(type, user.id()), User::toDisplayName));
  }

  private Map<EntityRef, String> getRolesNames(Set<EntityRef> entityRefs) {
    Set<String> ids = extractIds(entityRefs, Role.class);
    if (ids.isEmpty()) {
      return Map.of();
    }

    String type = EntityRef.toTypeString(Role.class);
    List<Role> roles = getRolesByIdsUseCase.getRolesByIds(new ArrayList<>(ids));
    return roles.stream()
        .collect(Collectors.toMap(role -> EntityRef.of(type, role.id()), Role::name));
  }

  private Map<EntityRef, String> getPrivilegesNames(Set<EntityRef> entityRefs) {
    Set<String> ids = extractIds(entityRefs, Privilege.class);
    if (ids.isEmpty()) {
      return Map.of();
    }

    String type = EntityRef.toTypeString(Privilege.class);
    List<Privilege> privileges = getPrivilegesByIdsUseCase.getPrivilegesByIds(new ArrayList<>(ids));
    return privileges.stream()
        .collect(
            Collectors.toMap(privilege -> EntityRef.of(type, privilege.id()), Privilege::name));
  }

  private Map<EntityRef, String> getLabelNames(Set<EntityRef> entityRefs) {
    Set<String> ids = extractIds(entityRefs, Label.class);
    if (ids.isEmpty()) {
      return Map.of();
    }

    String type = EntityRef.toTypeString(Label.class);
    List<Label> labels = getLabelsByIdsUseCase.getLabelsByIds(new ArrayList<>(ids));
    return labels.stream()
        .collect(Collectors.toMap(label -> EntityRef.of(type, label.id()), Label::name));
  }

  private <T> Set<String> extractIds(Set<EntityRef> entityRefs, Class<T> type) {
    return entityRefs.stream()
        .filter(entityRef -> EntityRef.toTypeString(type).equals(entityRef.type()))
        .map(EntityRef::id)
        .collect(Collectors.toSet());
  }
}
