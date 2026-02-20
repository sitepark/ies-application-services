package com.sitepark.ies.application.audit.revert.role;

import com.sitepark.ies.application.ApplicationAuditLogService;
import com.sitepark.ies.application.ApplicationAuditLogServiceFactory;
import com.sitepark.ies.application.audit.AuditLogAction;
import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.domain.value.AuditLogTarget;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.domain.entity.User;
import com.sitepark.ies.userrepository.core.domain.value.RoleSnapshot;
import com.sitepark.ies.userrepository.core.usecase.role.RestoreRoleRequest;
import com.sitepark.ies.userrepository.core.usecase.role.RestoreRoleResult;
import com.sitepark.ies.userrepository.core.usecase.role.RestoreRoleUseCase;
import jakarta.inject.Inject;
import java.io.IOException;

public class RevertRoleRemoveActionHandler implements RevertEntityActionHandler {

  private final ApplicationAuditLogServiceFactory auditLogServiceFactory;
  private final AuditLogService auditLogService;
  private final RestoreRoleUseCase restoreRoleUseCase;

  @Inject
  RevertRoleRemoveActionHandler(
      ApplicationAuditLogServiceFactory auditLogServiceFactory,
      AuditLogService auditLogService,
      RestoreRoleUseCase restoreRoleUseCase) {
    this.auditLogServiceFactory = auditLogServiceFactory;
    this.auditLogService = auditLogService;
    this.restoreRoleUseCase = restoreRoleUseCase;
  }

  @Override
  public void revert(RevertRequest request) {
    try {
      RoleSnapshot restoreData =
          this.auditLogService.deserialize(request.backwardData(), RoleSnapshot.class);
      RestoreRoleResult result =
          this.restoreRoleUseCase.restoreRole(new RestoreRoleRequest(restoreData));
      if (result instanceof RestoreRoleResult.Restored restored) {
        this.createRestoreAuditLog(restored, request.parentId());
      }
    } catch (IOException e) {
      throw new RevertFailedException(request, "Failed to deserialize role-snapshot", e);
    }
  }

  private void createRestoreAuditLog(RestoreRoleResult.Restored restored, String auditParentId) {

    ApplicationAuditLogService auditLogService =
        this.auditLogServiceFactory.create(restored.timestamp(), auditParentId);

    auditLogService.createLog(
        AuditLogTarget.of(User.class, restored.roleId(), restored.snapshot().role().name()),
        AuditLogAction.RESTORE,
        null,
        restored.snapshot());
  }
}
