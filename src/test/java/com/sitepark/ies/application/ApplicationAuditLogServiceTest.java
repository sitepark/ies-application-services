package com.sitepark.ies.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.audit.core.domain.exception.CreateAuditLogEntryFailedException;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.usecase.CreateAuditLogUseCase;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import java.io.IOException;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApplicationAuditLogServiceTest {

  private CreateAuditLogUseCase createAuditLogUseCase;
  private AuditLogService auditLogService;
  private MultiEntityNameResolver multiEntityNameResolver;
  private ApplicationAuditLogService service;

  @BeforeEach
  void setUp() {
    this.createAuditLogUseCase = mock();
    this.auditLogService = mock();
    this.multiEntityNameResolver = mock();
    Instant timestamp = Instant.parse("2024-01-01T00:00:00Z");
    this.service =
        new ApplicationAuditLogService(
            this.createAuditLogUseCase,
            this.auditLogService,
            this.multiEntityNameResolver,
            timestamp,
            "parent-id-123");
    when(this.createAuditLogUseCase.createAuditLog(any())).thenReturn("log-id");
  }

  @Test
  void testParentIdReturnsParentId() {
    assertEquals(
        "parent-id-123", this.service.parentId(), "Should return the configured parent id");
  }

  @Test
  void testUpdateParentIdChangesParentId() {
    this.service.updateParentId("new-parent-id");
    assertEquals("new-parent-id", this.service.parentId(), "Should return the updated parent id");
  }

  @Test
  void testCreateBatchLogCallsCreateAuditLog() {
    this.service.createBatchLog(null, AuditBatchLogAction.BATCH_REMOVE);
    verify(this.createAuditLogUseCase).createAuditLog(any());
  }

  @Test
  void testCreateBatchLogReturnsLogId() {
    when(this.createAuditLogUseCase.createAuditLog(any())).thenReturn("batch-log-id");
    String logId = this.service.createBatchLog(null, AuditBatchLogAction.BATCH_REMOVE);
    assertEquals("batch-log-id", logId, "Should return the audit log ID from use case");
  }

  @Test
  void testCreateLogWithEntityRefResolvesName() {
    EntityRef entityRef = EntityRef.of(EntityRef.toTypeString(User.class), "user-1");
    when(this.multiEntityNameResolver.resolveName(any())).thenReturn("Test User");
    this.service.createLog(entityRef, AuditLogAction.UPDATE, null, null);
    verify(this.multiEntityNameResolver).resolveName(entityRef);
  }

  @Test
  void testCreateLogWithEntityRefCallsCreateAuditLog() {
    EntityRef entityRef = EntityRef.of(EntityRef.toTypeString(User.class), "user-1");
    when(this.multiEntityNameResolver.resolveName(any())).thenReturn("Test User");
    this.service.createLog(entityRef, AuditLogAction.UPDATE, null, null);
    verify(this.createAuditLogUseCase).createAuditLog(any());
  }

  @Test
  void testCreateLogWithEntityNameCallsCreateAuditLog() {
    EntityRef entityRef = EntityRef.of(EntityRef.toTypeString(User.class), "user-1");
    this.service.createLog(entityRef, "Test User", AuditLogAction.UPDATE, null, null);
    verify(this.createAuditLogUseCase).createAuditLog(any());
  }

  @Test
  void testCreateLogWithEntityNameDoesNotResolveNameFromResolver() {
    EntityRef entityRef = EntityRef.of(EntityRef.toTypeString(User.class), "user-1");
    this.service.createLog(entityRef, "Test User", AuditLogAction.UPDATE, null, null);
    verify(this.multiEntityNameResolver, never()).resolveName(any());
  }

  @Test
  void testCreateLogReturnsLogId() {
    EntityRef entityRef = EntityRef.of(EntityRef.toTypeString(User.class), "user-1");
    when(this.multiEntityNameResolver.resolveName(any())).thenReturn("Test User");
    when(this.createAuditLogUseCase.createAuditLog(any())).thenReturn("returned-log-id");
    String logId = this.service.createLog(entityRef, AuditLogAction.UPDATE, null, null);
    assertEquals("returned-log-id", logId, "Should return the audit log ID from use case");
  }

  @Test
  void testCreateLogWithStringDataDoesNotCallSerialize() throws Exception {
    EntityRef entityRef = EntityRef.of(EntityRef.toTypeString(User.class), "user-1");
    when(this.multiEntityNameResolver.resolveName(any())).thenReturn("Test User");
    this.service.createLog(entityRef, AuditLogAction.UPDATE, "string-data", "string-data");
    verify(this.auditLogService, never()).serialize(any());
  }

  @Test
  void testCreateLogWithObjectDataCallsSerialize() throws Exception {
    Object data = new Object();
    EntityRef entityRef = EntityRef.of(EntityRef.toTypeString(User.class), "user-1");
    when(this.multiEntityNameResolver.resolveName(any())).thenReturn("Test User");
    when(this.auditLogService.serialize(any())).thenReturn("serialized");
    this.service.createLog(entityRef, AuditLogAction.UPDATE, data, null);
    verify(this.auditLogService).serialize(data);
  }

  @Test
  void testCreateLogSerializationFailureThrowsCreateAuditLogEntryFailedException()
      throws Exception {
    Object data = new Object();
    EntityRef entityRef = EntityRef.of(EntityRef.toTypeString(User.class), "user-1");
    when(this.multiEntityNameResolver.resolveName(any())).thenReturn("Test User");
    when(this.auditLogService.serialize(any())).thenThrow(new IOException("serialization failed"));
    assertThrows(
        CreateAuditLogEntryFailedException.class,
        () -> this.service.createLog(entityRef, AuditLogAction.UPDATE, data, null),
        "Serialization failure should throw CreateAuditLogEntryFailedException");
  }
}
