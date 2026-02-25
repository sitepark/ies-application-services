package com.sitepark.ies.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.audit.AuditBatchLogAction;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.usecase.CreateAuditLogUseCase;
import com.sitepark.ies.sharedkernel.domain.EntityRef;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApplicationAuditLogServiceFactoryTest {

  private CreateAuditLogUseCase createAuditLogUseCase;

  @SuppressWarnings("PMD.SingularField")
  private AuditLogService auditLogService;

  @SuppressWarnings("PMD.SingularField")
  private MultiEntityNameResolver multiEntityNameResolver;

  private ApplicationAuditLogServiceFactory factory;

  @BeforeEach
  void setUp() {
    this.createAuditLogUseCase = mock();
    this.auditLogService = mock();
    this.multiEntityNameResolver = mock();
    this.factory =
        new ApplicationAuditLogServiceFactory(
            this.createAuditLogUseCase, this.auditLogService, this.multiEntityNameResolver);
    when(this.createAuditLogUseCase.createAuditLog(any())).thenReturn("batch-id");
  }

  @Test
  void testCreateReturnsApplicationAuditLogService() {
    ApplicationAuditLogService service = this.factory.create(Instant.now(), null);
    assertNotNull(service, "Factory should return a non-null ApplicationAuditLogService");
  }

  @Test
  void testCreateSetsParentId() {
    ApplicationAuditLogService service = this.factory.create(Instant.now(), "parent-id");
    assertEquals(
        "parent-id", service.parentId(), "Should create service with the specified parent id");
  }

  @Test
  void testCreatePerTypeForBatchWithSingleEntityReturnsSingleEntry() {
    EntityRef userRef = EntityRef.of(EntityRef.toTypeString(User.class), "user-1");
    Map<String, ApplicationAuditLogService> services =
        this.factory.createPerTypeForBatch(
            Instant.now(), null, AuditBatchLogAction.BATCH_REMOVE, List.of(userRef));
    assertEquals(1, services.size(), "Single entity type should return one service");
  }

  @Test
  void testCreatePerTypeForBatchWithSingleTypeDoesNotCreateBatchLog() {
    EntityRef userRef = EntityRef.of(EntityRef.toTypeString(User.class), "user-1");
    this.factory.createPerTypeForBatch(
        Instant.now(), null, AuditBatchLogAction.BATCH_REMOVE, List.of(userRef));
    verify(this.createAuditLogUseCase, never()).createAuditLog(any());
  }

  @Test
  void testCreatePerTypeForBatchWithMultipleTypesReturnsTwoEntries() {
    EntityRef userRef = EntityRef.of(EntityRef.toTypeString(User.class), "user-1");
    EntityRef roleRef = EntityRef.of(EntityRef.toTypeString(Role.class), "role-1");
    Map<String, ApplicationAuditLogService> services =
        this.factory.createPerTypeForBatch(
            Instant.now(), null, AuditBatchLogAction.BATCH_REMOVE, List.of(userRef, roleRef));
    assertEquals(2, services.size(), "Two entity types should return two services");
  }

  @Test
  void testCreatePerTypeForBatchWithMultipleTypesCreatesBatchLog() {
    EntityRef userRef = EntityRef.of(EntityRef.toTypeString(User.class), "user-1");
    EntityRef roleRef = EntityRef.of(EntityRef.toTypeString(Role.class), "role-1");
    this.factory.createPerTypeForBatch(
        Instant.now(), null, AuditBatchLogAction.BATCH_REMOVE, List.of(userRef, roleRef));
    verify(this.createAuditLogUseCase).createAuditLog(any());
  }

  @Test
  void testCreatePerTypeForBatchWithMultipleEntitiesOfSameTypeCreatesBatchLog() {
    EntityRef userRef1 = EntityRef.of(EntityRef.toTypeString(User.class), "user-1");
    EntityRef userRef2 = EntityRef.of(EntityRef.toTypeString(User.class), "user-2");
    this.factory.createPerTypeForBatch(
        Instant.now(), null, AuditBatchLogAction.BATCH_REMOVE, List.of(userRef1, userRef2));
    verify(this.createAuditLogUseCase).createAuditLog(any());
  }
}
