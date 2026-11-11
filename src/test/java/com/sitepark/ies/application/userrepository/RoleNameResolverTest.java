package com.sitepark.ies.application.userrepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.usecase.role.GetRolesByIdsUseCase;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RoleNameResolverTest {

  private GetRolesByIdsUseCase getRolesByIdsUseCase;
  private RoleNameResolver resolver;

  @BeforeEach
  void setUp() {
    this.getRolesByIdsUseCase = mock();
    this.resolver = new RoleNameResolver(this.getRolesByIdsUseCase);
  }

  @Test
  void testType() {
    assertEquals("role", this.resolver.type(), "Should resolve roles");
  }

  @Test
  void testResolveNames() {
    Role role = Role.builder().id("1").name("Admin").build();
    when(this.getRolesByIdsUseCase.getRolesByIds(List.of("1"))).thenReturn(List.of(role));
    EntityRef roleRef = EntityRef.of(Role.class, "1");
    assertEquals(
        Map.of(roleRef, "Admin"),
        this.resolver.resolveNames(Set.of(roleRef)),
        "Should return the role name");
  }

  @Test
  void testResolveNamesIgnoresOtherTypes() {
    assertEquals(
        Map.of(),
        this.resolver.resolveNames(Set.of(EntityRef.of("label", "1"))),
        "Should ignore other types");
  }

  @Test
  void testResolveNamesDoesNotLoadOtherTypes() {
    this.resolver.resolveNames(Set.of(EntityRef.of("label", "1")));
    verify(this.getRolesByIdsUseCase, never()).getRolesByIds(any());
  }
}
