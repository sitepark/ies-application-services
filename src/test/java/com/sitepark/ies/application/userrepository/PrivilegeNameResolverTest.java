package com.sitepark.ies.application.userrepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import com.sitepark.ies.userrepository.core.usecase.privilege.GetPrivilegesByIdsUseCase;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrivilegeNameResolverTest {

  private GetPrivilegesByIdsUseCase getPrivilegesByIdsUseCase;
  private PrivilegeNameResolver resolver;

  @BeforeEach
  void setUp() {
    this.getPrivilegesByIdsUseCase = mock();
    this.resolver = new PrivilegeNameResolver(this.getPrivilegesByIdsUseCase);
  }

  @Test
  void testType() {
    assertEquals("privilege", this.resolver.type(), "Should resolve privileges");
  }

  @Test
  void testResolveNames() {
    Privilege privilege = Privilege.builder().id("1").name("Edit").build();
    when(this.getPrivilegesByIdsUseCase.getPrivilegesByIds(List.of("1")))
        .thenReturn(List.of(privilege));
    EntityRef privilegeRef = EntityRef.of(Privilege.class, "1");
    assertEquals(
        Map.of(privilegeRef, "Edit"),
        this.resolver.resolveNames(Set.of(privilegeRef)),
        "Should return the privilege name");
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
    verify(this.getPrivilegesByIdsUseCase, never()).getPrivilegesByIds(any());
  }
}
