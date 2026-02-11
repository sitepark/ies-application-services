package com.sitepark.ies.application.audit.revert.role;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesRequest;
import com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesUseCase;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;

public class RevertRoleUnassignPrivilegesActionHandler implements RevertEntityActionHandler {

  private final AuditLogService auditLogService;

  private final AssignPrivilegesToRolesUseCase assignPrivilegesToRolesUseCase;

  @Inject
  RevertRoleUnassignPrivilegesActionHandler(
      AuditLogService auditLogService,
      AssignPrivilegesToRolesUseCase assignPrivilegesToRolesUseCase) {
    this.auditLogService = auditLogService;
    this.assignPrivilegesToRolesUseCase = assignPrivilegesToRolesUseCase;
  }

  @Override
  public void revert(RevertRequest request) {
    try {
      List<String> privilegeIds =
          this.auditLogService.deserializeList(request.backwardData(), String.class);
      this.assignPrivilegesToRolesUseCase.assignPrivilegesToRoles(
          AssignPrivilegesToRolesRequest.builder()
              .roleIdentifiers(b -> b.id(request.target().id()))
              .privilegeIdentifiers(b -> b.ids(privilegeIds))
              .build());
    } catch (IOException e) {
      throw new RevertFailedException(request, "Failed to deserialize privilegeIds", e);
    }
  }
}
