package com.sitepark.ies.application.audit.revert.privilege;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.domain.value.PrivilegeSnapshot;
import com.sitepark.ies.userrepository.core.usecase.privilege.RestorePrivilegeRequest;
import com.sitepark.ies.userrepository.core.usecase.privilege.RestorePrivilegeUseCase;
import jakarta.inject.Inject;
import java.io.IOException;

public class RevertPrivilegeRemoveActionHandler implements RevertEntityActionHandler {

  private final AuditLogService auditLogService;

  private final RestorePrivilegeUseCase restorePrivilegeUseCase;

  @Inject
  RevertPrivilegeRemoveActionHandler(
      AuditLogService auditLogService, RestorePrivilegeUseCase restorePrivilegeUseCase) {
    this.auditLogService = auditLogService;
    this.restorePrivilegeUseCase = restorePrivilegeUseCase;
  }

  @Override
  public void revert(RevertRequest request) {
    try {
      PrivilegeSnapshot restoreData =
          this.auditLogService.deserialize(request.backwardData(), PrivilegeSnapshot.class);
      this.restorePrivilegeUseCase.restorePrivilege(new RestorePrivilegeRequest(restoreData));
    } catch (IOException e) {
      throw new RevertFailedException(request, "Failed to deserialize privilege-snapshot", e);
    }
  }
}
