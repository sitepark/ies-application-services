package com.sitepark.ies.application.role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.MultiEntityNameResolver;
import com.sitepark.ies.sharedkernel.patch.PatchDocument;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleResult;
import com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleUseCase;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateRoleServiceTest {

  private UpdateRoleUseCase updateRoleUseCase;

  @SuppressWarnings("PMD.SingularField")
  private MultiEntityNameResolver multiEntityNameResolver;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;

  private UpdateRoleService service;

  @BeforeEach
  void setUp() {
    this.updateRoleUseCase = mock();
    this.multiEntityNameResolver = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.service =
        new UpdateRoleService(updateRoleUseCase, multiEntityNameResolver, auditLogServiceFactory);
    when(auditLogServiceFactory.create(any(), any())).thenReturn(auditLogService);
  }

  @Test
  void testUpdateRoleReturnsRoleId() {

    Role role = Role.builder().id("123").name("TestRole").build();

    PatchDocument patch = mock(PatchDocument.class);
    when(patch.toJson()).thenReturn("{\"patch\":\"forward\"}");

    PatchDocument revertPatch = mock(PatchDocument.class);
    when(revertPatch.toJson()).thenReturn("{\"patch\":\"revert\"}");

    Instant timestamp = Instant.now();
    UpdateRoleResult result =
        new UpdateRoleResult("123", "TestRole", timestamp, patch, revertPatch, null);

    when(updateRoleUseCase.updateRole(
            any(com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest.class)))
        .thenReturn(result);

    UpdateRoleServiceRequest request =
        UpdateRoleServiceRequest.builder()
            .updateRoleRequest(
                com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest.builder()
                    .role(role)
                    .build())
            .build();

    String roleId = service.updateRole(request);

    assertEquals("123", roleId, "Should return the role ID");
  }

  @Test
  void testUpdateRoleCallsUpdateRoleUseCase() {

    Role role = Role.builder().id("123").name("TestRole").build();

    PatchDocument patch = mock(PatchDocument.class);
    when(patch.toJson()).thenReturn("{\"patch\":\"forward\"}");

    PatchDocument revertPatch = mock(PatchDocument.class);
    when(revertPatch.toJson()).thenReturn("{\"patch\":\"revert\"}");

    Instant timestamp = Instant.now();
    UpdateRoleResult result =
        new UpdateRoleResult("123", "TestRole", timestamp, patch, revertPatch, null);

    when(updateRoleUseCase.updateRole(
            any(com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest.class)))
        .thenReturn(result);

    UpdateRoleServiceRequest request =
        UpdateRoleServiceRequest.builder()
            .updateRoleRequest(
                com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest.builder()
                    .role(role)
                    .build())
            .build();

    service.updateRole(request);

    verify(updateRoleUseCase)
        .updateRole(any(com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest.class));
  }

  @Test
  void testUpdateRoleCreatesAuditLogWhenRoleWasUpdated() {

    Role role = Role.builder().id("123").name("TestRole").build();

    PatchDocument patch = mock(PatchDocument.class);
    when(patch.toJson()).thenReturn("{\"patch\":\"forward\"}");

    PatchDocument revertPatch = mock(PatchDocument.class);
    when(revertPatch.toJson()).thenReturn("{\"patch\":\"revert\"}");

    Instant timestamp = Instant.now();
    UpdateRoleResult result =
        new UpdateRoleResult("123", "TestRole", timestamp, patch, revertPatch, null);

    when(updateRoleUseCase.updateRole(
            any(com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest.class)))
        .thenReturn(result);

    UpdateRoleServiceRequest request =
        UpdateRoleServiceRequest.builder()
            .updateRoleRequest(
                com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest.builder()
                    .role(role)
                    .build())
            .build();

    service.updateRole(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testUpdateRoleDoesNotCreateAuditLogWhenRoleUnchanged() {

    Role role = Role.builder().id("123").name("TestRole").build();

    Instant timestamp = Instant.now();
    UpdateRoleResult result = new UpdateRoleResult("123", "TestRole", timestamp, null, null, null);

    when(updateRoleUseCase.updateRole(
            any(com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest.class)))
        .thenReturn(result);

    UpdateRoleServiceRequest request =
        UpdateRoleServiceRequest.builder()
            .updateRoleRequest(
                com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest.builder()
                    .role(role)
                    .build())
            .build();

    service.updateRole(request);

    verify(auditLogService, never()).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testUpdateRolePassesAuditParentId() {

    Role role = Role.builder().id("123").name("TestRole").build();

    PatchDocument patch = mock(PatchDocument.class);
    when(patch.toJson()).thenReturn("{\"patch\":\"forward\"}");

    PatchDocument revertPatch = mock(PatchDocument.class);
    when(revertPatch.toJson()).thenReturn("{\"patch\":\"revert\"}");

    Instant timestamp = Instant.now();
    UpdateRoleResult result =
        new UpdateRoleResult("123", "TestRole", timestamp, patch, revertPatch, null);

    when(updateRoleUseCase.updateRole(
            any(com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest.class)))
        .thenReturn(result);

    UpdateRoleServiceRequest request =
        UpdateRoleServiceRequest.builder()
            .updateRoleRequest(
                com.sitepark.ies.userrepository.core.usecase.role.UpdateRoleRequest.builder()
                    .role(role)
                    .build())
            .auditParentId("parent-audit-123")
            .build();

    service.updateRole(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }
}
