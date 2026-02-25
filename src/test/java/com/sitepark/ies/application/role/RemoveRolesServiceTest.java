package com.sitepark.ies.application.role;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.userrepository.core.domain.entity.Role;
import com.sitepark.ies.userrepository.core.domain.value.RoleSnapshot;
import com.sitepark.ies.userrepository.core.usecase.role.RemoveRoleRequest;
import com.sitepark.ies.userrepository.core.usecase.role.RemoveRoleResult;
import com.sitepark.ies.userrepository.core.usecase.role.RemoveRoleUseCase;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RemoveRolesServiceTest {

  private RemoveRoleUseCase removeRoleUseCase;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;
  private Clock clock;
  private RemoveRolesService service;

  @BeforeEach
  void setUp() {
    this.removeRoleUseCase = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.clock = mock();
    this.service = new RemoveRolesService(removeRoleUseCase, auditLogServiceFactory, clock);
    when(auditLogServiceFactory.create(any(), any())).thenReturn(auditLogService);
  }

  @Test
  void testRemoveRolesCallsRemoveRoleUseCase() {

    Role role = Role.builder().id("123").name("TestRole").build();
    RoleSnapshot snapshot = new RoleSnapshot(role, List.of(), List.of());
    Instant timestamp = Instant.now();
    RemoveRoleResult.Removed removedResult =
        new RemoveRoleResult.Removed("123", "TestRole", snapshot, timestamp);

    when(removeRoleUseCase.removeRole(any(RemoveRoleRequest.class))).thenReturn(removedResult);
    when(clock.instant()).thenReturn(timestamp);
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    RemoveRolesServiceRequest request =
        RemoveRolesServiceRequest.builder().identifiers(b -> b.id("123")).build();

    service.removeRoles(request);

    verify(removeRoleUseCase).removeRole(any(RemoveRoleRequest.class));
  }

  @Test
  void testRemoveRolesCreatesAuditLogWhenRoleWasRemoved() throws Exception {

    Role role = Role.builder().id("123").name("TestRole").build();
    RoleSnapshot snapshot = new RoleSnapshot(role, List.of(), List.of());
    Instant timestamp = Instant.now();
    RemoveRoleResult.Removed removedResult =
        new RemoveRoleResult.Removed("123", "TestRole", snapshot, timestamp);

    when(removeRoleUseCase.removeRole(any(RemoveRoleRequest.class))).thenReturn(removedResult);
    when(clock.instant()).thenReturn(timestamp);
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    RemoveRolesServiceRequest request =
        RemoveRolesServiceRequest.builder().identifiers(b -> b.id("123")).build();

    service.removeRoles(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testRemoveRolesDoesNotCreateAuditLogWhenRoleWasSkipped() {

    RemoveRoleResult.Skipped skippedResult =
        new RemoveRoleResult.Skipped("1", "Built-in role cannot be removed");

    when(removeRoleUseCase.removeRole(any(RemoveRoleRequest.class))).thenReturn(skippedResult);

    RemoveRolesServiceRequest request =
        RemoveRolesServiceRequest.builder().identifiers(b -> b.id("1")).build();

    service.removeRoles(request);

    verify(auditLogService, never()).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testRemoveRolesCreatesBatchParentLogForMultipleRoles() throws Exception {

    Role role1 = Role.builder().id("123").name("TestRole1").build();
    Role role2 = Role.builder().id("456").name("TestRole2").build();
    RoleSnapshot snapshot1 = new RoleSnapshot(role1, List.of(), List.of());
    RoleSnapshot snapshot2 = new RoleSnapshot(role2, List.of(), List.of());
    Instant timestamp = Instant.now();
    RemoveRoleResult.Removed removedResult1 =
        new RemoveRoleResult.Removed("123", "TestRole1", snapshot1, timestamp);
    RemoveRoleResult.Removed removedResult2 =
        new RemoveRoleResult.Removed("456", "TestRole2", snapshot2, timestamp);

    when(removeRoleUseCase.removeRole(any(RemoveRoleRequest.class)))
        .thenReturn(removedResult1, removedResult2);
    when(clock.instant()).thenReturn(timestamp);
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    when(auditLogService.createBatchLog(any(), any()))
        .thenReturn("batch-parent-id", "audit-1", "audit-2");

    RemoveRolesServiceRequest request =
        RemoveRolesServiceRequest.builder().identifiers(b -> b.id("123").id("456")).build();

    service.removeRoles(request);

    verify(auditLogService, times(2)).createLog(any(), any(), any(), any(), any());
  }
}
