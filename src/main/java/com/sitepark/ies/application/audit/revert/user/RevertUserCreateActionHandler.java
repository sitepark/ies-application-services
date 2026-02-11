package com.sitepark.ies.application.audit.revert.user;

import com.sitepark.ies.application.audit.revert.RevertEntityActionHandler;
import com.sitepark.ies.audit.core.service.RevertRequest;
import com.sitepark.ies.userrepository.core.usecase.user.RemoveUserRequest;
import com.sitepark.ies.userrepository.core.usecase.user.RemoveUserUseCase;
import jakarta.inject.Inject;

public class RevertUserCreateActionHandler implements RevertEntityActionHandler {

  private final RemoveUserUseCase removeUserUseCase;

  @Inject
  RevertUserCreateActionHandler(RemoveUserUseCase removeUserUseCase) {
    this.removeUserUseCase = removeUserUseCase;
  }

  @Override
  public void revert(RevertRequest request) {
    this.removeUserUseCase.removeUser(
        RemoveUserRequest.builder().id(request.target().id()).build());
  }
}
