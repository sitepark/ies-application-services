package com.sitepark.ies.application.audit.revert.user;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.application.user.UnassignRolesFromUsersService;
import com.sitepark.ies.application.user.UnassignRolesFromUsersServiceRequest;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.usecase.user.UnassignRolesFromUsersRequest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;

public class RevertUserAssignRolesActionHandler implements RevertEntityActionHandler {

  private final AuditLogService auditLogService;

  private final UnassignRolesFromUsersService unassignRolesFromUsersService;

  @Inject
  RevertUserAssignRolesActionHandler(
      AuditLogService auditLogService,
      UnassignRolesFromUsersService unassignRolesFromUsersService) {
    this.auditLogService = auditLogService;
    this.unassignRolesFromUsersService = unassignRolesFromUsersService;
  }

  @Override
  public void revert(RevertRequest request) {
    try {
      List<String> roleIds =
          this.auditLogService.deserializeList(request.backwardData(), String.class);
      this.unassignRolesFromUsersService.unassignRolesFromUsers(
          UnassignRolesFromUsersServiceRequest.builder()
              .unassignRolesFromUsersRequest(
                  UnassignRolesFromUsersRequest.builder()
                      .userIdentifiers(b -> b.id(request.target().id()))
                      .roleIdentifiers(b -> b.ids(roleIds))
                      .build())
              .auditParentId(request.parentId())
              .build());
    } catch (IOException e) {
      throw new RevertFailedException(request, "Failed to deserialize roleIds", e);
    }
  }
}
