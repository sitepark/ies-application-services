package com.sitepark.ies.application.audit.revert.role;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.application.role.AssignPrivilegesToRolesService;
import com.sitepark.ies.application.role.AssignPrivilegesToRolesServiceRequest;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.usecase.role.AssignPrivilegesToRolesRequest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;

public class RevertRoleUnassignPrivilegesActionHandler implements RevertEntityActionHandler {

  private final AuditLogService auditLogService;

  private final AssignPrivilegesToRolesService assignPrivilegesToRolesService;

  @Inject
  RevertRoleUnassignPrivilegesActionHandler(
      AuditLogService auditLogService,
      AssignPrivilegesToRolesService assignPrivilegesToRolesService) {
    this.auditLogService = auditLogService;
    this.assignPrivilegesToRolesService = assignPrivilegesToRolesService;
  }

  @Override
  public void revert(RevertRequest request) {
    try {
      List<String> privilegeIds =
          this.auditLogService.deserializeList(request.backwardData(), String.class);
      this.assignPrivilegesToRolesService.assignPrivilegesToRoles(
          AssignPrivilegesToRolesServiceRequest.builder()
              .assignPrivilegesToRolesRequest(
                  AssignPrivilegesToRolesRequest.builder()
                      .roleIdentifiers(b -> b.id(request.target().id()))
                      .privilegeIdentifiers(b -> b.ids(privilegeIds))
                      .build())
              .auditParentId(request.parentId())
              .build());
    } catch (IOException e) {
      throw new RevertFailedException(request, "Failed to deserialize privilegeIds", e);
    }
  }
}
