package com.sitepark.ies.application.privilege;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.userrepository.core.domain.entity.Privilege;
import com.sitepark.ies.userrepository.core.domain.value.PrivilegeSnapshot;
import com.sitepark.ies.userrepository.core.usecase.privilege.RemovePrivilegeRequest;
import com.sitepark.ies.userrepository.core.usecase.privilege.RemovePrivilegeResult;
import com.sitepark.ies.userrepository.core.usecase.privilege.RemovePrivilegeUseCase;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RemovePrivilegesServiceTest {

  private RemovePrivilegeUseCase removePrivilegeUseCase;

  @SuppressWarnings("PMD.SingularField")
  private ApplicationAuditLogServiceFactory auditLogServiceFactory;

  private ApplicationAuditLogService auditLogService;
  private Clock clock;
  private RemovePrivilegesService service;

  @BeforeEach
  void setUp() {
    this.removePrivilegeUseCase = mock();
    this.auditLogServiceFactory = mock();
    this.auditLogService = mock();
    this.clock = mock();
    this.service =
        new RemovePrivilegesService(removePrivilegeUseCase, auditLogServiceFactory, clock);
    when(auditLogServiceFactory.create(any(), any())).thenReturn(auditLogService);
  }

  @Test
  void testRemovePrivilegesCallsRemovePrivilegeUseCase() {

    Privilege privilege = Privilege.builder().id("201").name("TestPrivilege").build();
    PrivilegeSnapshot snapshot = new PrivilegeSnapshot(privilege, List.of());
    Instant timestamp = Instant.now();
    RemovePrivilegeResult.Removed removedResult =
        new RemovePrivilegeResult.Removed("201", "TestPrivilege", snapshot, timestamp);

    when(removePrivilegeUseCase.removePrivilege(any(RemovePrivilegeRequest.class)))
        .thenReturn(removedResult);
    when(clock.instant()).thenReturn(timestamp);
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    RemovePrivilegesServiceRequest request =
        RemovePrivilegesServiceRequest.builder().identifiers(b -> b.id("201")).build();

    service.removePrivileges(request);

    verify(removePrivilegeUseCase).removePrivilege(any(RemovePrivilegeRequest.class));
  }

  @Test
  void testRemovePrivilegesCreatesAuditLogWhenPrivilegeWasRemoved() throws Exception {

    Privilege privilege = Privilege.builder().id("201").name("TestPrivilege").build();
    PrivilegeSnapshot snapshot = new PrivilegeSnapshot(privilege, List.of());
    Instant timestamp = Instant.now();
    RemovePrivilegeResult.Removed removedResult =
        new RemovePrivilegeResult.Removed("201", "TestPrivilege", snapshot, timestamp);

    when(removePrivilegeUseCase.removePrivilege(any(RemovePrivilegeRequest.class)))
        .thenReturn(removedResult);
    when(clock.instant()).thenReturn(timestamp);
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    RemovePrivilegesServiceRequest request =
        RemovePrivilegesServiceRequest.builder().identifiers(b -> b.id("201")).build();

    service.removePrivileges(request);

    verify(auditLogService).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testRemovePrivilegesDoesNotCreateAuditLogWhenPrivilegeWasSkipped() {

    RemovePrivilegeResult.Skipped skippedResult =
        new RemovePrivilegeResult.Skipped("1", "Built-in privilege cannot be removed");

    when(removePrivilegeUseCase.removePrivilege(any(RemovePrivilegeRequest.class)))
        .thenReturn(skippedResult);

    RemovePrivilegesServiceRequest request =
        RemovePrivilegesServiceRequest.builder().identifiers(b -> b.id("1")).build();

    service.removePrivileges(request);

    verify(auditLogService, never()).createLog(any(), any(), any(), any(), any());
  }

  @Test
  void testRemovePrivilegesCreatesBatchParentLogForMultiplePrivileges() throws Exception {

    Privilege privilege1 = Privilege.builder().id("201").name("TestPrivilege1").build();
    Privilege privilege2 = Privilege.builder().id("202").name("TestPrivilege2").build();
    PrivilegeSnapshot snapshot1 = new PrivilegeSnapshot(privilege1, List.of());
    PrivilegeSnapshot snapshot2 = new PrivilegeSnapshot(privilege2, List.of());
    Instant timestamp = Instant.now();
    RemovePrivilegeResult.Removed removedResult1 =
        new RemovePrivilegeResult.Removed("201", "TestPrivilege1", snapshot1, timestamp);
    RemovePrivilegeResult.Removed removedResult2 =
        new RemovePrivilegeResult.Removed("202", "TestPrivilege2", snapshot2, timestamp);

    when(removePrivilegeUseCase.removePrivilege(any(RemovePrivilegeRequest.class)))
        .thenReturn(removedResult1, removedResult2);
    when(clock.instant()).thenReturn(timestamp);
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    when(auditLogService.createBatchLog(any(), any()))
        .thenReturn("batch-parent-id", "audit-1", "audit-2");

    RemovePrivilegesServiceRequest request =
        RemovePrivilegesServiceRequest.builder().identifiers(b -> b.id("201").id("202")).build();

    service.removePrivileges(request);

    verify(auditLogService, times(2)).createLog(any(), any(), any(), any(), any());
  }
}
