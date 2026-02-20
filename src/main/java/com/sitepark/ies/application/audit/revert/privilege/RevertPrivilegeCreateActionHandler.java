package com.sitepark.ies.application.audit.revert.privilege;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.privilege.RemovePrivilegesService;
import com.sitepark.ies.application.privilege.RemovePrivilegesServiceRequest;
import com.sitepark.ies.audit.core.service.RevertRequest;
import jakarta.inject.Inject;

public class RevertPrivilegeCreateActionHandler implements RevertEntityActionHandler {

  private final RemovePrivilegesService removePrivilegesService;

  @Inject
  RevertPrivilegeCreateActionHandler(RemovePrivilegesService removePrivilegesService) {
    this.removePrivilegesService = removePrivilegesService;
  }

  @Override
  public void revert(RevertRequest request) {
    this.removePrivilegesService.removePrivileges(
        RemovePrivilegesServiceRequest.builder()
            .identifiers(configure -> configure.add(request.target().id()))
            .auditParentId(request.parentId())
            .build());
  }
}
