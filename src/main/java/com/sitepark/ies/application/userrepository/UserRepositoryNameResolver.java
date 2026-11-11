package com.sitepark.ies.application.userrepository;

import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.query.filter.Filter;
import com.sitepark.ies.userrepository.core.usecase.role.GetRolesByIdsUseCase;
import com.sitepark.ies.userrepository.core.usecase.user.GetAllUsersUseCase;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

/** Resolves the names of users and roles by their ids, e.g. for assignments. */
public final class UserRepositoryNameResolver {

  private final GetAllUsersUseCase getAllUsersUseCase;
  private final GetRolesByIdsUseCase getRolesByIdsUseCase;

  @Inject
  public UserRepositoryNameResolver(
      GetAllUsersUseCase getAllUsersUseCase, GetRolesByIdsUseCase getRolesByIdsUseCase) {
    this.getAllUsersUseCase = getAllUsersUseCase;
    this.getRolesByIdsUseCase = getRolesByIdsUseCase;
  }

  public @Nullable String resolveDisplayUserName(String userId) {
    List<User> users = getAllUsersUseCase.getAllUsers(Filter.idList(userId));
    if (users.isEmpty()) {
      return null;
    }
    return users.getFirst().toDisplayName();
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
}
