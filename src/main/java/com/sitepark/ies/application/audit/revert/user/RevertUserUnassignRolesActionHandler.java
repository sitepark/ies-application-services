package com.sitepark.ies.application.audit.revert.user;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.audit.revert.RevertFailedException;
import com.sitepark.ies.audit.core.service.AuditLogService;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersRequest;
import com.sitepark.ies.userrepository.core.usecase.user.AssignRolesToUsersUseCase;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;

public class RevertUserUnassignRolesActionHandler implements RevertEntityActionHandler {

  private final AuditLogService auditLogService;

  private final AssignRolesToUsersUseCase assignRolesToUsersUseCase;

  @Inject
  RevertUserUnassignRolesActionHandler(
      AuditLogService auditLogService, AssignRolesToUsersUseCase assignRolesToUsersUseCase) {
    this.auditLogService = auditLogService;
    this.assignRolesToUsersUseCase = assignRolesToUsersUseCase;
  }

  @Override
  public void revert(RevertRequest request) {
    try {
      List<String> roleIds =
          this.auditLogService.deserializeList(request.backwardData(), String.class);
      this.assignRolesToUsersUseCase.assignRolesToUsers(
          AssignRolesToUsersRequest.builder()
              .userIdentifiers(b -> b.id(request.target().id()))
              .roleIdentifiers(b -> b.ids(roleIds))
              .build());
    } catch (IOException e) {
      throw new RevertFailedException(request, "Failed to deserialize roleIds", e);
    }
  }
}
