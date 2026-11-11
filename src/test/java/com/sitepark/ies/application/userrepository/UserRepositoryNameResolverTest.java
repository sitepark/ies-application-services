package com.sitepark.ies.application.userrepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.role.GetRolesByIdsUseCase;
import com.sitepark.ies.userrepository.core.usecase.user.GetAllUsersUseCase;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserRepositoryNameResolverTest {

  private GetAllUsersUseCase getAllUsersUseCase;
  private GetRolesByIdsUseCase getRolesByIdsUseCase;
  private UserRepositoryNameResolver resolver;

  @BeforeEach
  void setUp() {
    this.getAllUsersUseCase = mock();
    this.getRolesByIdsUseCase = mock();
    this.resolver =
        new UserRepositoryNameResolver(this.getAllUsersUseCase, this.getRolesByIdsUseCase);
  }

  @Test
  void testResolveDisplayUserNameReturnsDisplayName() {
    User user = User.builder().id("1").login("user").lastName("Test").build();
    when(this.getAllUsersUseCase.getAllUsers(any())).thenReturn(List.of(user));
    assertEquals(
        user.toDisplayName(),
        this.resolver.resolveDisplayUserName("1"),
        "Should return the display name");
  }

  @Test
  void testResolveDisplayUserNameReturnsNullWhenUserNotFound() {
    when(this.getAllUsersUseCase.getAllUsers(any())).thenReturn(List.of());
    assertNull(this.resolver.resolveDisplayUserName("1"), "Missing user should return null");
  }

  @Test
  void testResolveDisplayUserNamesReturnsEmptyMapForEmptySet() {
    assertEquals(Map.of(), this.resolver.resolveDisplayUserNames(Set.of()), "Should be empty");
  }

  @Test
  void testResolveDisplayUserNames() {
    User user = User.builder().id("1").login("user").lastName("Test").build();
    when(this.getAllUsersUseCase.getAllUsers(any())).thenReturn(List.of(user));
    assertEquals(
        Map.of("1", user.toDisplayName()),
        this.resolver.resolveDisplayUserNames(Set.of("1")),
        "Should return the display names by id");
  }

  @Test
  void testResolveRoleNamesReturnsEmptyMapForEmptySet() {
    assertEquals(Map.of(), this.resolver.resolveRoleNames(Set.of()), "Should be empty");
  }

  @Test
  void testResolveRoleNames() {
    Role role = Role.builder().id("1").name("Admin").build();
    when(this.getRolesByIdsUseCase.getRolesByIds(any())).thenReturn(List.of(role));
    assertEquals(
        Map.of("1", "Admin"),
        this.resolver.resolveRoleNames(Set.of("1")),
        "Should return the role names by id");
  }
}
