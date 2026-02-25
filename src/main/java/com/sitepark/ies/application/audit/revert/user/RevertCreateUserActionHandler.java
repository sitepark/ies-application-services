package com.sitepark.ies.application.audit.revert.user;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.application.user.RemoveUsersService;
import com.sitepark.ies.application.user.RemoveUsersServiceRequest;
import com.sitepark.ies.audit.core.service.RevertRequest;
import jakarta.inject.Inject;

public class RevertCreateUserActionHandler implements RevertEntityActionHandler {

  private final RemoveUsersService removeUserService;

  @Inject
  RevertCreateUserActionHandler(RemoveUsersService removeUserService) {
    this.removeUserService = removeUserService;
  }

  @Override
  public void revert(RevertRequest request) {
    this.removeUserService.removeUsers(
        RemoveUsersServiceRequest.builder()
            .identifiers(configure -> configure.add(request.target().id()))
            .auditParentId(request.parentId())
            .build());
  }
}
