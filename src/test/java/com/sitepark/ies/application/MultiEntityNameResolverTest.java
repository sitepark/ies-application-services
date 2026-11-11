package com.sitepark.ies.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.sharedkernel.domain.EntityRef;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MultiEntityNameResolverTest {

  private EntityNameResolver labelResolver;
  private EntityNameResolver roleResolver;
  private MultiEntityNameResolver resolver;

  @BeforeEach
  void setUp() {
    this.labelResolver = mock();
    this.roleResolver = mock();
    when(this.labelResolver.type()).thenReturn("label");
    when(this.roleResolver.type()).thenReturn("role");
    this.resolver = new MultiEntityNameResolver(Set.of(this.labelResolver, this.roleResolver));
  }

  @Test
  void testResolveNameDelegatesToTheResolverOfTheType() {
    EntityRef labelRef = EntityRef.of("label", "1");
    when(this.labelResolver.resolveNames(Set.of(labelRef))).thenReturn(Map.of(labelRef, "Label"));
    assertEquals("Label", this.resolver.resolveName(labelRef), "Should return the label name");
  }

  @Test
  void testResolveNameReturnsNullForUnknownType() {
    assertNull(
        this.resolver.resolveName(EntityRef.of("unknown-type", "123")),
        "Unknown entity type should return null");
  }

  @Test
  void testResolveNameReturnsNullForMissingEntity() {
    EntityRef labelRef = EntityRef.of("label", "1");
    when(this.labelResolver.resolveNames(any())).thenReturn(Map.of());
    assertNull(this.resolver.resolveName(labelRef), "Missing entity should return null");
  }

  @Test
  void testResolveNamesReturnsEmptyMapForEmptySet() {
    assertEquals(Map.of(), this.resolver.resolveNames(Set.of()), "Should return empty map");
  }

  @Test
  void testResolveNamesForMultipleTypes() {
    EntityRef labelRef = EntityRef.of("label", "1");
    EntityRef roleRef = EntityRef.of("role", "2");
    when(this.labelResolver.resolveNames(Set.of(labelRef))).thenReturn(Map.of(labelRef, "Label"));
    when(this.roleResolver.resolveNames(Set.of(roleRef))).thenReturn(Map.of(roleRef, "Admin"));
    assertEquals(
        Map.of(labelRef, "Label", roleRef, "Admin"),
        this.resolver.resolveNames(Set.of(labelRef, roleRef, EntityRef.of("unknown", "3"))),
        "Should resolve the names of all known types");
  }

  @Test
  void testResolveNamesCallsOnlyTheResolversOfTheTypes() {
    EntityRef labelRef = EntityRef.of("label", "1");
    when(this.labelResolver.resolveNames(any())).thenReturn(Map.of());
    this.resolver.resolveNames(Set.of(labelRef));
    verify(this.roleResolver, never()).resolveNames(any());
  }

  @Test
  void testDuplicateTypeIsRejected() {
    EntityNameResolver duplicate = mock();
    when(duplicate.type()).thenReturn("label");
    assertThrows(
        IllegalArgumentException.class,
        () -> new MultiEntityNameResolver(Set.of(this.labelResolver, duplicate)),
        "Two resolvers for one type should be rejected");
  }
}
