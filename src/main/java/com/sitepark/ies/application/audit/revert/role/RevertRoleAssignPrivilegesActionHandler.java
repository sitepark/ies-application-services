package com.sitepark.ies.application.audit.revert.role;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.usecase.role.UnassignPrivilegesFromRolesRequest;
import com.sitepark.ies.userrepository.core.usecase.role.UnassignPrivilegesFromRolesUseCase;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;

public class RevertRoleAssignPrivilegesActionHandler implements RevertEntityActionHandler {

  private final AuditLogService auditLogService;

  private final UnassignPrivilegesFromRolesUseCase unassignPrivilegesFromRolesUseCase;

  @Inject
  RevertRoleAssignPrivilegesActionHandler(
      AuditLogService auditLogService,
      UnassignPrivilegesFromRolesUseCase unassignPrivilegesFromRolesUseCase) {
    this.auditLogService = auditLogService;
    this.unassignPrivilegesFromRolesUseCase = unassignPrivilegesFromRolesUseCase;
  }

  @Override
  public void revert(RevertRequest request) {
    try {
      List<String> privilegeds =
          this.auditLogService.deserializeList(request.backwardData(), String.class);
      this.unassignPrivilegesFromRolesUseCase.unassignPrivilegesFromRoles(
          UnassignPrivilegesFromRolesRequest.builder()
              .roleIdentifiers(b -> b.id(request.target().id()))
              .privilegeIdentifiers(b -> b.ids(privilegeds))
              .build());
    } catch (IOException e) {
      throw new RevertFailedException(request, "Failed to deserialize privilegeds", e);
    }
  }
}
