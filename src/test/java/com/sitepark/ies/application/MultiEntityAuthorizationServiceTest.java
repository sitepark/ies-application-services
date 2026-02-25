package com.sitepark.ies.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.sharedkernel.security.EntityAuthorizationService;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MultiEntityAuthorizationServiceTest {

  private EntityAuthorizationService userAuthService;
  private MultiEntityAuthorizationService authService;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    this.userAuthService = mock();
    doReturn(User.class).when(this.userAuthService).type();
    this.authService = new MultiEntityAuthorizationService(Set.of(this.userAuthService));
  }

  @Test
  @SuppressWarnings("unchecked")
  void testConstructorWithDuplicateTypeThrowsIllegalArgumentException() {
    EntityAuthorizationService duplicate = mock();
    doReturn(User.class).when(duplicate).type();
    assertThrows(
        IllegalArgumentException.class,
        () -> new MultiEntityAuthorizationService(Set.of(this.userAuthService, duplicate)),
        "Duplicate type registration should throw IllegalArgumentException");
  }

  @Test
  void testIsCreatableReturnsTrueWhenAuthServiceReturnsTrue() {
    when(this.userAuthService.isCreatable()).thenReturn(true);
    assertTrue(
        this.authService.isCreatable(User.class),
        "isCreatable should delegate to registered auth service");
  }

  @Test
  void testIsCreatableReturnsFalseWhenAuthServiceReturnsFalse() {
    when(this.userAuthService.isCreatable()).thenReturn(false);
    assertFalse(
        this.authService.isCreatable(User.class),
        "isCreatable should return false when auth service returns false");
  }

  @Test
  void testIsReadableReturnsTrueWhenAuthServiceReturnsTrue() {
    EntityRef entityRef = EntityRef.of(EntityRef.toTypeString(User.class), "user-1");
    when(this.userAuthService.isReadable("user-1")).thenReturn(true);
    assertTrue(
        this.authService.isReadable(entityRef),
        "isReadable should delegate to registered auth service");
  }

  @Test
  void testIsWritableReturnsTrueWhenAuthServiceReturnsTrue() {
    EntityRef entityRef = EntityRef.of(EntityRef.toTypeString(User.class), "user-1");
    when(this.userAuthService.isWritable("user-1")).thenReturn(true);
    assertTrue(
        this.authService.isWritable(entityRef),
        "isWritable should delegate to registered auth service");
  }

  @Test
  void testIsRemovableReturnsTrueWhenAuthServiceReturnsTrue() {
    EntityRef entityRef = EntityRef.of(EntityRef.toTypeString(User.class), "user-1");
    when(this.userAuthService.isRemovable("user-1")).thenReturn(true);
    assertTrue(
        this.authService.isRemovable(entityRef),
        "isRemovable should delegate to registered auth service");
  }

  @Test
  void testIsCreatableWithUnknownTypeThrowsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> this.authService.isCreatable(Role.class),
        "Unknown type should throw IllegalArgumentException");
  }
}
