package com.sitepark.ies.application.audit.revert.role;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.domain.value.RoleSnapshot;
import com.sitepark.ies.userrepository.core.usecase.role.RestoreRoleRequest;
import com.sitepark.ies.userrepository.core.usecase.role.RestoreRoleUseCase;
import jakarta.inject.Inject;
import java.io.IOException;

public class RevertRoleRemoveActionHandler implements RevertEntityActionHandler {

  private final AuditLogService auditLogService;

  private final RestoreRoleUseCase restoreRoleUseCase;

  @Inject
  RevertRoleRemoveActionHandler(
      AuditLogService auditLogService, RestoreRoleUseCase restoreRoleUseCase) {
    this.auditLogService = auditLogService;
    this.restoreRoleUseCase = restoreRoleUseCase;
  }

  @Override
  public void revert(RevertRequest request) {
    try {
      RoleSnapshot restoreData =
          this.auditLogService.deserialize(request.backwardData(), RoleSnapshot.class);
      this.restoreRoleUseCase.restoreRole(new RestoreRoleRequest(restoreData));
    } catch (IOException e) {
      throw new RevertFailedException(request, "Failed to deserialize role-snapshot", e);
    }
  }
}
