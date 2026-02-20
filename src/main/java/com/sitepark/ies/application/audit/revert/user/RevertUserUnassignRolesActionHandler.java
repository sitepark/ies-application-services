package com.sitepark.ies.application.audit.revert.user;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.application.user.AssignRolesToUsersService;
import com.sitepark.ies.application.user.AssignRolesToUsersServiceRequest;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;

public class RevertUserUnassignRolesActionHandler implements RevertEntityActionHandler {

  private final AuditLogService auditLogService;

  private final AssignRolesToUsersService assignRolesToUsersService;

  @Inject
  RevertUserUnassignRolesActionHandler(
      AuditLogService auditLogService, AssignRolesToUsersService assignRolesToUsersService) {
    this.auditLogService = auditLogService;
    this.assignRolesToUsersService = assignRolesToUsersService;
  }

  @Override
  public void revert(RevertRequest request) {
    try {
      List<String> roleIds =
          this.auditLogService.deserializeList(request.backwardData(), String.class);
      this.assignRolesToUsersService.assignRolesToUsers(
          AssignRolesToUsersServiceRequest.builder()
              .assignRolesToUsersRequest(
                  AssignRolesToUsersRequest.builder()
                      .userIdentifiers(b -> b.id(request.target().id()))
                      .roleIdentifiers(b -> b.ids(roleIds))
                      .build())
              .build());
    } catch (IOException e) {
      throw new RevertFailedException(request, "Failed to deserialize roleIds", e);
    }
  }
}
