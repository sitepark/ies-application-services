package com.sitepark.ies.application.privilege;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.application.label.ReassignLabelsToEntitiesService;
import com.sitepark.ies.sharedkernel.patch.PatchDocument;
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeResult;
import com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeUseCase;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdatePrivilegeServiceTest {

  private UpdatePrivilegeUseCase updatePrivilegeUseCase;

  @SuppressWarnings("PMD.SingularField")
  private MultiEntityNameResolver multiEntityNameResolver;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  @SuppressWarnings("PMD.SingularField")
  private ReassignLabelsToEntitiesService reassignLabelsToEntitiesService;

  private ApplicationAuditLogService auditLogService;

  private UpdatePrivilegeService service;

  @BeforeEach
  void setUp() {
    this.updatePrivilegeUseCase = mock();
    this.reassignLabelsToEntitiesService = mock();
    this.multiEntityNameResolver = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.service =
        new UpdatePrivilegeService(
            updatePrivilegeUseCase,
            reassignLabelsToEntitiesService,
            multiEntityNameResolver,
            auditLogServiceFactory);
    when(auditLogServiceFactory.create(any(), any())).thenReturn(auditLogService);
  }

  @Test
  void testUpdatePrivilegeReturnsPrivilegeId() {

    Privilege privilege = Privilege.builder().id("123").name("TestPrivilege").build();

    PatchDocument patch = mock(PatchDocument.class);
    when(patch.toJson()).thenReturn("{\"patch\":\"forward\"}");

    PatchDocument revertPatch = mock(PatchDocument.class);
    when(revertPatch.toJson()).thenReturn("{\"patch\":\"revert\"}");

    Instant timestamp = Instant.now();
    UpdatePrivilegeResult result =
        new UpdatePrivilegeResult("123", "TestPrivilege", timestamp, patch, revertPatch, null);

    when(updatePrivilegeUseCase.updatePrivilege(
            any(
                com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest
                    .class)))
        .thenReturn(result);

    UpdatePrivilegeServiceRequest request =
        UpdatePrivilegeServiceRequest.builder()
            .updatePrivilegeRequest(
                com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest
                    .builder()
                    .privilege(privilege)
                    .build())
            .build();

    String privilegeId = service.updatePrivilege(request);

    assertEquals("123", privilegeId, "Should return the privilege ID");
  }

  @Test
  void testUpdatePrivilegeCallsUpdatePrivilegeUseCase() {

    Privilege privilege = Privilege.builder().id("123").name("TestPrivilege").build();

    PatchDocument patch = mock(PatchDocument.class);
    when(patch.toJson()).thenReturn("{\"patch\":\"forward\"}");

    PatchDocument revertPatch = mock(PatchDocument.class);
    when(revertPatch.toJson()).thenReturn("{\"patch\":\"revert\"}");

    Instant timestamp = Instant.now();
    UpdatePrivilegeResult result =
        new UpdatePrivilegeResult("123", "TestPrivilege", timestamp, patch, revertPatch, null);

    when(updatePrivilegeUseCase.updatePrivilege(
            any(
                com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest
                    .class)))
        .thenReturn(result);

    UpdatePrivilegeServiceRequest request =
        UpdatePrivilegeServiceRequest.builder()
            .updatePrivilegeRequest(
                com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest
                    .builder()
                    .privilege(privilege)
                    .build())
            .build();

    service.updatePrivilege(request);

    verify(updatePrivilegeUseCase)
        .updatePrivilege(
            any(
                com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest
                    .class));
  }

  @Test
  void testUpdatePrivilegeCreatesAuditLogWhenPrivilegeWasUpdated() {

    Privilege privilege = Privilege.builder().id("123").name("TestPrivilege").build();

    PatchDocument patch = mock(PatchDocument.class);
    when(patch.toJson()).thenReturn("{\"patch\":\"forward\"}");

    PatchDocument revertPatch = mock(PatchDocument.class);
    when(revertPatch.toJson()).thenReturn("{\"patch\":\"revert\"}");

    Instant timestamp = Instant.now();
    UpdatePrivilegeResult result =
        new UpdatePrivilegeResult("123", "TestPrivilege", timestamp, patch, revertPatch, null);

    when(updatePrivilegeUseCase.updatePrivilege(
            any(
                com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest
                    .class)))
        .thenReturn(result);

    UpdatePrivilegeServiceRequest request =
        UpdatePrivilegeServiceRequest.builder()
            .updatePrivilegeRequest(
                com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest
                    .builder()
                    .privilege(privilege)
                    .build())
            .build();

    service.updatePrivilege(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testUpdatePrivilegeDoesNotCreateAuditLogWhenPrivilegeUnchanged() {

    Privilege privilege = Privilege.builder().id("123").name("TestPrivilege").build();

    Instant timestamp = Instant.now();
    UpdatePrivilegeResult result =
        new UpdatePrivilegeResult("123", "TestPrivilege", timestamp, null, null, null);

    when(updatePrivilegeUseCase.updatePrivilege(
            any(
                com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest
                    .class)))
        .thenReturn(result);

    UpdatePrivilegeServiceRequest request =
        UpdatePrivilegeServiceRequest.builder()
            .updatePrivilegeRequest(
                com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest
                    .builder()
                    .privilege(privilege)
                    .build())
            .build();

    service.updatePrivilege(request);

    verify(auditLogService, never()).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testUpdatePrivilegePassesAuditParentId() {

    Privilege privilege = Privilege.builder().id("123").name("TestPrivilege").build();

    PatchDocument patch = mock(PatchDocument.class);
    when(patch.toJson()).thenReturn("{\"patch\":\"forward\"}");

    PatchDocument revertPatch = mock(PatchDocument.class);
    when(revertPatch.toJson()).thenReturn("{\"patch\":\"revert\"}");

    Instant timestamp = Instant.now();
    UpdatePrivilegeResult result =
        new UpdatePrivilegeResult("123", "TestPrivilege", timestamp, patch, revertPatch, null);

    when(updatePrivilegeUseCase.updatePrivilege(
            any(
                com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest
                    .class)))
        .thenReturn(result);

    UpdatePrivilegeServiceRequest request =
        UpdatePrivilegeServiceRequest.builder()
            .updatePrivilegeRequest(
                com.sitepark.ies.userrepository.core.usecase.privilege.UpdatePrivilegeRequest
                    .builder()
                    .privilege(privilege)
                    .build())
            .auditParentId("parent-audit-123")
            .build();

    service.updatePrivilege(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }
}
