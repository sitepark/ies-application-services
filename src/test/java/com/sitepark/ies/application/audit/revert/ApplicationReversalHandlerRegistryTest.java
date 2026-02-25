package com.sitepark.ies.application.audit.revert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sitepark.ies.audit.core.service.ReverseActionHandler;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ApplicationReversalHandlerRegistryTest {

  @Test
  void testGetHandlerReturnsRegisteredHandler() {
    ReverseActionHandler handler = mock();
    when(handler.getEntityType()).thenReturn("user");
    ApplicationReversalHandlerRegistry registry =
        new ApplicationReversalHandlerRegistry(Set.of(handler));
    assertEquals(
        handler,
        registry.getHandler("user"),
        "getHandler() should return the handler registered for the given entity type");
  }

  @Test
  void testGetHandlerWithNullTypeUsesAllEntitiesHandler() {
    ReverseActionHandler handler = mock();
    when(handler.getEntityType()).thenReturn(RevertEntityActionHandler.ALL_ENTITIES);
    ApplicationReversalHandlerRegistry registry =
        new ApplicationReversalHandlerRegistry(Set.of(handler));
    assertEquals(
        handler,
        registry.getHandler(null),
        "getHandler(null) should return the handler registered for ALL_ENTITIES (\"*\")");
  }

  @Test
  void testGetHandlerWithUnknownTypeThrowsIllegalArgumentException() {
    ApplicationReversalHandlerRegistry registry = new ApplicationReversalHandlerRegistry(Set.of());
    assertThrows(
        IllegalArgumentException.class,
        () -> registry.getHandler("unknown"),
        "getHandler() with an unregistered entity type should throw IllegalArgumentException");
  }

  @Test
  void testConstructorWithDuplicateHandlerTypeThrowsIllegalStateException() {
    ReverseActionHandler handler1 = mock();
    ReverseActionHandler handler2 = mock();
    when(handler1.getEntityType()).thenReturn("user");
    when(handler2.getEntityType()).thenReturn("user");
    assertThrows(
        IllegalStateException.class,
        () -> new ApplicationReversalHandlerRegistry(Set.of(handler1, handler2)),
        "Constructor should throw IllegalStateException when two handlers share the same entity"
            + " type");
  }
}
