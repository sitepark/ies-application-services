package com.sitepark.ies.application.audit.revert.role;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.role.RemoveRolesService;
import com.sitepark.ies.application.role.RemoveRolesServiceRequest;
import com.sitepark.ies.audit.core.service.RevertRequest;
import jakarta.inject.Inject;

public class RevertRoleCreateActionHandler implements RevertEntityActionHandler {

  private final RemoveRolesService removeRolesService;

  @Inject
  RevertRoleCreateActionHandler(RemoveRolesService removeRolesService) {
    this.removeRolesService = removeRolesService;
  }

  @Override
  public void revert(RevertRequest request) {
    this.removeRolesService.removeRoles(
        RemoveRolesServiceRequest.builder()
            .identifiers(configure -> configure.add(request.target().id()))
            .auditParentId(request.parentId())
            .build());
  }
}
