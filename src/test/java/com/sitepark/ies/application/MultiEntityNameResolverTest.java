package com.sitepark.ies.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.label.core.domain.entity.Label;
import com.sitepark.ies.label.core.usecase.GetLabelsByIdsUseCase;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.usecase.privilege.GetPrivilegesByIdsUseCase;
import com.sitepark.ies.userrepository.core.usecase.role.GetRolesByIdsUseCase;
import com.sitepark.ies.userrepository.core.usecase.user.GetAllUsersUseCase;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MultiEntityNameResolverTest {

  private GetAllUsersUseCase getAllUsersUseCase;
  private GetRolesByIdsUseCase getRolesByIdsUseCase;
  private GetPrivilegesByIdsUseCase getPrivilegesByIdsUseCase;
  private GetLabelsByIdsUseCase getLabelsByIdsUseCase;
  private MultiEntityNameResolver resolver;

  @BeforeEach
  void setUp() {
    this.getAllUsersUseCase = mock();
    this.getRolesByIdsUseCase = mock();
    this.getPrivilegesByIdsUseCase = mock();
    this.getLabelsByIdsUseCase = mock();
    this.resolver =
        new MultiEntityNameResolver(
            this.getAllUsersUseCase,
            this.getRolesByIdsUseCase,
            this.getPrivilegesByIdsUseCase,
            this.getLabelsByIdsUseCase);
  }

  @Test
  void testResolveNameForUserType() {
    User user = User.builder().id("1").login("user").lastName("Test").build();
    when(this.getAllUsersUseCase.getAllUsers(any())).thenReturn(List.of(user));
    EntityRef userRef = EntityRef.of(EntityRef.toTypeString(User.class), "1");
    String name = this.resolver.resolveName(userRef);
    assertEquals(user.toDisplayName(), name, "Should return user's display name");
  }

  @Test
  void testResolveNameForRoleType() {
    Role role = Role.builder().id("1").name("Admin").build();
    when(this.getRolesByIdsUseCase.getRolesByIds(any())).thenReturn(List.of(role));
    EntityRef roleRef = EntityRef.of(EntityRef.toTypeString(Role.class), "1");
    String name = this.resolver.resolveName(roleRef);
    assertEquals("Admin", name, "Should return role name");
  }

  @Test
  void testResolveNameForLabelType() {
    Label label = Label.builder().id("1").name("TestLabel").build();
    when(this.getLabelsByIdsUseCase.getLabelsByIds(any())).thenReturn(List.of(label));
    EntityRef labelRef = EntityRef.of(EntityRef.toTypeString(Label.class), "1");
    String name = this.resolver.resolveName(labelRef);
    assertEquals("TestLabel", name, "Should return label name");
  }

  @Test
  void testResolveNameReturnsNullForUnknownType() {
    EntityRef unknownRef = EntityRef.of("unknown-type", "123");
    String name = this.resolver.resolveName(unknownRef);
    assertNull(name, "Unknown entity type should return null");
  }

  @Test
  void testResolveNamesReturnsEmptyMapForEmptySet() {
    Map<EntityRef, String> names = this.resolver.resolveNames(Set.of());
    assertEquals(Map.of(), names, "Empty input should return empty map");
  }

  @Test
  void testResolveNamesForMultipleTypes() {
    User user = User.builder().id("1").login("user").lastName("Test").build();
    Role role = Role.builder().id("2").name("Admin").build();
    when(this.getAllUsersUseCase.getAllUsers(any())).thenReturn(List.of(user));
    when(this.getRolesByIdsUseCase.getRolesByIds(any())).thenReturn(List.of(role));
    EntityRef userRef = EntityRef.of(EntityRef.toTypeString(User.class), "1");
    EntityRef roleRef = EntityRef.of(EntityRef.toTypeString(Role.class), "2");
    Map<EntityRef, String> names = this.resolver.resolveNames(Set.of(userRef, roleRef));
    assertEquals(2, names.size(), "Should resolve names for both entity types");
  }

  @Test
  void testResolveDisplayUserNameReturnsDisplayName() {
    User user = User.builder().id("1").login("testuser").lastName("Test").build();
    when(this.getAllUsersUseCase.getAllUsers(any())).thenReturn(List.of(user));
    String name = this.resolver.resolveDisplayUserName("1");
    assertEquals(user.toDisplayName(), name, "Should return user's display name");
  }

  @Test
  void testResolveDisplayUserNameReturnsNullWhenUserNotFound() {
    when(this.getAllUsersUseCase.getAllUsers(any())).thenReturn(List.of());
    String name = this.resolver.resolveDisplayUserName("999");
    assertNull(name, "Should return null when user is not found");
  }

  @Test
  void testResolveDisplayUserNamesReturnsEmptyMapForEmptySet() {
    Map<String, String> names = this.resolver.resolveDisplayUserNames(Set.of());
    assertEquals(Map.of(), names, "Empty input should return empty map without calling use case");
  }

  @Test
  void testResolveDisplayUserNamesCallsGetAllUsersUseCase() {
    User user = User.builder().id("1").login("testuser").lastName("Test").build();
    when(this.getAllUsersUseCase.getAllUsers(any())).thenReturn(List.of(user));
    this.resolver.resolveDisplayUserNames(Set.of("1"));
    verify(this.getAllUsersUseCase).getAllUsers(any());
  }

  @Test
  void testResolveRoleNamesReturnsEmptyMapForEmptySet() {
    Map<String, String> names = this.resolver.resolveRoleNames(Set.of());
    assertEquals(Map.of(), names, "Empty input should return empty map without calling use case");
  }

  @Test
  void testResolveRoleNamesReturnsNamesForRoles() {
    Role role = Role.builder().id("1").name("Admin").build();
    when(this.getRolesByIdsUseCase.getRolesByIds(any())).thenReturn(List.of(role));
    Map<String, String> names = this.resolver.resolveRoleNames(Set.of("1"));
    assertEquals(Map.of("1", "Admin"), names, "Should return role names map");
  }

  @Test
  void testResolvePrivilegeNamesReturnsEmptyMapForEmptySet() {
    Map<String, String> names = this.resolver.resolvePrivilegeNames(Set.of());
    assertEquals(Map.of(), names, "Empty input should return empty map without calling use case");
  }

  @Test
  void testResolvePrivilegeNamesReturnsNamesForPrivileges() {
    Privilege privilege = Privilege.builder().id("1").name("ReadData").build();
    when(this.getPrivilegesByIdsUseCase.getPrivilegesByIds(any())).thenReturn(List.of(privilege));
    Map<String, String> names = this.resolver.resolvePrivilegeNames(Set.of("1"));
    assertEquals(Map.of("1", "ReadData"), names, "Should return privilege names map");
  }
}
